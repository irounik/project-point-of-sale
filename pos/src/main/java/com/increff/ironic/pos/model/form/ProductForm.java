package com.increff.ironic.pos.model.form;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductForm {

    private String name;

    @JsonAlias("brand")
    private String brandName;

    private String category;

    private Double price;

    private String barcode;

}
