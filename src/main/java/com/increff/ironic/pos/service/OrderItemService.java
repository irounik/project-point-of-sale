package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Transactional(rollbackOn = ApiException.class)
    public void create(OrderItem orderItem) {
        orderItemDao.insert(orderItem);
    }

    @Transactional
    public void update(OrderItem orderItem) {
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

    public void delete(Integer id) {
        orderItemDao.delete(id);
    }

}
