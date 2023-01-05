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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        // Checking if there are sufficient items in inventory
        List<Integer> requiredQuantities = getQuantities(orderFormItems);

        validateInventory(products, requiredQuantities);

        // Updating the inventory
        updateInventory(requiredQuantities, products);

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
        for (OrderItem item : orderItems) {
            orderItemService.create(item);
        }

    }

    private List<Integer> getQuantities(List<OrderItemForm> orderFormItems) {
        return orderFormItems
                .stream()
                .map(OrderItemForm::getQuantity)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackOn = ApiException.class)
    public void updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(updatedOrderFormItems);

        // Fetching products from barcode
        List<Product> products = fetchProducts(updatedOrderFormItems);

        // Handle inventory
        List<OrderItem> newOrderItems = getOrderItems(orderId, updatedOrderFormItems, products);

        List<OrderItem> previousOrderItems = orderItemService.getByOrderId(orderId);

        OrderItemChanges changes = new OrderItemChanges(previousOrderItems, newOrderItems);

        List<OrderItem> createOrUpdateItems = changes.getAllChanges();
        List<Product> productsToCreateOrUpdate = getProducts(createOrUpdateItems);
        List<Integer> requiredQuantities = changes.getRequiredQuantities();

        validateInventory(productsToCreateOrUpdate, requiredQuantities);

        // Updating the inventory
        updateInventory(requiredQuantities, productsToCreateOrUpdate);

        // Updating order time
        Order order = orderService.get(orderId);
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));

        // Update Order Items
        for (OrderItem toUpdate : changes.getToUpdate()) {
            orderItemService.update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItem toDelete : changes.getToDelete()) {
            orderItemService.deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItem toCreate : changes.getToCreate()) {
            orderItemService.create(toCreate);
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

    private void updateInventory(List<Integer> quantities, List<Product> products) throws ApiException {

        List<Inventory> inventoryList = getInventoryFromProducts(products);

        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventory = inventoryList.get(i);
            Integer newQuantity = inventory.getQuantity() - quantities.get(i);
            inventory.setQuantity(newQuantity);

            inventoryService.update(inventory);
        }

    }

    private void validateInventory(
            List<Product> products,
            List<Integer> requiredQuantities
    ) throws ApiException {
        // Fetching product wise inventory
        List<Inventory> inventoryList = getInventoryFromProducts(products);

        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventoryItem = inventoryList.get(i);
            Product product = products.get(i);
            Integer required = requiredQuantities.get(i);
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

    private static class OrderItemChanges {
        public List<OrderItem> getToUpdate() {
            return toUpdate;
        }

        public List<OrderItem> getToDelete() {
            return toDelete;
        }

        public List<OrderItem> getToCreate() {
            return toCreate;
        }

        private final List<OrderItem> toUpdate, toDelete, toCreate;
        private final List<OrderItem> oldOrderItems, newOrderItems;

        private Map<Integer, OrderItem> getItemMap(List<OrderItem> items) {
            Map<Integer, OrderItem> itemMap = new HashMap<>();
            for (OrderItem item : items) {
                itemMap.put(item.getProductId(), item);
            }
            return itemMap;
        }

        public OrderItemChanges(List<OrderItem> oldOrderItems, List<OrderItem> newOrderItems) {
            toUpdate = new LinkedList<>();
            toDelete = new LinkedList<>();
            toCreate = new LinkedList<>();
            this.oldOrderItems = oldOrderItems;
            this.newOrderItems = newOrderItems;
            computeChanges();
        }

        private void computeChanges() {
            Map<Integer, OrderItem> oldItemMap = getItemMap(this.oldOrderItems);

            for (OrderItem newItem : this.newOrderItems) {
                int productId = newItem.getProductId();

                if (oldItemMap.containsKey(productId)) {
                    int oldItemId = oldItemMap.get(productId).getId();
                    newItem.setId(oldItemId);
                    toUpdate.add(newItem);
                    oldItemMap.remove(productId);
                } else {
                    toCreate.add(newItem);
                }
            }

            toDelete.addAll(oldItemMap.values());
        }

        private List<Integer> getQuality(List<OrderItem> items) {
            return items.stream()
                    .map(OrderItem::getQuantity)
                    .collect(Collectors.toList());
        }

        public List<Integer> getRequiredQuantities() {
            Map<Integer, OrderItem> oldItemMap = getItemMap(this.oldOrderItems);

            List<Integer> quantities = getQuality(toCreate);

            for (OrderItem newItem : toUpdate) {
                OrderItem oldItem = oldItemMap.get(newItem.getProductId());
                int required = newItem.getQuantity() - oldItem.getQuantity();
                quantities.add(required);
            }

            List<Integer> deleteQuantities = getQuality(toDelete)
                    .stream()
                    .map(it -> -1 * it)
                    .collect(Collectors.toList());

            quantities.addAll(deleteQuantities);
            return quantities;
        }

        public List<OrderItem> getAllChanges() {
            List<OrderItem> items = new LinkedList<>();
            items.addAll(toCreate);
            items.addAll(toUpdate);
            items.addAll(toDelete);
            return items;
        }
    }

}
