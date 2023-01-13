package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryDao inventoryDao;

    @Autowired
    public InventoryService(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    public Inventory get(Integer id) throws ApiException {
        Inventory inventory = inventoryDao.select(id);
        if (inventory == null) {
            throw new ApiException("No inventory found for ID: " + id);
        }
        return inventory;
    }

    public List<Inventory> getAll() {
        return inventoryDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(Inventory inventory) throws ApiException {
        if (isDuplicate(inventory)) {
            throw new ApiException("Inventory for product id: " + inventory.getProductId() + " already exists!");
        }
        inventoryDao.insert(inventory);
    }

    private boolean isDuplicate(Inventory inventory) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("productId", inventory.getProductId());
        List<Inventory> list = inventoryDao.selectWhereEquals(condition);
        return !list.isEmpty();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void delete(Integer id) throws ApiException {
        inventoryDao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Inventory inventory) throws ApiException {
        get(inventory.getId());
        inventoryDao.update(inventory);
    }

    public List<Inventory> getByIds(List<Integer> inventoryIds) throws ApiException {
        List<Inventory> inventoryList = new LinkedList<>();

        for (Integer id : inventoryIds) {
            inventoryList.add(get(id));
        }

        return inventoryList;
    }


    private List<InventoryRequiredQuantity> zipInventoryRequiredQuantity(
            List<Inventory> inventoryList,
            List<Integer> requiredQuantities) {

        List<InventoryRequiredQuantity> combinedList = new LinkedList<>();

        for (int i = 0; i < inventoryList.size(); i++) {
            InventoryRequiredQuantity item = new InventoryRequiredQuantity();
            item.setInventory(inventoryList.get(i));
            item.setRequiredQuantity(requiredQuantities.get(i));
            combinedList.add(item);
        }

        return combinedList;
    }

    public void updateInventory(List<Product> products, List<Integer> requiredQuantities) throws ApiException {
        List<Inventory> inventories = getInventoryFromProducts(products);

        // Validating inventory
        validateInventory(zipProductInventory(products, inventories, requiredQuantities));

        // Updating the inventory
        updateInventory(zipInventoryRequiredQuantity(inventories, requiredQuantities));
    }

    private void validateInventory(List<ProductInventoryQuantity> productInventoryQuantityList) throws ApiException {
        // Fetching product wise inventory
        for (ProductInventoryQuantity item : productInventoryQuantityList) {
            Integer required = item.requiredQuantity;
            Integer inStock = item.existingInventory;

            if (required > inStock) {
                throw new ApiException(insufficientStock(item.barcode, item.productName, inStock));
            }
        }
    }

    private void updateInventory(List<InventoryRequiredQuantity> inventoryRequiredQuantityList) throws ApiException {

        for (InventoryRequiredQuantity inventoryQuantity : inventoryRequiredQuantityList) {
            Inventory inventory = inventoryQuantity.getInventory();
            Integer requiredQuantity = inventoryQuantity.getRequiredQuantity();

            Integer newQuantity = inventory.getQuantity() - requiredQuantity;
            inventory.setQuantity(newQuantity);
            update(inventory);
        }
    }

    @Data
    private static class InventoryRequiredQuantity {
        private Inventory inventory;
        private Integer requiredQuantity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ProductInventoryQuantity {
        private String barcode;
        private String productName;
        private Integer existingInventory;
        private Integer requiredQuantity;
    }

    private List<ProductInventoryQuantity> zipProductInventory(
            List<Product> products,
            List<Inventory> inventories,
            List<Integer> requiredQuantities) throws IllegalArgumentException {

        if (products.size() != inventories.size() && requiredQuantities.size() != inventories.size()) {
            throw new IllegalArgumentException("Size of Products, Inventory and Quantity list should be same!");
        }

        List<ProductInventoryQuantity> combinedList = new LinkedList<>();

        for (int i = 0; i < products.size(); i++) {
            ProductInventoryQuantity item = new ProductInventoryQuantity();
            Product product = products.get(i);

            item.setProductName(product.getName());
            item.setBarcode(product.getBarcode());
            item.setRequiredQuantity(requiredQuantities.get(i));
            item.setExistingInventory(inventories.get(i).getQuantity());

            combinedList.add(item);
        }

        return combinedList;
    }

    private String insufficientStock(String name, String barcode, Integer inStock) {
        return "Insufficient inventory for " + "[" + barcode + "] \"" + name + "\", only " + inStock + " units are left!";
    }

    private List<Inventory> getInventoryFromProducts(List<Product> products) throws ApiException {

        List<Integer> productIds = products
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        return getByIds(productIds);
    }

}
