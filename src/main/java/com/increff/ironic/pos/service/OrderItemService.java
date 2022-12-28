package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Transactional(rollbackOn = ApiException.class)
    public void create(OrderItem orderItem) {

        orderItemDao.insert(orderItem);
    }

    @Transactional
    public void update(OrderItem orderItem) throws ApiException {
        getById(orderItem.getOrderId()); // Check existence
        orderItemDao.update(orderItem);
    }

    public OrderItem getById(Integer id) throws ApiException {
        OrderItem item = orderItemDao.select(id);
        if (item == null) {
            throw new ApiException("No order found for ID: " + id);
        }
        return item;
    }

    public List<OrderItem> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItem> getByOrderId(Integer orderId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("orderId", orderId);
        return orderItemDao.selectWhereEquals(condition);
    }
}
