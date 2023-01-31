package com.increff.ironic.pos.model.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryForm {

    private String barcode;

    private Integer quantity;

}
