package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderData;
import com.increff.ironic.pos.model.data.OrderDetailsData;
import com.increff.ironic.pos.model.data.OrderItemChanges;
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
import java.util.LinkedList;
import java.util.List;
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
     *
     * @param orderFormItems list of order items from the user
     */
    @Transactional(rollbackOn = ApiException.class)
    public void createOrder(List<OrderItemForm> orderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(orderFormItems);

        // Creating new order
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        order = orderService.create(order); // After generating ID

        List<OrderItem> orderItems = getOrderItems(order.getId(), orderFormItems);

        // Fetching products from barcode
        List<Product> products = getProducts(orderItems);
        validateSellingPrice(orderItems, products);

        // Checking if there are sufficient items in inventory
        List<Integer> requiredQuantities = getQuantities(orderItems);

        // Get inventory from products
        inventoryService.updateInventory(products, requiredQuantities);

        // Creating order items
        for (OrderItem item : orderItems) {
            orderItemService.create(item);
        }

        // Generate PDF
        OrderDetailsData orderDetailsData = getOrderDetails(order.getId());
        String path = invoiceService.generateInvoice(orderDetailsData);

        // Updating invoice path
        order.setInvoicePath(path);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
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
    }

    /**
     * @param orderId       ID of the order to be updated
     * @param newOrderItems updated items for the order
     * @throws ApiException for insufficient inventory or invalid order items
     */
    private void updateItems(Integer orderId, List<OrderItem> newOrderItems) throws ApiException {

        List<OrderItem> previousOrderItems = orderItemService.getByOrderId(orderId);
        OrderItemChanges changes = new OrderItemChanges(previousOrderItems, newOrderItems);

        List<OrderItem> createOrUpdateItems = changes.getAllChanges();
        List<Product> productsToCreateOrUpdate = getProducts(createOrUpdateItems);
        List<Integer> requiredQuantities = changes.getRequiredQuantities();

        inventoryService.updateInventory(productsToCreateOrUpdate, requiredQuantities);

        // Update Order Items
        for (OrderItem toUpdate : changes.getItemsToUpdate()) {
            orderItemService.update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItem toDelete : changes.getItemsToDelete()) {
            orderItemService.deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItem toCreate : changes.getItemsToCreate()) {
            orderItemService.create(toCreate);
        }
    }

    private List<Integer> getQuantities(List<OrderItem> orderItems) {
        return orderItems
                .stream()
                .map(OrderItem::getQuantity)
                .collect(Collectors.toList());
    }

    private List<Product> getProducts(List<OrderItem> orderItems) throws ApiException {
        List<Integer> ids = orderItems
                .stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        return productService.getProductsByIds(ids);
    }

    private void validateSellingPrice(List<OrderItem> orderItems, List<Product> products) throws ApiException {
        for (int i = 0; i < orderItems.size(); i++) {
            boolean isPriceGreaterThanMRP = orderItems.get(i).getSellingPrice() > products.get(i).getPrice();

            if (isPriceGreaterThanMRP) {
                throw new ApiException("Selling price can't be more than MRP!");
            }
        }
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

            if (!ValidationUtil.isPositiveNumber(form.getSellingPrice())) {
                throw new ApiException("Invalid input: 'price' should be a positive number!");
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

    public Order getOrder(Integer orderId) throws ApiException {
        return orderService.get(orderId);
    }
}
