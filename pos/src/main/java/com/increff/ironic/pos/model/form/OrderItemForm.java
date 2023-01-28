package com.increff.ironic.pos.model.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemForm {

    private String barcode;

    private Integer quantity;

    private Double sellingPrice;

}
