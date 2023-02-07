package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderItemChanges;
import com.increff.ironic.pos.pojo.OrderItemPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static com.increff.ironic.pos.util.NormalizationUtil.normalizeOrderItem;

@Service
public class OrderItemService {

    private final OrderItemDao orderItemDao;

    @Autowired
    public OrderItemService(OrderItemDao orderItemDao) {
        this.orderItemDao = orderItemDao;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(OrderItemPojo orderItemPojo) throws ApiException {
        Integer id = orderItemPojo.getId();
        if (id != null && orderItemDao.select(id) != null) {
            throw new ApiException("OrderItem with ID: " + id + " already exists!");
        }

        normalizeOrderItem(orderItemPojo);
        orderItemDao.insert(orderItemPojo);
    }

    @Transactional
    public void update(OrderItemPojo orderItemPojo) throws ApiException {
        getCheck(orderItemPojo.getId()); // Check existence
        normalizeOrderItem(orderItemPojo);
        orderItemDao.update(orderItemPojo);
    }

    @Transactional
    public void deleteById(Integer id) {
        orderItemDao.delete(id);
    }

    public void getCheck(Integer id) throws ApiException {
        OrderItemPojo item = orderItemDao.select(id);
        if (item == null) {
            throw new ApiException("No order item found for ID: " + id);
        }
    }

    public List<OrderItemPojo> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public void addItems(List<OrderItemPojo> orderItemPojoList) {
        orderItemPojoList.forEach(orderItemDao::insert);
    }

    public void updateOrderItems(OrderItemChanges changes) throws ApiException {
        // Update Order Items
        for (OrderItemPojo toUpdate : changes.getItemsToUpdate()) {
            update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItemPojo toDelete : changes.getItemsToDelete()) {
            deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItemPojo toCreate : changes.getItemsToAdd()) {
            add(toCreate);
        }
    }
}
