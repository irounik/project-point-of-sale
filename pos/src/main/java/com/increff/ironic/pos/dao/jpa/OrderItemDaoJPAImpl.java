package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.pojo.OrderItem;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemDaoJPAImpl extends AbstractJPADao<OrderItem, Integer> implements OrderItemDao {

    @Override
    protected Class<OrderItem> getEntityClass() {
        return OrderItem.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}
