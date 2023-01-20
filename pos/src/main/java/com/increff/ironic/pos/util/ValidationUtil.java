package com.increff.ironic.pos.util;

public class ValidationUtil {

    private static final String EMAIL_PATTERN = "[a-z\\d]+@[a-z]+\\.[a-z]{2,3}";

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isPositiveNumber(Double number) {
        return number != null && number > 0;
    }

    public static boolean isPositiveNumber(Integer number) {
        return number != null && number > 0;
    }

    public static boolean isNegative(Integer number) {
        return number == null || number < 0;
    }

    public static boolean isValidEmail(String email) {
        return email.matches(EMAIL_PATTERN);
    }

}
