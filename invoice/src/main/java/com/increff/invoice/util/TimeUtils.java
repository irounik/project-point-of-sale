package com.increff.invoice.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    // Get date in required format
    public static String getCurrentDate() {
        LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC); // Current date
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(currentDate);
    }

    // Get time in required format
    public static String getCurrentTime() {
        LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(currentDate);
    }

}
