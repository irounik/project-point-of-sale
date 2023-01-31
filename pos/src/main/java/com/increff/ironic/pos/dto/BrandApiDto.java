package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrandApiDto {

    private final BrandService brandService;

    @Autowired
    public BrandApiDto(BrandService brandService) {
        this.brandService = brandService;
    }

    public Brand add(BrandForm form) throws ApiException {
        Brand brand = preprocess(form);
        return brandService.add(brand);
    }

    public BrandData get(Integer id) throws ApiException {
        Brand brand = brandService.get(id);
        return ConversionUtil.convertPojoToData(brand);
    }

    public List<BrandData> getAll() {
        return brandService
                .getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public BrandData update(int id, BrandForm form) throws ApiException {
        Brand brand = preprocess(form);
        brand.setId(id);
        Brand updatedBrand = brandService.update(brand);
        return ConversionUtil.convertPojoToData(updatedBrand);
    }

    private static Brand preprocess(BrandForm form) throws ApiException {
        ValidationUtil.validate(form);
        return ConversionUtil.convertFormToPojo(form);
    }

}
