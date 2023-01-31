package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductInventoryQuantity;
import com.increff.ironic.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

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
        return inventoryDao.select(inventory.getProductId()) != null;
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

    public void validateSufficientQuantity(List<ProductInventoryQuantity> productInventoryQuantityList) throws ApiException {
        for (ProductInventoryQuantity item : productInventoryQuantityList) {
            Integer required = item.getRequiredQuantity();
            Integer inStock = item.getInventory().getQuantity();

            if (required > inStock) {
                String message = insufficientStock(item.getBarcode(), item.getProductName(), inStock);
                throw new ApiException(message);
            }
        }
    }

    public void updateInventories(List<ProductInventoryQuantity> inventoryRequiredQuantityList) throws ApiException {

        for (ProductInventoryQuantity inventoryQuantity : inventoryRequiredQuantityList) {
            Inventory inventory = inventoryQuantity.getInventory();
            Integer requiredQuantity = inventoryQuantity.getRequiredQuantity();
            Integer newQuantity = inventory.getQuantity() - requiredQuantity;

            inventory.setQuantity(newQuantity);
            update(inventory);
        }
    }

    private String insufficientStock(String name, String barcode, Integer inStock) {
        return "Insufficient inventory for " + "[" + barcode + "] \"" + name + "\", only " + inStock + " units are left!";
    }

}
