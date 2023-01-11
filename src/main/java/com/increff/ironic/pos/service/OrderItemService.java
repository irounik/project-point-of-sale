package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.OrderItemChanges;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.OrderItem;
import com.increff.ironic.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderItemService {

    private final OrderItemDao orderItemDao;
    private final InventoryService inventoryService;
    private final ProductService productService;

    @Autowired
    public OrderItemService(
            OrderItemDao orderItemDao,
            InventoryService inventoryService,
            ProductService productService
    ) {
        this.orderItemDao = orderItemDao;
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void create(OrderItem orderItem) throws ApiException {
        Integer id = orderItem.getId();
        if (id != null && orderItemDao.select(id) != null) {
            throw new ApiException("OrderItem with ID: " + id + " already exists!");
        }
        orderItemDao.insert(orderItem);
    }

    @Transactional
    public void update(OrderItem orderItem) throws ApiException {
        getCheck(orderItem.getId()); // Check existence
        orderItemDao.update(orderItem);
    }

    @Transactional
    public void deleteById(Integer id) {
        orderItemDao.delete(id);
    }

    public void getCheck(Integer id) throws ApiException {
        OrderItem item = orderItemDao.select(id);
        if (item == null) {
            throw new ApiException("No order item found for ID: " + id);
        }
    }

    public List<OrderItem> getAll() {
        return orderItemDao.selectAll();
    }

    public List<OrderItem> getByOrderId(Integer orderId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("orderId", orderId);
        return orderItemDao.selectWhereEquals(condition);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void createItems(List<OrderItem> orderItems) throws ApiException {
        // Fetching products from barcode
        List<Product> products = getProducts(orderItems);

        // Checking if there are sufficient items in inventory
        List<Integer> requiredQuantities = getQuantities(orderItems);

        // Get inventory from products
        List<Inventory> inventories = getInventoryFromProducts(products);

        // Validating inventory
        validateInventory(products, inventories, requiredQuantities);

        // Updating the inventory
        updateInventory(inventories, requiredQuantities);

        // Adding Order Items to database
        for (OrderItem item : orderItems) {
            create(item);
        }
    }

    /**
     * Steps
     * @param orderId ID of the order to be updated
     * @param newOrderItems updated items for the order
     * @throws ApiException for insufficient inventory or invalid order items
     */
    @Transactional(rollbackOn = ApiException.class)
    public void updateItems(Integer orderId, List<OrderItem> newOrderItems) throws ApiException {

        List<OrderItem> previousOrderItems = getByOrderId(orderId);
        OrderItemChanges changes = new OrderItemChanges(previousOrderItems, newOrderItems);

        List<OrderItem> createOrUpdateItems = changes.getAllChanges();
        List<Product> productsToCreateOrUpdate = getProducts(createOrUpdateItems);
        List<Integer> requiredQuantities = changes.getRequiredQuantities();
        List<Inventory> inventoryList = getInventoryFromProducts(productsToCreateOrUpdate);

        validateInventory(productsToCreateOrUpdate, inventoryList, requiredQuantities);

        // Updating the inventory
        updateInventory(inventoryList, requiredQuantities);

        // Update Order Items
        for (OrderItem toUpdate : changes.getItemsToUpdate()) {
            update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItem toDelete : changes.getItemsToDelete()) {
            deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItem toCreate : changes.getItemsToCreate()) {
            create(toCreate);
        }
    }

    public List<Product> getProducts(List<OrderItem> orderItems) throws ApiException {
        List<Integer> ids = orderItems
                .stream()
                .map(OrderItem::getProductId)
                .collect(Collectors.toList());
        return productService.getProductsByIds(ids);
    }

    private List<Integer> getQuantities(List<OrderItem> orderItems) {
        return orderItems
                .stream()
                .map(OrderItem::getQuantity)
                .collect(Collectors.toList());
    }

    private void validateInventory(
            List<Product> products,
            List<Inventory> inventoryList,
            List<Integer> requiredQuantities
    ) throws ApiException {
        // Fetching product wise inventory
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

    private void updateInventory(
            List<Inventory> inventoryList,
            List<Integer> quantities
    ) throws ApiException {
        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventory = inventoryList.get(i);
            Integer newQuantity = inventory.getQuantity() - quantities.get(i);
            inventory.setQuantity(newQuantity);
            inventoryService.update(inventory);
        }
    }

}
