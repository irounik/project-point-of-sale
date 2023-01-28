package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderDao orderDao;

    @Autowired
    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // Create order
    // TODO: 27/01/23 27,28(if condn) are required?
    // TODO: 27/01/23 create -> add
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

    public List<Order> getOrderBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderDao.getOrderDuring(startDate, endDate);
    }

}
