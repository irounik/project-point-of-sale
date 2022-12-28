package com.increff.ironic.pos.model.data;

import lombok.Data;

@Data
public class OrderItemData {

    String barcode;

    Double price;

    Integer quantity;

    String name;

}
