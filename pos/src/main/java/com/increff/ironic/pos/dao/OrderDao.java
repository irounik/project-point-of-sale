package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.OrderPojo;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDao extends CrudDao<OrderPojo, Integer> {

    List<OrderPojo> getOrderDuring(LocalDateTime startDate, LocalDateTime endDate);

}
