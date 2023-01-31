package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Order;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.increff.ironic.pos.testutils.MockUtils.getNewOrder;
import static org.junit.Assert.assertEquals;

public class OrderServiceTest extends AbstractUnitTest {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    @Test
    public void createOrderThatDoesNotExists() throws ApiException {
        Order order = getNewOrder();
        LocalDateTime currentTime = order.getTime();
        orderService.add(order);

        Order createdOrder = orderDao.select(order.getId());
        assertEquals(currentTime, createdOrder.getTime());
    }

    @Test
    public void updateOrder() throws ApiException {
        // Insert
        Order order = getNewOrder();
        orderDao.insert(order);

        // Update
        LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC).plusHours(15);
        order.setTime(time);
        orderService.updateOrder(order);

        // Check
        Order actualOrder = orderDao.select(order.getId());

        assertEquals(time, actualOrder.getTime());
        assertEquals(order.getId(), actualOrder.getId());
    }

    @Test(expected = ApiException.class)
    public void updateOrderWithInvalidIdThrowsApiException() throws ApiException {
        // insert
        Order order = getNewOrder();
        orderDao.insert(order);

        LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC).plusHours(15);
        order.setTime(time);
        order.setId(699);
        orderService.updateOrder(order);
    }

    @Test
    public void getAll() {
        List<Order> actualList = orderService.getAll();
        List<Order> expectedList = orderDao.selectAll();

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            Order expected = expectedList.get(i);
            Order actual = actualList.get(i);
            assertEquals(expected, actual);
        }
    }

    @Test(expected = ApiException.class)
    public void getForInvalidIdThrowsApiException() throws ApiException {
        orderService.get(-1);
    }

    @Test
    public void getOrderForValidIdReturnsOrderPojo() throws ApiException {
        Order order = getNewOrder();
        orderDao.insert(order);

        Order expected = orderDao.select(order.getId());
        Order actual = orderService.get(order.getId());

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTime(), actual.getTime());
    }

}