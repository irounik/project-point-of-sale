package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.pojo.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderItemDaoJPAImpl extends AbstractJPADao<OrderItem, Integer> implements OrderItemDao {

    @Override
    protected Class<OrderItem> getEntityClass() {
        return OrderItem.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public List<OrderItem> selectByOrderId(Integer orderId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("orderId", orderId);
        return selectWhereEquals(condition);
    }

}
