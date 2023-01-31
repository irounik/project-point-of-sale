package com.increff.ironic.pos.util;

import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import org.junit.Test;

public class ConversionUtilTest extends AbstractUnitTest {

    @Test
    public void testConvertBrandFormToBrandPojo() {
        BrandForm brandForm = new BrandForm("brand name", "Category");
        Brand expected = new Brand(null, "brand name", "Category");
        Brand actual = ConversionUtil.convertFormToPojo(brandForm);
        AssertUtils.assertEqualBrands(expected, actual);
    }

}