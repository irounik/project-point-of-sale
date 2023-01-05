package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.Product;

public interface ProductDao extends CrudDao<Product, Integer> {

    Product getByBarcode(String barcode);

}
