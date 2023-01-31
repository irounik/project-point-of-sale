package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Product;
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
        Product expected = MockUtils.getMockProduct();
        productDao.insert(expected);
        Product actual = productService.get(expected.getId());
        AssertUtils.assertEqualProducts(expected, actual);
    }

    private List<Product> insertMockProducts(int size) {
        List<Product> mockProducts = MockUtils.getMockProducts(size);
        mockProducts.forEach(productDao::insert);
        return mockProducts;
    }

    @Test
    public void getAllReturnsAllProducts() {
        List<Product> expected = insertMockProducts(4);
        List<Product> actual = productService.getAll();
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualProducts);
    }

    @Test
    public void getProductByIds() throws ApiException {
        List<Product> products = insertMockProducts(5);
        List<Integer> ids = Arrays.asList(products.get(1).getId(), products.get(4).getId(), products.get(2).getId());

        List<Product> expected = products
                .stream()
                .filter(product -> ids.contains(product.getId()))
                .sorted(Comparator.comparing(Product::getId))
                .collect(Collectors.toList());

        List<Product> actual = productService.getProductsByIds(ids)
                .stream()
                .sorted(Comparator.comparing(Product::getId))
                .collect(Collectors.toList());

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualProducts);
    }

    @Test
    public void getByBarcodeForValidBarcodeReturnsProduct() throws ApiException {
        Product mock = MockUtils.getMockProduct();
        productDao.insert(mock);

        String barcode = mock.getBarcode();
        Product actual = productService.getByBarcode(barcode);

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
        Product product = MockUtils.getMockProduct();
        productService.add(product);

        Integer id = product.getId();
        Product actual = productDao.select(id);

        AssertUtils.assertEqualProducts(product, actual);
    }

    @Test
    public void addDuplicateProductThrowsException() throws ApiException {
        Product original = MockUtils.getMockProduct();
        productDao.insert(original);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("A product with barcode: " + original.getBarcode() + " already exists!");
        Product duplicate = MockUtils.getMockProduct();
        productService.add(duplicate);
    }

    @Test
    public void updateExistingProductsModifiesTheEntity() throws ApiException {
        Product product = MockUtils.getMockProduct();
        productDao.insert(product);

        product.setPrice(10000.0);
        productService.update(product);

        Product actual = productDao.select(product.getId());
        AssertUtils.assertEqualProducts(product, actual);
    }

    @Test
    public void updateProductTestChangingBarcodeToAlreadyExistingThrowsException() throws ApiException {
        Product alreadyExistingProduct = MockUtils.getMockProduct();
        alreadyExistingProduct.setBarcode("already_existing");
        productDao.insert(alreadyExistingProduct);

        Product productToUpdate = MockUtils.getMockProduct();
        productDao.insert(productToUpdate);

        exceptionRule.expect(ApiException.class);
        String message = "Barcode " + alreadyExistingProduct.getBarcode() + " is already being used!";
        exceptionRule.expectMessage(message);

        Product updateProduct = MockUtils.getMockProduct();
        updateProduct.setBarcode(alreadyExistingProduct.getBarcode());
        updateProduct.setId(productToUpdate.getId());

        productService.update(updateProduct);
    }

    @Test
    public void updateProductTestForNotExistingProductThrowsException() throws ApiException {
        Product product = MockUtils.getMockProduct();
        int invalidId = -1;
        product.setId(invalidId);

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Can't find any product with ID: " + invalidId);

        product.setName("New Name");
        productService.update(product);
    }

}
