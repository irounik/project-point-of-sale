package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.pojo.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInventoryQuantity {
    private String barcode;
    private String productName;
    private Integer requiredQuantity;
    private Inventory inventory;
}
