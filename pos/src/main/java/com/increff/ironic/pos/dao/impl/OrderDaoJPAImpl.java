package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.pojo.OrderPojo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderDaoJPAImpl extends AbstractJPADao<OrderPojo, Integer> implements OrderDao {

    @Override
    public List<OrderPojo> getOrderDuring(LocalDateTime startTime, LocalDateTime endTime) {
        return selectWhereBetween("time", startTime, endTime);
    }

}