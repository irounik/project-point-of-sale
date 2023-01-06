package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Order;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.OrderItemService;
import com.increff.ironic.pos.service.OrderService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDto {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    @Autowired
    public OrderDto(OrderService orderService, OrderItemService orderItemService, ProductService productService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void createOrder(List<OrderItemForm> orderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(orderFormItems);

        // Creating new order
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        order = orderService.create(order); // After generating ID

        List<OrderItem> orderItems = getOrderItems(order.getId(), orderFormItems);

        // Creating order items
        orderItemService.createItems(orderItems);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(updatedOrderFormItems);

        // Updating order items
        List<OrderItem> orderItems = getOrderItems(orderId, updatedOrderFormItems);
        orderItemService.updateItems(orderId, orderItems);

        // Updating order time
        Order order = orderService.get(orderId);
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
    }

    public List<OrderData> getAll() {
        return orderService.getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public OrderDetailsData getOrderDetails(Integer orderId) throws ApiException {
        OrderDetailsData data = new OrderDetailsData();

        // Setting order details
        Order order = orderService.get(orderId);
        data.setOrderId(order.getId());
        data.setTime(order.getTime());

        // Setting order item details
        List<OrderItem> orderItems = orderItemService.getByOrderId(orderId);
        List<Product> products = orderItemService.getProducts(orderItems);

        List<OrderItemData> detailsList = getDetailsList(products, orderItems);
        data.setItems(detailsList);

        return data;
    }

    private void validateOrderForm(List<OrderItemForm> orderFormItems) throws ApiException {
        if (orderFormItems.isEmpty()) {
            throw new ApiException("Please add at lease one item for creating order!");
        }

        for (OrderItemForm form : orderFormItems) {
            if (!ValidationUtil.isPositiveNumber(form.getQuantity())) {
                throw new ApiException("Invalid input: 'quantity' should be a positive number!");
            }

            if (ValidationUtil.isBlank(form.getBarcode())) {
                throw new ApiException("Barcode can't be blank!");
            }
        }
    }

    private List<OrderItem> getOrderItems(Integer orderId, List<OrderItemForm> orderFormItems) throws ApiException {
        List<OrderItem> orderItems = new LinkedList<>();

        for (OrderItemForm form : orderFormItems) {
            Product product = productService.getByBarcode(form.getBarcode());
            OrderItem item = ConversionUtil.convertPojoToData(orderId, form, product);
            orderItems.add(item);
        }

        return orderItems;
    }

    private List<OrderItemData> getDetailsList(List<Product> products, List<OrderItem> orderItems) {
        List<OrderItemData> dataList = new LinkedList<>();

        for (int i = 0; i < products.size(); i++) {
            OrderItemData data = ConversionUtil.convertPojoToData(orderItems.get(i), products.get(i));
            dataList.add(data);
        }

        return dataList;
    }

}
