package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.ProductPojo;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductServiceTest extends AbstractUnitTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductDao productDao;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void getByIdForInvalidIdThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int invalidId = -1;
        exceptionRule.expectMessage("Can't find any product with ID: " + invalidId);
        productService.get(invalidId);
    }

    @Test
    public void getByIdForValidIdReturnProductEntity() throws ApiException {
        ProductPojo expected = MockUtils.getMockProduct();
        productDao.insert(expected);
        ProductPojo actual = productService.get(expected.getId());
        AssertUtils.assertEqualProducts(expected, actual);
    }

    private List<ProductPojo> insertMockProducts(int size) {
        List<ProductPojo> mockProductEntities = MockUtils.getMockProducts(size);
        mockProductEntities.forEach(productDao::insert);
        return mockProductEntities;
    }

    @Test
    public void getAllReturnsAllProducts() {
        List<ProductPojo> expected = insertMockProducts(4);
        List<ProductPojo> actual = productService.getAll();
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualProducts);
    }

    @Test
    public void getProductByIds() throws ApiException {
        List<ProductPojo> productEntities = insertMockProducts(5);
        List<Integer> ids = Arrays.asList(productEntities.get(1).getId(), productEntities.get(4).getId(), productEntities.get(2).getId());

        List<ProductPojo> expected = productEntities
                .stream()
                .filter(product -> ids.contains(product.getId()))
                .sorted(Comparator.comparing(ProductPojo::getId))
                .collect(Collectors.toList());

        List<ProductPojo> actual = productService.getProductsByIds(ids)
                .stream()
                .sorted(Comparator.comparing(ProductPojo::getId))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualProducts);
    }

    @Test
    public void getByBarcodeForValidBarcodeReturnsProduct() throws ApiException {
        ProductPojo mock = MockUtils.getMockProduct();
        productDao.insert(mock);

        String barcode = mock.getBarcode();
        ProductPojo actual = productService.getByBarcode(barcode);

        AssertUtils.assertEqualProducts(mock, actual);
    }

    @Test
    public void getByBarcodeForInvalidBarcodeThrowsException() throws ApiException {
        String invalidBarcode = "Invalid Barcode";
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Can't find any product with barcode: " + invalidBarcode);
        productService.getByBarcode(invalidBarcode);
    }

    @Test
    public void addValidProductInsertsEntity() throws ApiException {
        ProductPojo productPojo = MockUtils.getMockProduct();
        productService.add(productPojo);

        Integer id = productPojo.getId();
        ProductPojo actual = productDao.select(id);

        AssertUtils.assertEqualProducts(productPojo, actual);
    }

    @Test
    public void addDuplicateProductThrowsException() throws ApiException {
        ProductPojo original = MockUtils.getMockProduct();
        productDao.insert(original);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("A product with barcode: " + original.getBarcode() + " already exists!");
        ProductPojo duplicate = MockUtils.getMockProduct();
        productService.add(duplicate);
    }

    @Test
    public void updateExistingProductsModifiesTheEntity() throws ApiException {
        ProductPojo productPojo = MockUtils.getMockProduct();
        productDao.insert(productPojo);

        productPojo.setPrice(10000.0);
        productService.update(productPojo);

        ProductPojo actual = productDao.select(productPojo.getId());
        AssertUtils.assertEqualProducts(productPojo, actual);
    }

    @Test
    public void updateProductTestChangingBarcodeToAlreadyExistingThrowsException() throws ApiException {
        ProductPojo alreadyExistingProduct = MockUtils.getMockProduct();
        alreadyExistingProduct.setBarcode("already_existing");
        productDao.insert(alreadyExistingProduct);

        ProductPojo productPojoToUpdate = MockUtils.getMockProduct();
        productDao.insert(productPojoToUpdate);

        exceptionRule.expect(ApiException.class);
        String message = "Barcode " + alreadyExistingProduct.getBarcode() + " is already being used!";
        exceptionRule.expectMessage(message);

        ProductPojo updateProductPojo = MockUtils.getMockProduct();
        updateProductPojo.setBarcode(alreadyExistingProduct.getBarcode());
        updateProductPojo.setId(productPojoToUpdate.getId());

        productService.update(updateProductPojo);
    }

    @Test
    public void updateProductTestForNotExistingProductThrowsException() throws ApiException {
        ProductPojo productPojo = MockUtils.getMockProduct();
        int invalidId = -1;
        productPojo.setId(invalidId);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Can't find any product with ID: " + invalidId);

        productPojo.setName("New Name");
        productService.update(productPojo);
    }

}
