package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductInventoryQuantity;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
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

    // TODO: 27/01/23 update inventory should just update, move the validate or call validate before this call
    // TODO: 27/01/23 what will happen if same product comes twice in products list -
    //  try to use map and prepare the required data at once instead of iterating multiple times and if possible do this is in orderDto itself
    @Transactional(rollbackOn = ApiException.class)
    public void updateInventories(List<Product> products, List<Integer> requiredQuantities) throws ApiException {
        List<Inventory> inventories = getInventoryFromProducts(products);

        // Validating inventory
        List<ProductInventoryQuantity> productInventoryQuantities = zipProductInventory(products, inventories, requiredQuantities);
        validateInventory(productInventoryQuantities);

        // Updating the inventory
        updateInventories(productInventoryQuantities);
    }

    private void validateInventory(List<ProductInventoryQuantity> productInventoryQuantityList) throws ApiException {
        // Fetching product wise inventory
        for (ProductInventoryQuantity item : productInventoryQuantityList) {
            Integer required = item.getRequiredQuantity();
            Integer inStock = item.getInventory().getQuantity();

            if (required > inStock) {
                String message = insufficientStock(item.getBarcode(), item.getProductName(), inStock);
                throw new ApiException(message);
            }
        }
    }

    private void updateInventories(List<ProductInventoryQuantity> inventoryRequiredQuantityList) throws ApiException {

        for (ProductInventoryQuantity inventoryQuantity : inventoryRequiredQuantityList) {
            Inventory inventory = inventoryQuantity.getInventory();
            Integer requiredQuantity = inventoryQuantity.getRequiredQuantity();
            Integer newQuantity = inventory.getQuantity() - requiredQuantity;

            inventory.setQuantity(newQuantity);
            update(inventory);
        }
    }

    // TODO: 27/01/23 try to not depend on indices
    private List<ProductInventoryQuantity> zipProductInventory(
            List<Product> products,
            List<Inventory> inventories,
            List<Integer> requiredQuantities) throws IllegalArgumentException {

        if (products.size() != inventories.size() && requiredQuantities.size() != inventories.size()) {
            throw new IllegalArgumentException("Size of Products, Inventory and Quantity list should be same!");
        }

        products.sort(Comparator.comparing(Product::getId));
        inventories.sort(Comparator.comparing(Inventory::getProductId));

        List<ProductInventoryQuantity> combinedList = new LinkedList<>();

        for (int i = 0; i < products.size(); i++) {
            ProductInventoryQuantity item = new ProductInventoryQuantity();
            Product product = products.get(i);

            item.setProductName(product.getName());
            item.setBarcode(product.getBarcode());
            item.setRequiredQuantity(requiredQuantities.get(i));
            item.setInventory(inventories.get(i));

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
