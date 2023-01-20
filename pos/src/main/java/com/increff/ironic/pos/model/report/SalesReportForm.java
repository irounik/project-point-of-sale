package com.increff.ironic.pos.model.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesReportForm {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String brandName;

    private String category;

}
