package com.increff.ironic.pos.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerDaySaleData {

    private LocalDate date;

    private Integer ordersCount;

    private Integer itemsCount;

    private Double totalRevenue;

}
