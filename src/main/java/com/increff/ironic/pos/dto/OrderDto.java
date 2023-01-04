package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemData;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Order;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDto {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Autowired
    public OrderDto(
            OrderService orderService,
            OrderItemService orderItemService,
            ProductService productService,
            InventoryService inventoryService
    ) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    /**
     * Steps:
     * 1. Fetch products from barcode submitted in {@link OrderItemForm}
     * 2. Fetch {@link Inventory} for each product
     * 3. Check if there are enough items in inventory
     * 4. Update inventory, to reserve items for the current order
     * 5. Create new {@link Order}
     * 6. Create a list of {@link OrderItem} by using form data, product & order ID
     * 7. Add each {@link OrderItem} to the database
     *
     * @param orderFormItems list of order items from the user
     */
    @Transactional(rollbackOn = ApiException.class)
    public void createOrder(List<OrderItemForm> orderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(orderFormItems);

        // Fetching products from barcode
        List<Product> products = fetchProducts(orderFormItems);

        // Fetching product wise inventory
        List<Inventory> inventoryList = getInventoryFromProducts(products);

        // Checking if there are sufficient items in inventory
        validateInventory(inventoryList, products, orderFormItems);

        // Updating the inventory
        reduceInventory(orderFormItems, products);

        // Creating new order
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        order = orderService.create(order); // After generating ID

        // Creating order items POJO from products, order id, and form data
        List<OrderItem> orderItems = getOrderItems(
                order.getId(),
                orderFormItems,
                products
        );

        // Adding Order Items to database
        orderItems.forEach(orderItemService::create);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(updatedOrderFormItems);

        // Rollback previous order
        rollbackPreviousOrder(orderId);

        // Fetching products from barcode
        List<Product> products = fetchProducts(updatedOrderFormItems);

        // Fetching product wise inventory
        List<Inventory> inventoryList = getInventoryFromProducts(products);

        // Checking if there are sufficient items in inventory
        validateInventory(inventoryList, products, updatedOrderFormItems);

        // Updating the inventory
        reduceInventory(updatedOrderFormItems, products);

        // Creating new order
        Order order = orderService.get(orderId);
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));

        // Creating order items POJO from products, order id, and form data
        List<OrderItem> orderItems = getOrderItems(orderId, updatedOrderFormItems, products);

        // Adding Order Items to database
        orderItems.forEach(orderItemService::create);
    }

    private void rollbackPreviousOrder(Integer orderId) throws ApiException {
        // Rollback previous order items
        List<OrderItem> previousOrderItems = orderItemService.getByOrderId(orderId);

        List<Product> products = getProducts(previousOrderItems);
        List<Inventory> inventoryList = getInventoryFromProducts(products);

        Iterator<OrderItem> orderIterator = previousOrderItems.iterator();
        Iterator<Inventory> inventoryIterator = inventoryList.iterator();

        while (orderIterator.hasNext()) {
            OrderItem item = orderIterator.next();
            Inventory inventory = inventoryIterator.next();
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryService.update(inventory);
            orderItemService.deleteById(item.getId());
        }
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

    private void reduceInventory(List<OrderItemForm> orderFormItems, List<Product> products) throws ApiException {

        List<Inventory> inventoryList = getInventoryFromProducts(products);

        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventory = inventoryList.get(i);
            OrderItemForm form = orderFormItems.get(i);

            Integer newQuantity = inventory.getQuantity() - form.getQuantity();
            inventory.setQuantity(newQuantity);

            inventoryService.update(inventory);
        }

    }

    private void validateInventory(
            List<Inventory> inventory,
            List<Product> products,
            List<OrderItemForm> orderFormItems
    ) throws ApiException {
        for (int i = 0; i < inventory.size(); i++) {
            OrderItemForm orderFromItem = orderFormItems.get(i);
            Inventory inventoryItem = inventory.get(i);
            Product product = products.get(i);
            Integer required = orderFromItem.getQuantity();
            Integer inStock = inventoryItem.getQuantity();

            if (required > inStock) {
                throw new ApiException(insufficientStock(product.getName(), product.getBarcode(), inStock));
            }
        }
    }

    private String insufficientStock(String name, String barcode, Integer inStock) {
        return "Insufficient inventory for " + "[" + barcode + "] \"" + name + "\", only " + inStock + " units are left!";
    }

    private List<Inventory> getInventoryFromProducts(List<Product> products) throws ApiException {
        List<Inventory> inventory = new LinkedList<>();

        for (Product product : products) {
            Inventory item = inventoryService.get(product.getId());
            inventory.add(item);
        }

        return inventory;
    }

    private List<OrderItem> getOrderItems(
            Integer orderId,
            List<OrderItemForm> orderFormItems,
            List<Product> products
    ) {
        List<OrderItem> orderItems = new LinkedList<>();

        for (int i = 0; i < orderFormItems.size(); i++) {
            OrderItem orderItem = new OrderItem();
            OrderItemForm orderFromItem = orderFormItems.get(i);
            Product product = products.get(i);

            orderItem.setOrderId(orderId);
            orderItem.setQuantity(orderFromItem.getQuantity());
            orderItem.setProductId(product.getId());
            orderItem.setSellingPrice(product.getPrice());

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private List<Product> fetchProducts(List<OrderItemForm> orderItems) throws ApiException {
        List<Product> products = new LinkedList<>();

        for (OrderItemForm orderItem : orderItems) {
            Product product = convertToProduct(orderItem);
            products.add(product);
        }

        return products;
    }

    private Product convertToProduct(OrderItemForm orderFormItem) throws ApiException {
        return productService.getByBarcode(orderFormItem.getBarcode());
    }

    public List<OrderData> getAll() {
        return orderService.getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    private List<Product> getProducts(List<OrderItem> orderItems) throws ApiException {
        List<Integer> ids = orderItems
                .stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        return productService.getProductsByIds(ids);
    }

    public OrderDetailsData getOrderDetails(Integer orderId) throws ApiException {
        OrderDetailsData data = new OrderDetailsData();

        // Setting order details
        Order order = orderService.get(orderId);
        data.setOrderId(order.getId());
        data.setTime(order.getTime());

        // Setting order item details
        List<OrderItem> orderItems = orderItemService.getByOrderId(orderId);
        List<Product> products = getProducts(orderItems);

        List<OrderItemData> detailsList = getDetailsList(products, orderItems);
        data.setItems(detailsList);

        return data;
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
