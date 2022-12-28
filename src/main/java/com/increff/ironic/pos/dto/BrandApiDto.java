package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.NormalizationUtil;
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
        Brand brand = preprocess(form);
        brandService.add(brand);
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

    public void update(int id, BrandForm form) throws ApiException {
        Brand brand = preprocess(form);
        brand.setId(id);
        brandService.update(brand);
    }

    private static Brand preprocess(BrandForm form) throws ApiException {
        validate(form);
        Brand brand = ConversionUtil.convertFormToPojo(form);
        normalize(brand);
        return brand;
    }

    private static void validate(BrandForm form) throws ApiException {
        if (ValidationUtil.isBlank(form.getName())) {
            throw new ApiException("Name can't be blank");
        }

        if (ValidationUtil.isBlank(form.getCategory())) {
            throw new ApiException("Category can't be blank");
        }
    }

    private static void normalize(Brand brand) {
        String normalizedName = NormalizationUtil.normalize(brand.getName());
        brand.setName(normalizedName);

        String normalizedCategory = NormalizationUtil.normalize(brand.getCategory());
        brand.setCategory(normalizedCategory);
    }

}
