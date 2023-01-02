package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
