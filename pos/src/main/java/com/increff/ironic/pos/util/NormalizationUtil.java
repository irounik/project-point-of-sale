package com.increff.ironic.pos.util;

import com.increff.ironic.pos.pojo.OrderItem;

public class NormalizationUtil {

    public static String normalize(String input) {
        return input.trim().toLowerCase();
    }

    public static Double normalize(Double input) {
        String[] parts = input.toString().split("\\.");
        if (parts.length == 1) return input;

        String integerPart = parts[0];
        String decimalPart = parts[1];

        if (decimalPart.length() <= 2) return input;
        decimalPart = decimalPart.substring(0, 2);

        String doubleStr = integerPart + "." + decimalPart;
        return Double.parseDouble(doubleStr);
    }

    public static void normalizeOrderItem(OrderItem orderItem) {
        double normalizedPrice = NormalizationUtil.normalize(orderItem.getSellingPrice());
        orderItem.setSellingPrice(normalizedPrice);
    }

}