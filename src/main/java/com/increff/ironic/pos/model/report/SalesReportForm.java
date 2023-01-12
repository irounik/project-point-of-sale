package com.increff.ironic.pos.model.report;

import lombok.Data;

import java.util.Date;

@Data
public class SalesReportForm {

    private Date startDate;

    private Date endDate;

    private String brandName;

    private String category;

}
