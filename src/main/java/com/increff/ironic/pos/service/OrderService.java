package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class OrderService {

    private final OrderDao orderDao;

    @Autowired
    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // Create order
    @Transactional
    public Order create(Order order) throws ApiException {
        if (order.getId() != null && orderDao.select(order.getId()) != null) {
            throw new ApiException("Order with ID: " + order.getId() + " already exists!");
        }
        return orderDao.insert(order);
    }

    // Get all orders
    @Transactional
    public void updateOrder(Order order) throws ApiException {
        get(order.getId()); // Check if order exists
        orderDao.update(order);
    }

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
