package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.BrandDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.BrandPojo;
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

public class BrandEntityServiceTest extends AbstractUnitTest {

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

    private BrandPojo addMockBrand() {
        BrandPojo mockBrandPojo = MockUtils.getMockBrand();
        brandDao.insert(mockBrandPojo);
        return mockBrandPojo;
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
        BrandPojo brandPojo = MockUtils.getMockBrand();
        brandDao.insert(brandPojo);

        BrandPojo actual = brandService.get(brandPojo.getId());
        AssertUtils.assertEqualBrands(brandPojo, actual);
    }

    @Test
    @Rollback
    public void getAll() {
        List<BrandPojo> actual = brandService.getAll();
        List<BrandPojo> expected = MockUtils.BRAND_POJOS;
        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualBrands);
    }

    @Test
    @Rollback
    public void addValidBrandReturnsAddedPojo() throws ApiException {
        BrandPojo brandPojo = MockUtils.getMockBrand();
        BrandPojo actual = brandService.add(brandPojo);
        BrandPojo expected = brandDao.select(brandPojo.getId());
        AssertUtils.assertEqualBrands(expected, actual);
    }

    @Test
    @Rollback
    public void addingDuplicateBrandThrowsApiException() throws ApiException {
        BrandPojo originalBrandPojo = new BrandPojo(null, "mock_brand", "mock_category");
        brandDao.insert(originalBrandPojo);

        exceptionRule.expect(ApiException.class);
        String message = "Brand with name " + originalBrandPojo.getBrand() + " and category " + originalBrandPojo.getCategory() + " already exists!";
        exceptionRule.expectMessage(message);

        BrandPojo duplicateBrandPojo = new BrandPojo(null, "mock_brand", "mock_category");
        brandService.add(duplicateBrandPojo);
    }

    @Test
    @Rollback
    public void update() throws ApiException {
        BrandPojo brandPojo = addMockBrand();
        int id = brandPojo.getId();

        brandPojo = new BrandPojo();
        brandPojo.setBrand("updated name 1");
        brandPojo.setCategory("updated category 2");
        brandPojo.setId(id);

        BrandPojo actual = brandService.update(brandPojo);
        BrandPojo expected = brandDao.select(id);

        AssertUtils.assertEqualBrands(expected, actual);
    }

    @Test
    @Rollback
    public void testSelectByNameAndCategory() throws ApiException {
        BrandPojo expected = addMockBrand();
        BrandPojo actual = brandService.selectByNameAndCategory(expected.getBrand(), expected.getCategory());
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
        BrandPojo brandPojo = addMockBrand();
        exceptionRule.expect(ApiException.class);
        String message = "Brand with name " + brandPojo.getBrand() + " and category " + brandPojo.getCategory() + " already exists!";
        exceptionRule.expectMessage(message);
        brandService.duplicateCheck(brandPojo);
    }

    @Test
    @Rollback
    public void testDuplicateCheckForNewBrand() throws ApiException {
        BrandPojo brandPojo = MockUtils.getMockBrand();
        brandService.duplicateCheck(brandPojo); // No error
    }

}