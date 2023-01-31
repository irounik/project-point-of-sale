package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;

public class BrandServiceTest extends AbstractUnitTest {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private BrandService brandService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        MockUtils.setUpBrands(brandService);
    }

    private Brand addMockBrand() {
        Brand mockBrand = MockUtils.getMockBrand();
        brandDao.insert(mockBrand);
        return mockBrand;
    }

    @Test
    @Rollback
    public void getBrandByIdForUnknownIdThrowsApiException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int id = -1;
        exceptionRule.expectMessage("No brand found for ID: " + id);
        brandService.get(id);
    }

    @Test
    @Rollback
    public void getBrandByIdForValidIdReturnsBrand() throws ApiException {
        Brand brand = MockUtils.getMockBrand();
        brandDao.insert(brand);

        Brand actual = brandService.get(brand.getId());
        AssertUtils.assertEqualBrands(brand, actual);
    }

    @Test
    @Rollback
    public void getAll() {
        List<Brand> actual = brandService.getAll();
        List<Brand> expected = MockUtils.BRANDS;
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualBrands);
    }

    @Test
    @Rollback
    public void addValidBrandReturnsAddedPojo() throws ApiException {
        Brand brand = MockUtils.getMockBrand();
        Brand actual = brandService.add(brand);
        Brand expected = brandDao.select(brand.getId());
        AssertUtils.assertEqualBrands(expected, actual);
    }

    @Test
    @Rollback
    public void addingDuplicateBrandThrowsApiException() throws ApiException {
        Brand originalBrand = new Brand(null, "mock_brand", "mock_category");
        brandDao.insert(originalBrand);

        exceptionRule.expect(ApiException.class);
        String message = "Brand with name " + originalBrand.getBrand() + " and category " + originalBrand.getCategory() + " already exists!";
        exceptionRule.expectMessage(message);

        Brand duplicateBrand = new Brand(null, "mock_brand", "mock_category");
        brandService.add(duplicateBrand);
    }

    @Test
    @Rollback
    public void update() throws ApiException {
        Brand brand = addMockBrand();
        int id = brand.getId();

        brand = new Brand();
        brand.setBrand("updated name 1");
        brand.setCategory("updated category 2");
        brand.setId(id);

        Brand actual = brandService.update(brand);
        Brand expected = brandDao.select(id);

        AssertUtils.assertEqualBrands(expected, actual);
    }

    @Test
    @Rollback
    public void testSelectByNameAndCategory() throws ApiException {
        Brand expected = addMockBrand();
        Brand actual = brandService.selectByNameAndCategory(expected.getBrand(), expected.getCategory());
        AssertUtils.assertEqualBrands(expected, actual);
    }

    @Test
    @Rollback
    public void selectByNameAndCategoryForUnknownAttrThrowsException() throws ApiException {
        String name = "Unknown-Brand";
        String category = "Unknown-Category";

        exceptionRule.expect(ApiException.class);

        String message = "No brand found for name " + name + " and category " + category;
        exceptionRule.expectMessage(message);

        brandService.selectByNameAndCategory(name, category);
    }

    @Test
    @Rollback
    public void duplicateCheckForDuplicateBrandsThrowsException() throws ApiException {
        Brand brand = addMockBrand();
        exceptionRule.expect(ApiException.class);
        String message = "Brand with name " + brand.getBrand() + " and category " + brand.getCategory() + " already exists!";
        exceptionRule.expectMessage(message);
        brandService.duplicateCheck(brand);
    }

    @Test
    @Rollback
    public void testDuplicateCheckForNewBrand() throws ApiException {
        Brand brand = MockUtils.getMockBrand();
        brandService.duplicateCheck(brand); // No error
    }

}