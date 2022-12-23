package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

@Service
public class BrandService {

    private final BrandDao brandDao;

    @Autowired
    public BrandService(BrandDao brandDao) {
        this.brandDao = brandDao;
    }

    public Brand get(Integer id) throws ApiException {
        return brandDao.select(id);
    }

    public List<Brand> getAll() {
        return brandDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(Brand brand) throws ApiException {
        duplicateCheck(brand);
        brandDao.insert(brand);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void delete(Integer id) throws ApiException {
        brandDao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Integer id, Brand brand) throws ApiException {
        duplicateCheck(brand);
        brandDao.update(id, brand);
    }

    public void duplicateCheck(Brand brand) throws ApiException {
        boolean isDuplicate = isDuplicate(brand);
        if (isDuplicate) {
            throw new ApiException("Brand already exists!");
        }
    }

    private boolean isDuplicate(Brand brand) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", brand.getName());
        map.put("category", brand.getCategory());
        List<Brand> matches = brandDao.selectWhereEquals(map);
        return !matches.isEmpty();
    }

}
