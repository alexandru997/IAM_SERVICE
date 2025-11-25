package com.post_hub.iam_Service.security.encrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String super_admin_passwor = encoder.encode("Test11111!");
        String admin_password = encoder.encode("Test22222!");
        String user_password = encoder.encode("Test33333!");

        System.out.println("Hashed first_password: " + super_admin_passwor);
        System.out.println("Hashed second_password: " + admin_password);
        System.out.println("Hashed third_password: " + user_password);
    }
}
