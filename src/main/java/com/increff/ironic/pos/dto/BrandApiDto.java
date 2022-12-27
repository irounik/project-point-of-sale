package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrandApiDto {

    @Autowired
    BrandService brandService;

    public void add(BrandForm form) throws ApiException {
        Brand brand = convert(form);
        brandService.add(brand);
    }

    public BrandData get(Integer id) throws ApiException {
        Brand brand = brandService.get(id);
        if (brand == null) {
            throw new ApiException("Brand not found!");
        }
        return ConversionUtil.convertPojoToData(brand);
    }

    public List<BrandData> getAll() {
        return brandService
                .getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public void update(int id, BrandForm form) throws ApiException {
        Brand brand = convert(form);
        brand.setId(id);
        brandService.update(brand);
    }

    private static final String CANT_BE_BLANK = "can't be blank!";

    private static void validate(BrandForm form) throws ApiException {
        if (ValidationUtil.isBlank(form.getName())) {
            throwException("Name " + CANT_BE_BLANK);
        }

        if (ValidationUtil.isBlank(form.getCategory())) {
            throwException("Category " + CANT_BE_BLANK);
        }
    }

    private static void throwException(String message) throws ApiException {
        throw new ApiException(message);
    }

    private static Brand convert(BrandForm form) throws ApiException {
        validate(form);
        Brand brand = ConversionUtil.convertFormToPojo(form);
        normalize(brand);
        return brand;
    }

    private static String normalizeString(String input) {
        return input.trim().toLowerCase();
    }

    private static void normalize(Brand brand) {
        brand.setName(normalizeString(brand.getName()));
        brand.setCategory(normalizeString(brand.getCategory()));
    }

}
