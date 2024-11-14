package com.hmdp.utils;
import java.util.regex.Pattern;

public class PhoneValidator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public static boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static void main(String[] args) {
        System.out.println(isValidPhone("13812345678")); // true
        System.out.println(isValidPhone("12345678901")); // false
        System.out.println(isValidPhone(""));
    }
}
