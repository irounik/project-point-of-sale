package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.BrandPojo;
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

    public BrandPojo get(Integer id) throws ApiException {
        BrandPojo brandPojo = brandDao.select(id);
        if (brandPojo == null) {
            throw new ApiException("No brand found for ID: " + id);
        }
        return brandPojo;
    }

    public List<BrandPojo> getAll() {
        return brandDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public BrandPojo add(BrandPojo brandPojo) throws ApiException {
        normalize(brandPojo);
        duplicateCheck(brandPojo);
        return brandDao.insert(brandPojo);
    }

    private void normalize(BrandPojo brandPojo) {
        brandPojo.setCategory(NormalizationUtil.normalize(brandPojo.getCategory()));
        brandPojo.setBrand(NormalizationUtil.normalize(brandPojo.getBrand()));
    }

    @Transactional(rollbackOn = ApiException.class)
    public BrandPojo update(BrandPojo brandPojo) throws ApiException {
        get(brandPojo.getId());
        normalize(brandPojo);
        duplicateCheck(brandPojo);
        return brandDao.update(brandPojo);
    }

    public BrandPojo selectByNameAndCategory(String name, String category) throws ApiException {
        String brandName = NormalizationUtil.normalize(name);
        String brandCategory = NormalizationUtil.normalize(category);
        BrandPojo brandPojo = brandDao.selectByBrandAndCategory(brandName, brandCategory);
        if (brandPojo == null) {
            String message = "No brand found for name " + name + " and category " + category;
            throw new ApiException(message);
        }
        return brandPojo;
    }

    public void duplicateCheck(BrandPojo brandPojo) throws ApiException {
        boolean isDuplicate = isPresent(brandPojo);
        if (isDuplicate) {
            String message = "Brand with name " + brandPojo.getBrand() + " and category " + brandPojo.getCategory() + " already exists!";
            throw new ApiException(message);
        }
    }

    private boolean isPresent(BrandPojo brandPojo) {
        BrandPojo result = brandDao.selectByBrandAndCategory(brandPojo.getBrand(), brandPojo.getCategory());
        return result != null;
    }

}
