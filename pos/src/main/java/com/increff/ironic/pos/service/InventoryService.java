package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductInventoryQuantity;
import com.increff.ironic.pos.pojo.InventoryPojo;
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

    public InventoryPojo get(Integer id) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.select(id);
        if (inventoryPojo == null) {
            throw new ApiException("No inventory found for ID: " + id);
        }
        return inventoryPojo;
    }

    public List<InventoryPojo> getAll() {
        return inventoryDao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(InventoryPojo inventoryPojo) throws ApiException {
        if (isDuplicate(inventoryPojo)) {
            throw new ApiException("Inventory for product id: " + inventoryPojo.getProductId() + " already exists!");
        }
        inventoryDao.insert(inventoryPojo);
    }

    private boolean isDuplicate(InventoryPojo inventoryPojo) {
        return inventoryDao.select(inventoryPojo.getProductId()) != null;
    }

    @Transactional(rollbackOn = ApiException.class)
    public InventoryPojo update(InventoryPojo inventoryPojo) throws ApiException {
        get(inventoryPojo.getId());
        return inventoryDao.update(inventoryPojo);
    }

    public List<InventoryPojo> getByIds(List<Integer> inventoryIds) throws ApiException {
        List<InventoryPojo> inventoryPojoList = new LinkedList<>();

        for (Integer id : inventoryIds) {
            inventoryPojoList.add(get(id));
        }

        return inventoryPojoList;
    }

    public void validateSufficientQuantity(List<ProductInventoryQuantity> productInventoryQuantityList) throws ApiException {
        for (ProductInventoryQuantity item : productInventoryQuantityList) {
            Integer required = item.getRequiredQuantity();
            Integer inStock = item.getInventoryPojo().getQuantity();

            if (required > inStock) {
                String message = insufficientStock(item.getBarcode(), item.getProductName(), inStock);
                throw new ApiException(message);
            }
        }
    }

    public void updateInventories(List<ProductInventoryQuantity> inventoryRequiredQuantityList) throws ApiException {

        for (ProductInventoryQuantity inventoryQuantity : inventoryRequiredQuantityList) {
            InventoryPojo inventoryPojo = inventoryQuantity.getInventoryPojo();
            Integer requiredQuantity = inventoryQuantity.getRequiredQuantity();
            Integer newQuantity = inventoryPojo.getQuantity() - requiredQuantity;

            inventoryPojo.setQuantity(newQuantity);
            update(inventoryPojo);
        }
    }

    private String insufficientStock(String name, String barcode, Integer inStock) {
        return "Insufficient inventory for " + "[" + barcode + "] \"" + name + "\", only " + inStock + " units are left!";
    }

}
