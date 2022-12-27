package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Order;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
        order.setTime(Date.from(Instant.now()));
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

    private void validateOrderForm(List<OrderItemForm> orderFormItems) throws ApiException {
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
            if (inventoryItem.getQuantity() < orderFromItem.getQuantity()) {
                throw new ApiException("Insufficient inventory for Product: " + product.getBarcode());
            }
        }
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

}
