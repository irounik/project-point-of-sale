package com.increff.ironic.pos.model.data;

import lombok.Data;

@Data
public class OrderItemData {

    String barcode;

    Double sellingPrice;

    Integer quantity;

    String name;

}
