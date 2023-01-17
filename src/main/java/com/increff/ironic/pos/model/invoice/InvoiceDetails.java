package com.increff.ironic.pos.model.invoice;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceDetails {

    private Integer orderId;

    private LocalDateTime time;

    private List<InvoiceItem> items;

}
