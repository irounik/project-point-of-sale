package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.BrandPojo;
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

    // Return data
    public BrandData add(BrandForm form) throws ApiException {
        BrandPojo brandPojo = preprocess(form);
        brandService.add(brandPojo);
        return ConversionUtil.convertPojoToData(brandPojo);
    }

    public BrandData get(Integer id) throws ApiException {
        BrandPojo brandPojo = brandService.get(id);
        return ConversionUtil.convertPojoToData(brandPojo);
    }

    public List<BrandData> getAll() {
        return brandService
                .getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public BrandData update(int id, BrandForm form) throws ApiException {
        BrandPojo brandPojo = preprocess(form);
        brandPojo.setId(id);
        BrandPojo updatedBrandPojo = brandService.update(brandPojo);
        return ConversionUtil.convertPojoToData(updatedBrandPojo);
    }

    private static BrandPojo preprocess(BrandForm form) throws ApiException {
        ValidationUtil.validate(form);
        return ConversionUtil.convertFormToPojo(form);
    }

}
