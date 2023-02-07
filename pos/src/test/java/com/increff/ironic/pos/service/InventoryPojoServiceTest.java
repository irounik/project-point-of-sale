package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.InventoryDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductInventoryQuantity;
import com.increff.ironic.pos.pojo.InventoryPojo;
import com.increff.ironic.pos.pojo.ProductPojo;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class InventoryPojoServiceTest extends AbstractUnitTest {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    InventoryDao inventoryDao;


    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
    }

    @Test
    public void addInventory() throws ApiException {
        InventoryPojo expected = MockUtils.getMockInventory(1);
        inventoryService.add(expected);
        InventoryPojo actual = inventoryDao.select(1);
        AssertUtils.assertEqualInventory(expected, actual);
    }

    @Test
    public void addingDuplicateInventoryThrowsException() throws ApiException {
        int productId = 1;
        InventoryPojo original = MockUtils.getMockInventory(productId);
        inventoryService.add(original);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Inventory for product id: " + productId + " already exists!");
        InventoryPojo duplicate = MockUtils.getMockInventory(productId);
        inventoryService.add(duplicate);
    }

    @Test
    public void updatingNonExistingInventoryThrowsException() throws ApiException {
        int invalidId = -1;
        InventoryPojo mock = MockUtils.getMockInventory(invalidId);
        mock.setQuantity(100);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("No inventory found for ID: " + invalidId);
        inventoryService.update(mock);
    }

    @Test
    public void updateInventory() throws ApiException {
        InventoryPojo mock = MockUtils.getMockInventory(1);
        mock.setQuantity(10);

        inventoryDao.insert(mock);

        mock.setQuantity(100);
        inventoryService.update(mock);

        InventoryPojo actual = inventoryDao.select(1);
        AssertUtils.assertEqualInventory(mock, actual);
    }

    @Test
    public void validateInventoryWithInsufficientStock() throws ApiException {
        Integer productId = 1;
        ProductPojo productPojo = MockUtils.getMockProduct();
        productPojo.setId(productId);
        InventoryPojo inventoryPojo = MockUtils.getMockInventory(productId);
        inventoryDao.insert(inventoryPojo);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Insufficient inventory");

        ProductInventoryQuantity productInventoryQuantity = MockUtils.getMockProductInventoryQuantity(
                productPojo, inventoryPojo, 200
        );

        inventoryService.validateSufficientQuantity(Collections.singletonList(productInventoryQuantity));
    }

}
