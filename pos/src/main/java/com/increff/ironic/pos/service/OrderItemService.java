package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.util.NormalizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderItemService {

    private final OrderItemDao orderItemDao;

    @Autowired
    public OrderItemService(OrderItemDao orderItemDao) {
        this.orderItemDao = orderItemDao;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void create(OrderItem orderItem) throws ApiException {
        Integer id = orderItem.getId();
        if (id != null && orderItemDao.select(id) != null) {
            throw new ApiException("OrderItem with ID: " + id + " already exists!");
        }

        normalizeOrderItem(orderItem);
        orderItemDao.insert(orderItem);
    }

    private void normalizeOrderItem(OrderItem orderItem) {
        double normalizedPrice = NormalizationUtil.normalize(orderItem.getSellingPrice());
        orderItem.setSellingPrice(normalizedPrice);
    }

    @Transactional
    public void update(OrderItem orderItem) throws ApiException {
        getCheck(orderItem.getId()); // Check existence
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

    public List<OrderItem> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItem> getByOrderId(Integer orderId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("orderId", orderId);
        return orderItemDao.selectWhereEquals(condition);
    }

    public void createItems(List<OrderItem> orderItemList) {
        orderItemList.forEach(orderItemDao::insert);
    }

}
