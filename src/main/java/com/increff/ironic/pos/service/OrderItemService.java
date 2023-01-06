package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.OrderItemDao;
import com.increff.ironic.pos.exceptions.ApiException;
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

        validateInventory(products, requiredQuantities);

        // Updating the inventory
        updateInventory(requiredQuantities, products);

        // Adding Order Items to database
        for (OrderItem item : orderItems) {
            create(item);
        }
    }

    @Transactional(rollbackOn = ApiException.class)
    public void updateItems(Integer orderId, List<OrderItem> newOrderItems) throws ApiException {

        List<OrderItem> previousOrderItems = getByOrderId(orderId);
        OrderItemChanges changes = new OrderItemChanges(previousOrderItems, newOrderItems);

        List<OrderItem> createOrUpdateItems = changes.getAllChanges();
        List<Product> productsToCreateOrUpdate = getProducts(createOrUpdateItems);
        List<Integer> requiredQuantities = changes.getRequiredQuantities();

        validateInventory(productsToCreateOrUpdate, requiredQuantities);

        // Updating the inventory
        updateInventory(requiredQuantities, productsToCreateOrUpdate);

        // Update Order Items
        for (OrderItem toUpdate : changes.getToUpdate()) {
            update(toUpdate);
        }

        // Deleting Order Items
        for (OrderItem toDelete : changes.getToDelete()) {
            deleteById(toDelete.getId());
        }

        // Adding Order Items to database
        for (OrderItem toCreate : changes.getToCreate()) {
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


    private void updateInventory(List<Integer> quantities, List<Product> products) throws ApiException {
        List<Inventory> inventoryList = getInventoryFromProducts(products);

        for (int i = 0; i < inventoryList.size(); i++) {
            Inventory inventory = inventoryList.get(i);
            Integer newQuantity = inventory.getQuantity() - quantities.get(i);
            inventory.setQuantity(newQuantity);
            inventoryService.update(inventory);
        }

    }

    private static class OrderItemChanges {

        private final List<OrderItem> toUpdate, toDelete, toCreate;
        private final List<OrderItem> oldOrderItems, newOrderItems;

        public OrderItemChanges(List<OrderItem> oldOrderItems, List<OrderItem> newOrderItems) {
            toUpdate = new LinkedList<>();
            toDelete = new LinkedList<>();
            toCreate = new LinkedList<>();
            this.oldOrderItems = oldOrderItems;
            this.newOrderItems = newOrderItems;
            computeChanges();
        }

        public List<OrderItem> getToUpdate() {
            return toUpdate;
        }

        public List<OrderItem> getToDelete() {
            return toDelete;
        }

        public List<OrderItem> getToCreate() {
            return toCreate;
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

        private Map<Integer, OrderItem> getItemMap(List<OrderItem> items) {
            Map<Integer, OrderItem> itemMap = new HashMap<>();
            for (OrderItem item : items) {
                itemMap.put(item.getProductId(), item);
            }
            return itemMap;
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
