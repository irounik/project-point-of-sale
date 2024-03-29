package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.BrandPojo;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import com.increff.ironic.pos.util.ConversionUtil;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BrandApiDtoTest extends AbstractUnitTest {

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandApiDto brandApiDto;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    @Rollback
    public void addBrandTestWithValidInput() throws ApiException {
        BrandForm brandForm = new BrandForm();
        brandForm.setCategory("Category");
        brandForm.setName("Name");

        BrandData actual = brandApiDto.add(brandForm);
        BrandData expected = new BrandData();
        expected.setName("name");
        expected.setCategory("category");
        expected.setId(actual.getId());

        AssertUtils.assertEqualBrandData(expected, actual);
    }

    @Test
    @Rollback
    public void addBrandThrowsExceptionForBlankCategory() throws ApiException {
        BrandForm brandForm = new BrandForm("Brand", "   ");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid input: category can't be blank!");

        brandApiDto.add(brandForm);
    }

    @Test
    @Rollback
    public void addBrandThrowsExceptionForBlankName() throws ApiException {
        BrandForm brandForm = new BrandForm(null, "Category");

        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("Invalid input: brand name can't be blank");

        brandApiDto.add(brandForm);
    }

    @Test
    @Rollback
    public void addBrandThrows() throws ApiException {
        BrandForm brandForm = new BrandForm();
        brandForm.setName("Name");
        brandForm.setCategory("");

        exceptionRule.expect(ApiException.class);
        String message = "Invalid input: category can't be blank!";
        exceptionRule.expectMessage(message);
        brandApiDto.add(brandForm);
    }

    @Test
    @Rollback
    public void getByValidId() throws ApiException {
        BrandPojo brandPojo = MockUtils.getMockBrand();
        brandService.add(brandPojo);

        int id = brandPojo.getId();
        BrandData actual = brandApiDto.get(id);
        BrandData expected = ConversionUtil.convertPojoToData(brandPojo);

        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getCategory(), actual.getCategory());
    }

    @Test
    @Rollback
    public void getByIdForInvalidIdThrowsApiException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int id = -1;
        exceptionRule.expectMessage("No brand found for ID: " + id);
        brandApiDto.get(id);
    }

    @Test
    @Rollback
    public void getAll() throws ApiException {
        List<BrandData> expectedBrandDataList = new LinkedList<>();

        for (BrandPojo brandPojo : MockUtils.BRAND_POJOS) {
            brandService.add(brandPojo);
            expectedBrandDataList.add(ConversionUtil.convertPojoToData(brandPojo));
        }

        List<BrandData> actualBrandDataList = brandApiDto.getAll();
        actualBrandDataList.sort(Comparator.comparing(BrandData::getId));
        expectedBrandDataList.sort(Comparator.comparing(BrandData::getId));

        AssertUtils.assertEqualList(expectedBrandDataList, actualBrandDataList, AssertUtils::assertEqualBrandData);
    }

    @Test
    @Rollback
    public void updateBrandWithValidInputs() throws ApiException {
        BrandPojo brandPojo = MockUtils.getMockBrand();
        brandService.add(brandPojo);

        BrandForm brandForm = new BrandForm("New Name", "New Category");
        BrandData actual = brandApiDto.update(brandPojo.getId(), brandForm);
        BrandData expected = new BrandData(brandPojo.getId());

        expected.setName("new name");
        expected.setCategory("new category");
        AssertUtils.assertEqualBrandData(expected, actual);
    }

}