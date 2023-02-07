package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.pojo.BrandPojo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class BrandDaoJPAImpl extends AbstractJPADao<BrandPojo, Integer> implements BrandDao {

    @Override
    protected Class<BrandPojo> getEntityClass() {
        return BrandPojo.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public BrandPojo selectByBrandAndCategory(String brand, String category) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("brand", brand);
        map.put("category", category);
        return selectWhereEquals(map)
                .stream()
                .findFirst()
                .orElse(null);
    }
}