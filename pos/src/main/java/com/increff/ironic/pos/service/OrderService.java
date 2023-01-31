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

    @Transactional
    public Order add(Order order) throws ApiException {
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
