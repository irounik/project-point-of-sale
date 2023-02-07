package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.*;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.pojo.InventoryPojo;
import com.increff.ironic.pos.pojo.OrderItemPojo;
import com.increff.ironic.pos.pojo.OrderPojo;
import com.increff.ironic.pos.pojo.ProductPojo;
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
     * 2. Fetch {@link InventoryPojo} for each product
     * 3. Check if there are enough items in inventory
     * 4. Update inventory, to reserve items for the current order
     * 5. Create new {@link OrderPojo}
     * 6. Create a list of {@link OrderItemPojo} by using form data, product & order ID
     * 7. Add each {@link OrderItemPojo} to the database
     * 8. Generating invoice (PDF)
     * 9. Adding path to the invoice in the Order Entity.
     * 10. Return the created invoice entity
     *
     * @param orderFormItems list of order items from the user
     */
    @Transactional(rollbackOn = ApiException.class)
    public OrderPojo createOrder(List<OrderItemForm> orderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(orderFormItems);

        // Creating new order
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setTime(LocalDateTime.now(ZoneOffset.UTC));
        orderService.add(orderPojo);

        int size = orderFormItems.size();
        List<ProductPojo> productEntities = new ArrayList<>(size);
        List<OrderItemPojo> orderItemEntities = new ArrayList<>(size);
        List<Integer> requiredQuantities = new ArrayList<>(size);

        for (OrderItemForm itemForm : orderFormItems) {
            ProductPojo productPojo = productService.getByBarcode(itemForm.getBarcode());
            productEntities.add(productPojo);

            // Validating validating selling price
            productService.validateSellingPrice(productPojo, itemForm.getSellingPrice());

            OrderItemPojo orderItemPojo = ConversionUtil.convertFromToPojo(orderPojo.getId(), itemForm, productPojo);
            orderItemEntities.add(orderItemPojo);

            requiredQuantities.add(itemForm.getQuantity());
        }

        // Get inventory from products
        updateInventories(productEntities, requiredQuantities);

        // Creating order items
        for (OrderItemPojo item : orderItemEntities) {
            orderItemService.add(item);
        }

        return orderPojo;
    }

    private void updateInventories(List<ProductPojo> productEntities, List<Integer> requiredQuantities) throws ApiException {

        List<InventoryPojo> inventories = getInventoryFromProducts(productEntities);

        // Preparing data for updating inventory
        List<ProductInventoryQuantity> productInventoryQuantities = getInventoryQuantityList(
                productEntities,
                inventories,
                requiredQuantities
        );

        // Validating inventory
        inventoryService.validateSufficientQuantity(productInventoryQuantities);

        // Updating the inventory
        inventoryService.updateInventories(productInventoryQuantities);
    }

    private List<ProductInventoryQuantity> getInventoryQuantityList(
            List<ProductPojo> productEntities,
            List<InventoryPojo> inventories,
            List<Integer> requiredQuantities) {

        if (productEntities.size() != inventories.size() && requiredQuantities.size() != inventories.size()) {
            throw new IllegalArgumentException("Size of Products, Inventory and Quantity list should be same!");
        }

        List<ProductInventoryQuantity> combinedList = new LinkedList<>();

        productEntities.sort(Comparator.comparing(ProductPojo::getId));
        inventories.sort(Comparator.comparing(InventoryPojo::getProductId));

        Iterator<ProductPojo> productItr = productEntities.iterator();
        Iterator<InventoryPojo> inventoryItr = inventories.iterator();
        Iterator<Integer> requiredQuantityItr = requiredQuantities.iterator();

        while (productItr.hasNext()) {
            ProductInventoryQuantity item = new ProductInventoryQuantity();
            ProductPojo productPojo = productItr.next();

            item.setProductName(productPojo.getName());
            item.setBarcode(productPojo.getBarcode());
            item.setRequiredQuantity(requiredQuantityItr.next());
            item.setInventoryPojo(inventoryItr.next());

            combinedList.add(item);
        }

        return combinedList;
    }

    private List<InventoryPojo> getInventoryFromProducts(List<ProductPojo> productEntities) throws ApiException {

        List<Integer> productIds = productEntities
                .stream()
                .map(ProductPojo::getId)
                .collect(Collectors.toList());

        return inventoryService.getByIds(productIds);
    }

    @Transactional(rollbackOn = ApiException.class)
    public OrderPojo updateOrder(Integer orderId, List<OrderItemForm> updatedOrderFormItems) throws ApiException {
        // Validate order form
        validateOrderForm(updatedOrderFormItems);

        // Updating order items
        List<OrderItemPojo> orderItemEntities = getOrderItems(orderId, updatedOrderFormItems);
        updateItems(orderId, orderItemEntities);

        // Updating order time
        OrderPojo orderPojo = orderService.get(orderId);
        orderPojo.setTime(LocalDateTime.now(ZoneOffset.UTC));

        // Updating invoice path
        orderPojo.setInvoicePath(null);
        return orderPojo;
    }

    /**
     * @param orderId              ID of the order to be updated
     * @param newOrderItemEntities updated items for the order
     * @throws ApiException for insufficient inventory or invalid order items
     */
    private void updateItems(Integer orderId, List<OrderItemPojo> newOrderItemEntities) throws ApiException {

        List<OrderItemPojo> previousOrderItemEntities = orderItemService.getByOrderId(orderId);
        OrderItemChanges changes = new OrderItemChanges(previousOrderItemEntities, newOrderItemEntities);

        List<OrderItemPojo> orderItemEntities = changes.getAllChanges();
        List<ProductPojo> productEntities = new LinkedList<>();

        for (OrderItemPojo orderItemPojo : orderItemEntities) {
            ProductPojo productPojo = productService.get(orderItemPojo.getProductId());
            productEntities.add(productPojo);
            productService.validateSellingPrice(productPojo, orderItemPojo.getSellingPrice());
        }

        List<Integer> requiredQuantities = changes.getRequiredQuantities();

        updateInventories(productEntities, requiredQuantities);

        orderItemService.updateOrderItems(changes);
    }

    private List<ProductPojo> getProducts(List<OrderItemPojo> orderItemEntities) throws ApiException {
        List<Integer> ids = orderItemEntities
                .stream()
                .map(OrderItemPojo::getProductId)
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
        OrderPojo orderPojo = orderService.get(orderId);
        data.setOrderId(orderPojo.getId());
        data.setTime(orderPojo.getTime());

        // Setting order item details
        List<OrderItemPojo> orderItemEntities = orderItemService.getByOrderId(orderId);
        List<ProductPojo> productEntities = getProducts(orderItemEntities);

        List<OrderItemData> detailsList = getDetailsList(productEntities, orderItemEntities);
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
                throw new ApiException("Invalid0 input: 'price' should be a positive number!");
            }

            if (ValidationUtil.isBlank(form.getBarcode())) {
                ApiException.throwCantBeBlank("barcode");
            }
        }
    }

    private List<OrderItemPojo> getOrderItems(Integer orderId, List<OrderItemForm> orderFormItems) throws ApiException {
        List<OrderItemPojo> orderItemEntities = new LinkedList<>();

        for (OrderItemForm form : orderFormItems) {
            ProductPojo productPojo = productService.getByBarcode(form.getBarcode());
            OrderItemPojo item = ConversionUtil.convertFromToPojo(orderId, form, productPojo);
            orderItemEntities.add(item);
        }

        return orderItemEntities;
    }

    private List<OrderItemData> getDetailsList(List<ProductPojo> productEntities, List<OrderItemPojo> orderItemEntities) {
        List<OrderItemData> dataList = new LinkedList<>();

        for (int i = 0; i < productEntities.size(); i++) {
            OrderItemData data = ConversionUtil.convertPojoToData(orderItemEntities.get(i), productEntities.get(i));
            dataList.add(data);
        }

        return dataList;
    }

    public OrderPojo getOrder(Integer orderId) throws ApiException {
        return orderService.get(orderId);
    }

    private String generateInvoice(Integer orderId) throws ApiException {
        OrderPojo orderPojo = orderService.get(orderId);

        // Generate PDF
        OrderDetailsData orderDetailsData = getOrderDetails(orderId);
        String path = invoiceService.generateInvoice(orderDetailsData);

        if (path == null) {
            throw new ApiException("Error occured while generating invoice for order");
        }

        // Updating invoice path
        orderPojo.setInvoicePath(path);

        return path;
    }

    public String getInvoicePath(Integer orderId) throws ApiException {
        String invoicePath = getOrder(orderId).getInvoicePath();

        if (invoicePath == null) {
            invoicePath = generateInvoice(orderId);
        }

        return invoicePath;
    }

}
