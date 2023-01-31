package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.OrderItem;

import java.util.List;

public interface OrderItemDao extends CrudDao<OrderItem, Integer> {

    List<OrderItem> selectByOrderId(Integer orderId);

}
