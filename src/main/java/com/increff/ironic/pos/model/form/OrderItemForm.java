package com.increff.ironic.pos.model.form;

import lombok.Data;

@Data
public class OrderItemForm {

    private String barcode;

    private Integer quantity;

}
