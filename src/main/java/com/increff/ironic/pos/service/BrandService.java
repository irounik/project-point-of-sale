package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.pojo.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandDao brandDao;

    public Brand get(Integer id) throws ApiException {
        return brandDao.select(id);
    }

    public List<Brand> getAll() {
        return brandDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(Brand brand) throws ApiException {
        brandDao.insert(brand);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void delete(Integer id) throws ApiException {
        brandDao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Integer id, Brand brand) throws ApiException {
        brandDao.update(id, brand);
    }

}
