package com.increff.ironic.pos.model.data;

import lombok.Data;

@Data
public class OrderItemData {

    private String barcode;

    private Double sellingPrice;

    private Integer quantity;

    private String name;

}
