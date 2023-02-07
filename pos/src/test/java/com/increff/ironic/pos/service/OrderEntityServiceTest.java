package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.OrderPojo;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.increff.ironic.pos.testutils.MockUtils.getNewOrder;
import static org.junit.Assert.assertEquals;

public class OrderEntityServiceTest extends AbstractUnitTest {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    @Test
    public void createOrderThatDoesNotExists() throws ApiException {
        OrderPojo orderPojo = getNewOrder();
        LocalDateTime currentTime = orderPojo.getTime();
        orderService.add(orderPojo);

        OrderPojo createdOrderPojo = orderDao.select(orderPojo.getId());
        assertEquals(currentTime, createdOrderPojo.getTime());
    }

    @Test
    public void updateOrder() throws ApiException {
        // Insert
        OrderPojo orderPojo = getNewOrder();
        orderDao.insert(orderPojo);

        // Update
        LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC).plusHours(15);
        orderPojo.setTime(time);
        orderService.updateOrder(orderPojo);

        // Check
        OrderPojo actualOrderPojo = orderDao.select(orderPojo.getId());

        assertEquals(time, actualOrderPojo.getTime());
        assertEquals(orderPojo.getId(), actualOrderPojo.getId());
    }

    @Test(expected = ApiException.class)
    public void updateOrderWithInvalidIdThrowsApiException() throws ApiException {
        // insert
        OrderPojo orderPojo = getNewOrder();
        orderDao.insert(orderPojo);

        LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC).plusHours(15);
        orderPojo.setTime(time);
        orderPojo.setId(699);
        orderService.updateOrder(orderPojo);
    }

    @Test
    public void getAll() {
        List<OrderPojo> actualList = orderService.getAll();
        List<OrderPojo> expectedList = orderDao.selectAll();

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            OrderPojo expected = expectedList.get(i);
            OrderPojo actual = actualList.get(i);
            assertEquals(expected, actual);
        }
    }

    @Test(expected = ApiException.class)
    public void getForInvalidIdThrowsApiException() throws ApiException {
        orderService.get(-1);
    }

    @Test
    public void getOrderForValidIdReturnsOrderPojo() throws ApiException {
        OrderPojo orderPojo = getNewOrder();
        orderDao.insert(orderPojo);

        OrderPojo expected = orderDao.select(orderPojo.getId());
        OrderPojo actual = orderService.get(orderPojo.getId());

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTime(), actual.getTime());
    }

}