package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.util.NormalizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // TODO: 24/01/23 normalise
    @Transactional(rollbackOn = ApiException.class)
    public Brand add(Brand brand) throws ApiException {
        normalize(brand);
        duplicateCheck(brand);
        return brandDao.insert(brand);
    }

    private void normalize(Brand brand) {
        brand.setCategory(NormalizationUtil.normalize(brand.getCategory()));
        brand.setName(NormalizationUtil.normalize(brand.getName()));
    }

    @Transactional(rollbackOn = ApiException.class)
    public Brand update(Brand brand) throws ApiException {
        get(brand.getId());
        normalize(brand);
        return brandDao.update(brand);
    }

    public Brand selectByNameAndCategory(String name, String category) throws ApiException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("category", category);

        return brandDao
                .selectWhereEquals(map)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    String message = "No brand found for name " + name + " and category " + category;
                    return new ApiException(message);
                });
    }

    // TODO: 24/01/23 Brand -> Brand category combination
    public void duplicateCheck(Brand brand) throws ApiException {
        boolean isDuplicate = isPresent(brand);
        if (isDuplicate) {
            throw new ApiException("Brand already exists!");
        }
    }

    private boolean isPresent(Brand brand) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", brand.getName());
        map.put("category", brand.getCategory());
        List<Brand> matches = brandDao.selectWhereEquals(map);
        return !matches.isEmpty();
    }

}
