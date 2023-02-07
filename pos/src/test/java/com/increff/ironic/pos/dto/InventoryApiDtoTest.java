package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.pojo.BrandPojo;
import com.increff.ironic.pos.pojo.InventoryPojo;
import com.increff.ironic.pos.pojo.ProductPojo;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.service.InventoryService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryApiDtoTest extends AbstractUnitTest {

    @Autowired
    private InventoryApiDto inventoryApiDto;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private List<ProductPojo> productEntities;

    @Before
    public void setUp() throws ApiException {
        List<BrandPojo> brandEntities = MockUtils.setUpBrands(brandService);
        productEntities = MockUtils.setupProducts(brandEntities, productService);
        List<Integer> productIds = productEntities.stream().map(ProductPojo::getId).collect(Collectors.toList());
        MockUtils.setUpInventory(productIds, inventoryService);
    }

    @Test
    public void testGetInventoryItemByIdForInvalidBarcodeThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        Integer invalidId = -1;
        exceptionRule.expectMessage("No inventory found for ID: " + invalidId);
        inventoryApiDto.get(invalidId);
    }

    @Test
    public void testGetInventoryItemByIdForValidIdReturnsData() throws ApiException {
        String barcode = "a1001";
        ProductPojo productPojo = productService.getByBarcode(barcode);
        InventoryData actual = inventoryApiDto.get(productPojo.getId());
        InventoryData expected = new InventoryData(productPojo.getId(), barcode, "iphone x", 10);
        AssertUtils.assertEqualInventoryData(expected, actual);
    }

    @Test
    public void testUpdateInventoryItem() throws ApiException {
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setBarcode("a1001");
        inventoryForm.setQuantity(100);
        inventoryApiDto.update(inventoryForm);

        Integer productId = productEntities
                .stream()
                .filter(it -> it.getBarcode().equals("a1001"))
                .collect(Collectors.toList()).get(0)
                .getId();

        InventoryPojo inventoryPojo = inventoryService.get(productId);
        Assert.assertEquals(Integer.valueOf(100), inventoryPojo.getQuantity());
    }

    @Test
    public void testUpdateInventoryItemWithNegativeQuantityThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid input: 'quantity' should not be a negative number!");
        inventoryApiDto.update(new InventoryForm("a1001", -1));
    }

    @Test
    public void testGetAllInventoryData() {
        List<InventoryData> expected = MockUtils.getMockInventoryData();
        List<InventoryData> actual = inventoryApiDto.getAll();
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualInventoryData);
    }

}
