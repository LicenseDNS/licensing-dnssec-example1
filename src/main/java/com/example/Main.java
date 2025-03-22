package com.example;

import java.io.IOException;
import java.util.List;

public class Main {

    // product Id from https://manager.licensedns.net
    private static final String PRODUCT_ID = "ADA14AE9-08A8-4AE2-B69E-AAE277B8346F";
    
    // license key example
    private static final String LICENSE_KEY = "5F32A-UN7KF-UE9V8-AW3NS";

    public static void main(String[] args) {

        try {
            // get the license key from the user
            // product Id should be hardcoded in source

            // ACTIVATE
            List<String> licenseTXT1 = LicenseData.activate(LICENSE_KEY, PRODUCT_ID);
            if (licenseTXT1 != null) {
                if (licenseTXT1.contains("result=success")) {
                    System.out.println("LICENSE VALID, ACTIVATED");
                } else if (licenseTXT1.contains("result=fail")) {
                    System.out.println("LICENSE INVALID");
                }

                System.out.println("------------------");
                licenseTXT1.forEach(System.out::println);
                System.out.println("------------------");
            } else {
                System.out.println("INVALID REQUEST, connection problem etc");
            }

            // DEACTIVATE
            List<String> licenseTXT2 = LicenseData.deactivate(LICENSE_KEY, PRODUCT_ID);
            if (licenseTXT2 != null) {
                if (licenseTXT2.contains("result=success")) {
                    System.out.println("LICENSE DEACTIVATED");
                } else if (licenseTXT2.contains("result=fail")) {
                    System.out.println("INVALID REQUEST");
                }

                System.out.println("------------------");
                licenseTXT2.forEach(System.out::println);
                System.out.println("------------------");
            } else {
                System.out.println("INVALID REQUEST, connection problem etc");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
