package com.increff.ironic.pos.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.ApiException;

public class BrandApiDto {

    private static final String CANT_BE_BLANK = "can't be blank!";

    public static void validate(BrandForm form) throws ApiException {
        String name = form.getName();
        String category = form.getCategory();

        if (name == null || name.trim().isEmpty()) {
            throwException("Name " + CANT_BE_BLANK);
        }
        if (category == null || category.trim().isEmpty()) {
            throwException("Category " + CANT_BE_BLANK);
        }
    }

    private static void throwException(String message) throws ApiException {
        throw new ApiException(message);
    }

    public static Brand convert(BrandForm form) throws ApiException {
        validate(form);

        Brand brand = new Brand();
        brand.setName(normalize(form.getName()));
        brand.setCategory(normalize(form.getCategory()));

        return brand;
    }

    private static String normalize(String input) {
        return input.trim().toLowerCase();
    }

    public static BrandData convert(Brand brand) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(brand, BrandData.class);
    }

}
