package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderApiDto {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final InvoiceService invoiceService;

    @Autowired
    public OrderApiDto(
            OrderService orderService,
            OrderItemService orderItemService,
            ProductService productService,
            InvoiceService invoiceService,
            InventoryService inventoryService) {

        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
        this.invoiceService = invoiceService;
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
     * 8. Generating invoice (PDF)
     * 9. Adding path to the invoice in the Order Entity.
     * 10. Return the created invoice entity
     *
     * @param orderFormItems list of order items from the user
     */
    @Transactional(rollbackOn = ApiException.class)
    public Order createOrder(List<OrderItemForm> orderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(orderFormItems);

        // Creating new order
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        orderService.add(order);

        int size = orderFormItems.size();
        List<Product> products = new ArrayList<>(size);
        List<OrderItem> orderItems = new ArrayList<>(size);
        List<Integer> requiredQuantities = new ArrayList<>(size);

        for (OrderItemForm itemForm : orderFormItems) {
            Product product = productService.getByBarcode(itemForm.getBarcode());
            products.add(product);

            // Validating validating selling price
            productService.validateSellingPrice(product, itemForm.getSellingPrice());

            OrderItem orderItem = ConversionUtil.convertFromToPojo(order.getId(), itemForm, product);
            orderItems.add(orderItem);

            requiredQuantities.add(itemForm.getQuantity());
        }

        // Get inventory from products
        updateInventories(products, requiredQuantities);

        // Creating order items
        for (OrderItem item : orderItems) {
            orderItemService.add(item);
        }

        // Generate PDF
        OrderDetailsData orderDetailsData = getOrderDetails(order, products, orderItems);
        String path = invoiceService.generateInvoice(orderDetailsData);

        // Updating invoice path
        order.setInvoicePath(path);
        return order;
    }

    private OrderDetailsData getOrderDetails(Order order, List<Product> products, List<OrderItem> orderItems) {
        OrderDetailsData orderDetailsData = new OrderDetailsData();

        orderDetailsData.setOrderId(order.getId());
        orderDetailsData.setTime(order.getTime());
        List<OrderItemData> itemDetails = getDetailsList(products, orderItems);
        orderDetailsData.setItems(itemDetails);

        return orderDetailsData;
    }

    private void updateInventories(List<Product> products, List<Integer> requiredQuantities) throws ApiException {

        List<Inventory> inventories = getInventoryFromProducts(products);

        // Preparing data for updating inventory
        List<ProductInventoryQuantity> productInventoryQuantities = getInventoryQuantityList(
                products,
                inventories,
                requiredQuantities
        );

        // Validating inventory
        inventoryService.validateSufficientQuantity(productInventoryQuantities);

        // Updating the inventory
        inventoryService.updateInventories(productInventoryQuantities);
    }

    private List<ProductInventoryQuantity> getInventoryQuantityList(
            List<Product> products,
            List<Inventory> inventories,
            List<Integer> requiredQuantities) {

        if (products.size() != inventories.size() && requiredQuantities.size() != inventories.size()) {
            throw new IllegalArgumentException("Size of Products, Inventory and Quantity list should be same!");
        }

        List<ProductInventoryQuantity> combinedList = new LinkedList<>();

        products.sort(Comparator.comparing(Product::getId));
        inventories.sort(Comparator.comparing(Inventory::getProductId));

        Iterator<Product> productItr = products.iterator();
        Iterator<Inventory> inventoryItr = inventories.iterator();
        Iterator<Integer> requiredQuantityItr = requiredQuantities.iterator();

        while (productItr.hasNext()) {
            ProductInventoryQuantity item = new ProductInventoryQuantity();
            Product product = productItr.next();

            item.setProductName(product.getName());
            item.setBarcode(product.getBarcode());
            item.setRequiredQuantity(requiredQuantityItr.next());
            item.setInventory(inventoryItr.next());

            combinedList.add(item);
        }

        return combinedList;
    }

    private List<Inventory> getInventoryFromProducts(List<Product> products) throws ApiException {

        List<Integer> productIds = products
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        return inventoryService.getByIds(productIds);
    }

    @Transactional(rollbackOn = ApiException.class)
    public Order updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(updatedOrderFormItems);

        // Updating order items
        List<OrderItem> orderItems = getOrderItems(orderId, updatedOrderFormItems);
        updateItems(orderId, orderItems);

        // Updating order time
        Order order = orderService.get(orderId);
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));

        // Updating invoice
        OrderDetailsData orderDetailsData = getOrderDetails(orderId);
        invoiceService.generateInvoice(orderDetailsData);
        return order;
    }

    /**
     * @param orderId       ID of the order to be updated
     * @param newOrderItems updated items for the order
     * @throws ApiException for insufficient inventory or invalid order items
     */
    private void updateItems(Integer orderId, List<OrderItem> newOrderItems) throws ApiException {

        List<OrderItem> previousOrderItems = orderItemService.getByOrderId(orderId);
        OrderItemChanges changes = new OrderItemChanges(previousOrderItems, newOrderItems);

        List<OrderItem> orderItems = changes.getAllChanges();
        List<Product> products = new LinkedList<>();

        for (OrderItem orderItem : orderItems) {
            Product product = productService.get(orderItem.getProductId());
            products.add(product);
            productService.validateSellingPrice(product, orderItem.getSellingPrice());
        }

        List<Integer> requiredQuantities = changes.getRequiredQuantities();

        updateInventories(products, requiredQuantities);

        // Update Order Items
        for (OrderItem toUpdate : changes.getItemsToUpdate()) {
            orderItemService.update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItem toDelete : changes.getItemsToDelete()) {
            orderItemService.deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItem toCreate : changes.getItemsToAdd()) {
            orderItemService.add(toCreate);
        }
    }

    private List<Product> getProducts(List<OrderItem> orderItems) throws ApiException {
        List<Integer> ids = orderItems
                .stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        return productService.getProductsByIds(ids);
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
        List<Product> products = getProducts(orderItems);

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

            if (ValidationUtil.isNegativeNumber(form.getSellingPrice())) {
                throw new ApiException("Invalid input: 'price' should be a positive number!");
            }

            if (ValidationUtil.isBlank(form.getBarcode())) {
                ApiException.throwCantBeBlank("barcode");
            }
        }
    }

    private List<OrderItem> getOrderItems(Integer orderId, List<OrderItemForm> orderFormItems) throws ApiException {
        List<OrderItem> orderItems = new LinkedList<>();

        for (OrderItemForm form : orderFormItems) {
            Product product = productService.getByBarcode(form.getBarcode());
            OrderItem item = ConversionUtil.convertFromToPojo(orderId, form, product);
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

    public Order getOrder(Integer orderId) throws ApiException {
        return orderService.get(orderId);
    }
}
