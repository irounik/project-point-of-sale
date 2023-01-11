package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDao extends CrudDao<Order, Integer> {

    List<Order> getOrderDuring(LocalDateTime startDate, LocalDateTime endDate);

}
