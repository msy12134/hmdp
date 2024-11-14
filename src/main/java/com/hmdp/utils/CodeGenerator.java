package com.hmdp.utils;

import java.security.SecureRandom;

public class CodeGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateCode(6)); // Example: 123456
    }
}