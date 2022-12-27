package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDaoJPAImpl extends AbstractJPADao<Order, Integer> implements OrderDao {

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}