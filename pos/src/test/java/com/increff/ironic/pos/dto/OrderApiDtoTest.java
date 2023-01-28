package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Order;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import com.increff.ironic.pos.util.ConversionUtil;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderApiDtoTest extends AbstractUnitTest {

    @Autowired
    private OrderApiDto orderApiDto;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderItemService orderItemService;

    private List<Product> productList;
    private List<Pair<Order, List<OrderItem>>> orderList;

    @Before
    public void setUp() throws ApiException {
        List<Brand> brandList = MockUtils.setUpBrands(brandService);
        productList = MockUtils.setupProducts(brandList, productService);
        List<Integer> productIds = productList.stream().map(Product::getId).collect(Collectors.toList());
        MockUtils.setUpInventory(productIds, inventoryService);
        orderList = MockUtils.setUpMockOrders(orderService, orderItemService, inventoryService, productList);
    }

    @Test
    public void createOrder() throws ApiException {
        List<OrderItemForm> orderItemForms = Collections.singletonList(MockUtils.getMockOrderItemForm());
        Order expectedOrder = orderApiDto.createOrder(orderItemForms);

        Integer orderId = expectedOrder.getId();
        Order actualOrder = orderService.get(orderId);
        AssertUtils.assertEqualOrder(expectedOrder, actualOrder);

        List<OrderItem> actualOrderItems = orderItemService.getByOrderId(orderId);
        List<OrderItem> expectedOrderItems = orderItemForms
                .stream()
                .map(formItem -> convert(orderId, formItem))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expectedOrderItems, actualOrderItems, AssertUtils::assertEqualOrderItems);
    }

    private OrderItem convert(Integer orderId, OrderItemForm formItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setQuantity(formItem.getQuantity());
        orderItem.setSellingPrice(formItem.getSellingPrice());
        try {
            Product product = productService.getByBarcode(formItem.getBarcode());
            orderItem.setProductId(product.getId());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return orderItem;
    }

    @Test
    public void updateOrder() throws ApiException {
        Order order = orderService.getAll().get(0);
        List<OrderItemForm> orderItemForms = Arrays.asList(
                new OrderItemForm(productList.get(0).getBarcode(), 1, 1000.0),
                new OrderItemForm(productList.get(1).getBarcode(), 1, 2500.0)
        );

        Integer orderId = order.getId();
        Order updatedOrder = orderApiDto.updateOrder(orderId, orderItemForms);
        Order actualOrder = orderService.get(order.getId());

        AssertUtils.assertEqualOrder(updatedOrder, actualOrder);

        List<OrderItem> actualItems = orderItemService.getByOrderId(order.getId());
        List<OrderItem> expectedItems = Arrays.asList(
                new OrderItem(null, orderId, productList.get(0).getId(), 1, 1000.0),
                new OrderItem(null, orderId, productList.get(1).getId(), 1, 2500.0)
        );

        AssertUtils.assertEqualList(expectedItems, actualItems, AssertUtils::assertEqualOrderItems);
    }

    @Test
    public void getAll() {
        List<OrderData> actual = orderApiDto.getAll();
        List<OrderData> expected = orderList.stream()
                .map(Pair::getKey)
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualOrderData);
    }

    @Test
    public void testGetOrderById() throws ApiException {
        Order expected = orderList.get(0).getKey();
        Order actual = orderApiDto.getOrder(expected.getId());
        AssertUtils.assertEqualOrder(expected, actual);
    }

    @Test
    public void getOrderDetails() throws ApiException {
        Order order = orderList.get(0).getKey();
        List<OrderItem> orderItems = orderList.get(0).getValue();

        OrderDetailsData orderDetails = orderApiDto.getOrderDetails(order.getId());

        Assert.assertEquals(order.getId(), orderDetails.getOrderId());
        Assert.assertEquals(order.getTime(), orderDetails.getTime());

        List<OrderItemData> expectedOrderItems = getOrderItemData(orderItems);
        List<OrderItemData> actualOrderItems = orderDetails.getItems();
        AssertUtils.assertEqualList(expectedOrderItems, actualOrderItems, AssertUtils::assertEqualOrderItemData);
    }

    private List<OrderItemData> getOrderItemData(List<OrderItem> orderItems) {
        return orderItems.stream().map(orderItem -> {
            Product product = productList
                    .stream()
                    .filter(it -> it.getId().equals(orderItem.getProductId()))
                    .findFirst()
                    .orElse(null);
            assert product != null;
            return ConversionUtil.convertPojoToData(orderItem, product);
        }).collect(Collectors.toList());
    }
}