package com.increff.ironic.pos.util;

public class ValidationUtil {

    public static boolean isBlank(String input) {
        return input != null && input.trim().isEmpty();
    }

    public static boolean isPositiveNumber(Double number) {
        return number != null && number > 0;
    }

    public static boolean isNegative(Integer number) {
        return number == null || number < 0;
    }

}
