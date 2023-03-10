package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.pojo.ProductPojo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ProductDaoJPAImpl extends AbstractJPADao<ProductPojo, Integer> implements ProductDao {

    @Override
    public ProductPojo getByBarcode(String barcode) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("barcode", barcode);
        return selectWhereEquals(conditions)
                .stream()
                .findFirst()
                .orElse(null);
    }

}
