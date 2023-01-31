package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.increff.ironic.pos.util.NormalizationUtil.normalizeOrderItem;

@Service
public class OrderItemService {

    private final OrderItemDao orderItemDao;

    @Autowired
    public OrderItemService(OrderItemDao orderItemDao) {
        this.orderItemDao = orderItemDao;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(OrderItem orderItem) throws ApiException {
        Integer id = orderItem.getId();
        if (id != null && orderItemDao.select(id) != null) {
            throw new ApiException("OrderItem with ID: " + id + " already exists!");
        }

        normalizeOrderItem(orderItem);
        orderItemDao.insert(orderItem);
    }

    @Transactional
    public void update(OrderItem orderItem) throws ApiException {
        getCheck(orderItem.getId()); // Check existence
        normalizeOrderItem(orderItem);
        orderItemDao.update(orderItem);
    }

    @Transactional
    public void deleteById(Integer id) {
        orderItemDao.delete(id);
    }

    public void getCheck(Integer id) throws ApiException {
        OrderItem item = orderItemDao.select(id);
        if (item == null) {
            throw new ApiException("No order item found for ID: " + id);
        }
    }

    public List<OrderItem> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public void addItems(List<OrderItem> orderItemList) {
        orderItemList.forEach(orderItemDao::insert);
    }

}
