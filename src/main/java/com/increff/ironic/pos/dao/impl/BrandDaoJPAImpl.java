package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.util.SerializationUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BrandDaoJPAImpl extends AbstractJPADao<Brand, Integer> implements BrandDao {

    private void validateExistence(Brand brand) throws ApiException {
        Map<String, Object> map = SerializationUtils.getAttributeMap(brand);
        map.remove(getPrimaryKeyColumnName());
        List<Brand> matches = selectWhereEquals(map);
        if (!matches.isEmpty()) {
            String message = "A brand with same name and category already exists with ID: " + matches.get(0).getId();
            throw new ApiException(message);
        }
    }

    @Override
    public void insert(Brand brand) throws ApiException {
        validateExistence(brand);
        super.insert(brand);
    }

    @Override
    protected String getEntityTableName() {
        return "brand";
    }

    @Override
    protected Class<Brand> getEntityClass() {
        return Brand.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}