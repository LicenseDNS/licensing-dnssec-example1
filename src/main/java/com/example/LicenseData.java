package com.example;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;
import org.xbill.DNS.dnssec.ValidatingResolver;

public class LicenseData {

    // Root anchors, see https://data.iana.org/root-anchors/root-anchors.xml
    private static final String ROOT
            = ". IN DS 20326 8 2 E06D44B80B8F1D39A95C0B0D7C65D08458E880409BBC683457104237C7F8EC8D\n"
            + ". IN DS 38696 8 2 683D2D0ACB8C9B712A1948B27F741219298D0A450D612C483AF444A4C0FB2B16";

    public static List<String> activate(String licenseKey, String productId) throws IOException {
        return verifyGetLicenseData("a", licenseKey, productId); // a letter for activation
    }

    public static List<String> deactivate(String licenseKey, String productId) throws IOException {
        return verifyGetLicenseData("d", licenseKey, productId); // d letter for deactivation
    }

    public static List<String> verifyGetLicenseData(String action, String licenseKey, String productId) throws IOException {
        // get DNS server list defined in local operating system
        List<InetSocketAddress> dnsServers = ResolverConfig.getCurrentConfig().servers();

        String activation = action; // action
        String domain = DigestUtils.sha256Hex(licenseKey + productId).substring(0, 32); // key and product hash
        String fingerprint = "some-fingerprint"; // fingerprint, device id, anything max 32 chars
        String mainDomain = "q.licensedns.net.";

        // concanate to make domain to query
        String query = activation + "." + domain + "." + fingerprint + "." + mainDomain;

        // use local configured first dns server
        ValidatingResolver vr = new ValidatingResolver(new SimpleResolver(dnsServers.getFirst()));
        // load ROOT trust anchors
        vr.loadTrustAnchors(new ByteArrayInputStream(ROOT.getBytes(StandardCharsets.US_ASCII)));

        // record
        org.xbill.DNS.Record qr = org.xbill.DNS.Record.newRecord(Name.fromConstantString(query), Type.TXT, DClass.IN);
        // send query and get the response
        Message response = vr.send(Message.newQuery(qr));

        // check if AD flag is true, means DNSSEC validation succeded
        if (response.getHeader().getFlag(Flags.AD) && response.getRcode() == Rcode.NOERROR) {
            List<String> listTXT = new ArrayList<>();

            for (RRset set : response.getSectionRRsets(Section.ANSWER)) {
                if (set.getType() == Type.TXT) {
                    set.rrs().forEach((r) -> {
                        TXTRecord t = (TXTRecord) r;
                        listTXT.addAll(t.getStrings());
                    });
                }
            }

            return listTXT;
        } else {
            // if DNSSEC validation is false, return null
            return null;
        }
    }
}
