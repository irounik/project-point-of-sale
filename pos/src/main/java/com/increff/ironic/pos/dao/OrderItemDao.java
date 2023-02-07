package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.OrderItemPojo;

import java.util.List;

public interface OrderItemDao extends CrudDao<OrderItemPojo, Integer> {

    List<OrderItemPojo> selectByOrderId(Integer orderId);

}
