package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.Brand;

public interface BrandDao extends CrudDao<Brand, Integer> {

    Brand selectByBrandAndCategory(String brand, String category);

}
