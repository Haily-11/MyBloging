package com.blog.mylog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Small utility to print a BCrypt hash for a given password.
 * Usage:
 *  - with argument: java -cp ... com.blog.mylog.util.PasswordEncoderTool myPassword
 *  - or run without args and type the password when prompted
 */
public class
PasswordEncoderTool {

    public static void main(String[] args) throws Exception {
        String raw;
        if (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            raw = args[0];
        } else {
            System.out.print("Enter password to encode: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            raw = reader.readLine();
        }

        if (raw == null || raw.isEmpty()) {
            System.err.println("No password provided. Exiting.");
            System.exit(2);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(raw);

        System.out.println("Raw password: " + raw);
        System.out.println("BCrypt hash: " + encoded);
    }

}

