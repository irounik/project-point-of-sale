package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.ProductPojo;

public interface ProductDao extends CrudDao<ProductPojo, Integer> {

    ProductPojo getByBarcode(String barcode);

}
