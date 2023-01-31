package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProductDtoTest extends AbstractUnitTest {

    @Autowired
    private ProductApiDto productApiDto;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private List<ProductData> productDataList;
    private List<Brand> mockBrands;

    @Before
    public void setUp() {
        mockBrands = MockUtils.setUpBrands(brandService);
        List<Product> mockProducts = MockUtils.setupProducts(mockBrands, productService);
        productDataList = MockUtils.getMockProductDataList(mockBrands, mockProducts);
    }

    @Test
    public void addingProductWithUnknownBrandThrowsException() throws ApiException {
        ProductForm productForm = new ProductForm();
        productForm.setBrandName("INVALID_BRAND");
        productForm.setCategory("INVALID_CATEGORY");
        productForm.setName("Product Name");
        productForm.setPrice(1000.0034);
        productForm.setBarcode("aBcd134");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("No brand found for name INVALID_BRAND and category INVALID_CATEGORY");

        Product expected = productApiDto.add(productForm);
        Product actual = productService.getByBarcode(expected.getBarcode());
        AssertUtils.assertEqualProducts(expected, actual);
    }

    @Test
    public void addingProductWithBlankNameThrowsApiException() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        productForm.setName("   ");

        exceptionRule.expect(ApiException.class);
        String message = getBlankMessage("product name");
        exceptionRule.expectMessage(message);
        productApiDto.add(productForm);
    }

    @Test
    public void addingProductWithBlankBarcodeThrowsApiException() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        productForm.setBarcode(null);

        exceptionRule.expect(ApiException.class);
        String message = getBlankMessage("barcode");
        exceptionRule.expectMessage(message);
        productApiDto.add(productForm);
    }

    @Test
    public void addingProductWithBlankBrandNameThrowsApiException() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        productForm.setBrandName("   ");

        exceptionRule.expect(ApiException.class);
        String message = getBlankMessage("brand name");
        exceptionRule.expectMessage(message);

        productApiDto.add(productForm);
    }

    @Test
    public void addingProductWithBlankCategoryThrowsApiException() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        productForm.setCategory("   ");

        exceptionRule.expect(ApiException.class);
        String message = getBlankMessage("category");
        exceptionRule.expectMessage(message);
        productApiDto.add(productForm);
    }

    @Test
    public void addingProductWithNegativeBarcodeThrowsApiException() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        productForm.setPrice(-10.0);
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid input: price can only be a positive number!");
        productApiDto.add(productForm);
    }


    private String getBlankMessage(String field) {
        return "Invalid input: " + field + " can't be blank!";
    }

    @Test
    public void testAddValidForm() throws ApiException {
        ProductForm productForm = MockUtils.getMockProductForm();
        Product expected = productApiDto.add(productForm);
        Product actual = productService.get(expected.getId());
        AssertUtils.assertEqualProducts(expected, actual);
    }

    @Test
    public void getByBarcodeForValidBarcodeReturnsPojo() throws ApiException {
        ProductData expected = productDataList.get(0);
        ProductData actual = productApiDto.getByBarcode(expected.getBarcode());
        AssertUtils.assertEqualProductData(expected, actual);
    }

    @Test
    public void getByBarcodeForInvalidBarcodeThrowsException() throws ApiException {
        String barcode = "invalid_barcode";
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Can't find any product with barcode: " + barcode);
        productApiDto.getByBarcode(barcode);
    }

    @Test
    public void getAllReturnsAllProducts() {
        List<ProductData> actual = productApiDto.getAll();
        AssertUtils.assertEqualList(productDataList, actual, AssertUtils::assertEqualProductData);
    }

    @Test
    public void testUpdateForInvalidIdThrowsException() throws ApiException {
        ProductForm form = MockUtils.getMockProductForm();
        exceptionRule.expect(ApiException.class);
        int invalidId = -1;
        exceptionRule.expectMessage("Can't find any product with ID: " + invalidId);
        productApiDto.update(invalidId, form);
    }

}
