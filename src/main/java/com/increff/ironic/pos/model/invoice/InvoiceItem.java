package com.increff.ironic.pos.model.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceItem {

    private Double price;

    private Integer quantity;

    private String name;

}
