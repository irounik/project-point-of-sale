package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.pojo.InventoryPojo;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDaoJPAImpl extends AbstractJPADao<InventoryPojo, Integer> implements InventoryDao {

    @Override
    protected Class<InventoryPojo> getEntityClass() {
        return InventoryPojo.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "product_id";
    }

}
