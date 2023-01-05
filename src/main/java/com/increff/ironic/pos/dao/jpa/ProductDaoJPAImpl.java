package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.pojo.Product;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ProductDaoJPAImpl extends AbstractJPADao<Product, Integer> implements ProductDao {

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public Product getByBarcode(String barcode) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("barcode", barcode);
        return selectWhereEquals(conditions)
                .stream()
                .findFirst()
                .orElse(null);
    }

}
