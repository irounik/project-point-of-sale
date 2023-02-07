package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.BrandPojo;

public interface BrandDao extends CrudDao<BrandPojo, Integer> {

    BrandPojo selectByBrandAndCategory(String brand, String category);

}
