package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.BrandPojo;
import com.increff.ironic.pos.pojo.OrderPojo;
import com.increff.ironic.pos.pojo.OrderItemPojo;
import com.increff.ironic.pos.pojo.ProductPojo;
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

    private List<ProductPojo> productPojoList;
    private List<Pair<OrderPojo, List<OrderItemPojo>>> orderList;

    @Before
    public void setUp() throws ApiException {
        List<BrandPojo> brandPojoList = MockUtils.setUpBrands(brandService);
        productPojoList = MockUtils.setupProducts(brandPojoList, productService);
        List<Integer> productIds = productPojoList.stream().map(ProductPojo::getId).collect(Collectors.toList());
        MockUtils.setUpInventory(productIds, inventoryService);
        orderList = MockUtils.setUpMockOrders(orderService, orderItemService, inventoryService, productPojoList);
    }

    @Test
    public void createOrder() throws ApiException {
        List<OrderItemForm> orderItemForms = Collections.singletonList(MockUtils.getMockOrderItemForm());
        OrderPojo expectedOrderPojo = orderApiDto.createOrder(orderItemForms);

        Integer orderId = expectedOrderPojo.getId();
        OrderPojo actualOrderPojo = orderService.get(orderId);
        AssertUtils.assertEqualOrder(expectedOrderPojo, actualOrderPojo);

        List<OrderItemPojo> actualOrderItemEntities = orderItemService.getByOrderId(orderId);
        List<OrderItemPojo> expectedOrderItemEntities = orderItemForms
                .stream()
                .map(formItem -> convert(orderId, formItem))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expectedOrderItemEntities, actualOrderItemEntities, AssertUtils::assertEqualOrderItems);
    }

    private OrderItemPojo convert(Integer orderId, OrderItemForm formItem) {
        OrderItemPojo orderItemPojo = new OrderItemPojo();
        orderItemPojo.setOrderId(orderId);
        orderItemPojo.setQuantity(formItem.getQuantity());
        orderItemPojo.setSellingPrice(formItem.getSellingPrice());
        try {
            ProductPojo productPojo = productService.getByBarcode(formItem.getBarcode());
            orderItemPojo.setProductId(productPojo.getId());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return orderItemPojo;
    }

    @Test
    public void updateOrder() throws ApiException {
        OrderPojo orderPojo = orderService.getAll().get(0);
        List<OrderItemForm> orderItemForms = Arrays.asList(
                new OrderItemForm(productPojoList.get(0).getBarcode(), 1, 1000.0),
                new OrderItemForm(productPojoList.get(1).getBarcode(), 1, 2500.0)
        );

        Integer orderId = orderPojo.getId();
        OrderPojo updatedOrderPojo = orderApiDto.updateOrder(orderId, orderItemForms);
        OrderPojo actualOrderPojo = orderService.get(orderPojo.getId());

        AssertUtils.assertEqualOrder(updatedOrderPojo, actualOrderPojo);

        List<OrderItemPojo> actualItems = orderItemService.getByOrderId(orderPojo.getId());
        List<OrderItemPojo> expectedItems = Arrays.asList(
                new OrderItemPojo(null, orderId, productPojoList.get(0).getId(), 1000.0, 1),
                new OrderItemPojo(null, orderId, productPojoList.get(1).getId(), 2500.0, 1)
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
        OrderPojo expected = orderList.get(0).getKey();
        OrderPojo actual = orderApiDto.getOrder(expected.getId());
        AssertUtils.assertEqualOrder(expected, actual);
    }

    @Test
    public void getOrderDetails() throws ApiException {
        OrderPojo orderPojo = orderList.get(0).getKey();
        List<OrderItemPojo> orderItemEntities = orderList.get(0).getValue();

        OrderDetailsData orderDetails = orderApiDto.getOrderDetails(orderPojo.getId());

        Assert.assertEquals(orderPojo.getId(), orderDetails.getOrderId());
        Assert.assertEquals(orderPojo.getTime(), orderDetails.getTime());

        List<OrderItemData> expectedOrderItems = getOrderItemData(orderItemEntities);
        List<OrderItemData> actualOrderItems = orderDetails.getItems();
        AssertUtils.assertEqualList(expectedOrderItems, actualOrderItems, AssertUtils::assertEqualOrderItemData);
    }

    private List<OrderItemData> getOrderItemData(List<OrderItemPojo> orderItemEntities) {
        return orderItemEntities.stream().map(orderItem -> {
            ProductPojo productPojo = productPojoList
                    .stream()
                    .filter(it -> it.getId().equals(orderItem.getProductId()))
                    .findFirst()
                    .orElse(null);
            assert productPojo != null;
            return ConversionUtil.convertPojoToData(orderItem, productPojo);
        }).collect(Collectors.toList());
    }
}