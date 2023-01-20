package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<Order> getOrderDuring(LocalDateTime startTime, LocalDateTime endTime) {
        return selectWhereBetween("time", startTime, endTime);
    }

}