package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.OrderPojo;
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
    public OrderPojo add(OrderPojo orderPojo) throws ApiException {
        return orderDao.insert(orderPojo);
    }

    // Get all orders
    @Transactional
    public void updateOrder(OrderPojo orderPojo) throws ApiException {
        get(orderPojo.getId()); // Check if order exists
        orderDao.update(orderPojo);
    }

    public List<OrderPojo> getAll() {
        return orderDao.selectAll();
    }

    public OrderPojo get(Integer orderId) throws ApiException {
        OrderPojo orderPojo = orderDao.select(orderId);
        if (orderPojo == null) {
            throw new ApiException("No order found with ID: " + orderId);
        }
        return orderPojo;
    }

    public List<OrderPojo> getOrderBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderDao.getOrderDuring(startDate, endDate);
    }

}
