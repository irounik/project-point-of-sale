package com.increff.ironic.pos.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReportData {

    private String brand;

    private String category;

    private Integer quantity;

}
