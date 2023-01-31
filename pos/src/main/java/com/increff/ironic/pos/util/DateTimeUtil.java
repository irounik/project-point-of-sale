package com.increff.ironic.pos.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeUtil {

    private static final LocalDateTime MIN_START_DATE = LocalDateTime.of(0, 1, 1, 1, 1);

    public static LocalDateTime formatStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            startDate = MIN_START_DATE;
        }
        // Start date should have time: 12:00:00 AM
        return startDate.toLocalDate().atTime(0, 0, 0);
    }

    public static LocalDateTime formatEndDate(LocalDateTime endDate) {
        if (endDate == null) {
            endDate = LocalDateTime.now(ZoneOffset.UTC);
        }
        // End date should have time: 11:59:59 PM
        return endDate.toLocalDate().atTime(23, 59, 59);
    }


}
