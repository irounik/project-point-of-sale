package com.increff.ironic.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryData {

    private Integer id;

    private String barcode;

    private String productName;

    private Integer quantity;

}
