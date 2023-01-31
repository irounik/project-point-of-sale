package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.pojo.Brand;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

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

    @Override
    public Brand selectByBrandAndCategory(String brand, String category) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("brand", brand);
        map.put("category", category);
        return selectWhereEquals(map)
                .stream()
                .findFirst()
                .orElse(null);
    }
}