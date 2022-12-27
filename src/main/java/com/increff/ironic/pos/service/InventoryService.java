package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
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
        inventoryDao.insert(inventory);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void delete(Integer id) throws ApiException {
        inventoryDao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Inventory inventory) throws ApiException {
        inventoryDao.update(inventory);
    }

}
