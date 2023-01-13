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
        inventoryService.updateInventory(products, requiredQuantities);

        // Adding Order Items to database
        for (OrderItem item : orderItems) {
            create(item);
        }
    }

    /**
     * Steps
     *
     * @param orderId       ID of the order to be updated
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

        inventoryService.updateInventory(productsToCreateOrUpdate, requiredQuantities);

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

}
