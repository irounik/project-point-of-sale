package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.pojo.InventoryPojo;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryDaoJPAImpl extends AbstractJPADao<InventoryPojo, Integer> implements InventoryDao {

}
