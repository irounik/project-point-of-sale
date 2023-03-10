package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.pojo.OrderItemPojo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderItemDaoJPAImpl extends AbstractJPADao<OrderItemPojo, Integer> implements OrderItemDao {

    @Override
    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("orderId", orderId);
        return selectWhereEquals(condition);
    }

}
