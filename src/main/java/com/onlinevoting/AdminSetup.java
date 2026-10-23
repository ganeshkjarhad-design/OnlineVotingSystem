
package com.onlinevoting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;

public class AdminSetup {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter new admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter new admin password: ");
        char[] password = System.console() != null
                ? System.console().readPassword()
                : scanner.nextLine().toCharArray();

        if (username.isEmpty() || password.length == 0) {
            System.out.println("Username and password are required.");
            java.util.Arrays.fill(password, '\0');
            return;
        }

        byte[] bytes = new String(password)
                .getBytes(StandardCharsets.UTF_8);

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(bytes);

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }

            System.out.println("\nGenerated password hash:");
            System.out.println(hex);

            System.out.println("\nCopy this hash into the admins table.");
            System.out.println("Do not share it publicly.");

        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(bytes, (byte) 0);
            scanner.close();
        }
    }
}
