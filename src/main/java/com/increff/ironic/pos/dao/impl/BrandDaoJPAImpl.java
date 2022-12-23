package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Brand;
import org.springframework.stereotype.Repository;

@Repository
public class BrandDaoJPAImpl extends AbstractJPADao<Brand, Integer> implements BrandDao {

    @Override
    protected Class<Brand> getEntityClass() {
        return Brand.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}