package com.increff.ironic.pos.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandReportForm {

    private String brand;
    private String category;

}
