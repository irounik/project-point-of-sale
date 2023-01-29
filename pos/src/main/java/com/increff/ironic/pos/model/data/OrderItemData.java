package com.increff.ironic.pos.model.data;

import lombok.Data;

@Data
public class OrderItemData {
    // TODO: 29/01/23 access modifiers are missing
    String barcode;

    Double sellingPrice;

    Integer quantity;

    String name;

}
