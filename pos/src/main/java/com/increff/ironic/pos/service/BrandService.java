package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.util.NormalizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class BrandService {

    private final BrandDao brandDao;

    @Autowired
    public BrandService(BrandDao brandDao) {
        this.brandDao = brandDao;
    }

    public Brand get(Integer id) throws ApiException {
        Brand brand = brandDao.select(id);
        if (brand == null) {
            throw new ApiException("No brand found for ID: " + id);
        }
        return brand;
    }

    public List<Brand> getAll() {
        return brandDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public Brand add(Brand brand) throws ApiException {
        normalize(brand);
        duplicateCheck(brand);
        return brandDao.insert(brand);
    }

    private void normalize(Brand brand) {
        brand.setCategory(NormalizationUtil.normalize(brand.getCategory()));
        brand.setBrand(NormalizationUtil.normalize(brand.getBrand()));
    }

    @Transactional(rollbackOn = ApiException.class)
    public Brand update(Brand brand) throws ApiException {
        get(brand.getId());
        normalize(brand);
        duplicateCheck(brand);
        return brandDao.update(brand);
    }

    public Brand selectByNameAndCategory(String name, String category) throws ApiException {
        String brandName = NormalizationUtil.normalize(name);
        String brandCategory = NormalizationUtil.normalize(category);
        Brand brand = brandDao.selectByBrandAndCategory(brandName, brandCategory);
        if (brand == null) {
            String message = "No brand found for name " + name + " and category " + category;
            throw new ApiException(message);
        }
        return brand;
    }

    public void duplicateCheck(Brand brand) throws ApiException {
        boolean isDuplicate = isPresent(brand);
        if (isDuplicate) {
            String message = "Brand with name " + brand.getBrand() + " and category " + brand.getCategory() + " already exists!";
            throw new ApiException(message);
        }
    }

    private boolean isPresent(Brand brand) {
        Brand result = brandDao.selectByBrandAndCategory(brand.getBrand(), brand.getCategory());
        return result != null;
    }

}
