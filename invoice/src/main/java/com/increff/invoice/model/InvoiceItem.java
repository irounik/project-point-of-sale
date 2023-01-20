package com.increff.invoice.model;

import lombok.Data;

@Data
public class InvoiceItem {

    private Double price;

    private Integer quantity;

    private String name;

}
