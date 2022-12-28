package com.increff.ironic.pos.util;

import java.util.Formatter;

public class NormalizationUtil {

    public static String normalize(String input) {
        return input.trim().toLowerCase();
    }

    public static Double normalize(Double input) {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", input);
        return input;
    }

}