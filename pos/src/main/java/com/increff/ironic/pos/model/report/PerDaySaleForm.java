package com.increff.ironic.pos.model.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PerDaySaleForm {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}
