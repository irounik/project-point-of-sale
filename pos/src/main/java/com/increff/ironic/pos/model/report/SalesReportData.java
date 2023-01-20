package com.increff.ironic.pos.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportData {

    private String category;

    private String brandName;

    private Integer quantity;

    private Double revenue;

}
