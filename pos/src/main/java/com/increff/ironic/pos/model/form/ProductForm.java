package com.increff.ironic.pos.model.form;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ProductForm {

    private String name;

    @JsonAlias("brand")
    private String brandName;

    private String category;

    private Double price;

    private String barcode;

}
