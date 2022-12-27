package com.increff.ironic.pos.model.data;

import lombok.Data;

@Data
public class InventoryData {

    private String barcode;

    private String productName;

    private Integer quantity;

}
