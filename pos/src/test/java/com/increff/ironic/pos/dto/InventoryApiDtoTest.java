package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
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

    private List<Product> products;

    @Before
    public void setUp() throws ApiException {
        List<Brand> brands = MockUtils.setUpBrands(brandService);
        products = MockUtils.setupProducts(brands, productService);
        List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        MockUtils.setUpInventory(productIds, inventoryService);
    }

    @Test
    public void testGetInventoryItemByIdForInvalidBarcodeThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        String barcode = "INVALID_BARCODE_101";

        exceptionRule.expectMessage("Can't find any product with barcode: " + barcode);
        inventoryApiDto.getByBarcode(barcode);
    }

    @Test
    public void testGetInventoryItemByIdForValidBarcodeReturnsData() throws ApiException {
        String barcode = "a1001";
        InventoryData actual = inventoryApiDto.getByBarcode(barcode);
        InventoryData expected = new InventoryData(barcode, "iphone x", 10);
        AssertUtils.assertEqualInventoryData(expected, actual);
    }

    @Test
    public void testUpdateInventoryItem() throws ApiException {
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setQuantity(100);
        inventoryApiDto.update("a1001", inventoryForm);

        Integer productId = products
                .stream()
                .filter(it -> it.getBarcode().equals("a1001"))
                .collect(Collectors.toList()).get(0)
                .getId();

        Inventory inventory = inventoryService.get(productId);
        Assert.assertEquals(Integer.valueOf(100), inventory.getQuantity());
    }

    @Test
    public void testUpdateInventoryItemWithNegativeQuantityThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid input: 'quantity' should not be a negative number!");
        inventoryApiDto.update("a1001", new InventoryForm(-1));
    }

    @Test
    public void testGetAllInventoryData() {
        List<InventoryData> expected = MockUtils.getMockInventoryData();
        List<InventoryData> actual = inventoryApiDto.getAll();
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualInventoryData);
    }

}
