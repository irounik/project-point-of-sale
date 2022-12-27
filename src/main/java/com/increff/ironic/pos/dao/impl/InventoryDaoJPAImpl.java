package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Inventory;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDaoJPAImpl extends AbstractJPADao<Inventory, Integer> implements InventoryDao {

    @Override
    protected Class<Inventory> getEntityClass() {
        return Inventory.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "product_id";
    }

}
