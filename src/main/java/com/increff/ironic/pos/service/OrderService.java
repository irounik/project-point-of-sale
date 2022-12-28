package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    // Create order
    public Order create(Order order) {
        return orderDao.insert(order);
    }

    // Get all orders
    public List<Order> getAll() {
        return orderDao.selectAll();
    }

    public Order get(Integer orderId) throws ApiException {
        Order order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("No order found with ID: " + orderId);
        }
        return order;
    }

}
