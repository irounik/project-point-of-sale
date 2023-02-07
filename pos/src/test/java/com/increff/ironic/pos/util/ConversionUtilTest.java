package com.increff.ironic.pos.util;

import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.model.data.*;
import com.increff.ironic.pos.model.form.*;
import com.increff.ironic.pos.model.report.PerDaySaleData;
import com.increff.ironic.pos.pojo.*;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class ConversionUtilTest extends AbstractUnitTest {

    @Test
    public void convertBrandToBrandDataTest() {
        BrandPojo BrandPojo = new BrandPojo(1, "allen solly", "tshirts");
        BrandData data = ConversionUtil.convertPojoToData(BrandPojo);
        assertEquals("allen solly", data.getName());
        assertEquals("tshirts", data.getCategory());
    }

    @Test
    public void convertBrandFormToBrandTest() {
        BrandForm brandCategoryForm = new BrandForm("  allen solly", " tshirts   ");
        BrandPojo BrandPojo = ConversionUtil.convertFormToPojo(brandCategoryForm);
        assertEquals("  allen solly", BrandPojo.getBrand());
        assertEquals(" tshirts   ", BrandPojo.getCategory());
    }

    @Test
    public void convertToProductDataTest() {
        BrandPojo BrandPojo = new BrandPojo(1, "allen solly", "tshirts");
        ProductPojo productPojo = new ProductPojo(1, "a1110", 1, "polo tshirt", 2100.00);
        ProductData productData = ConversionUtil.convertPojoToData(productPojo, BrandPojo);
        assertEquals("polo tshirt", productData.getName());
        assertEquals("a1110", productData.getBarcode());
        assertEquals("tshirts", productData.getCategory());
        assertEquals("allen solly", productData.getBrandName());
        assertEquals((Double) 2100.00, productData.getPrice());
    }

    @Test
    public void convertToProductTest() {
        BrandPojo BrandPojo = new BrandPojo(1, "allen solly", "tshirts");
        ProductForm productForm = new ProductForm(
                "polo tshirt",
                "allen solly",
                "tshirts",
                2100.0,
                "a1110"
        );
        ProductPojo productPojo = ConversionUtil.convertFormToPojo(productForm, BrandPojo);
        assertEquals("polo tshirt", productPojo.getName());
        assertEquals("a1110", productPojo.getBarcode());
        assertEquals(Double.valueOf(2100.00), productPojo.getPrice());
        assertEquals(Integer.valueOf(1), productPojo.getBrandId());
    }

    @Test
    public void convertToInventoryDataTest() {
        ProductPojo productPojo = new ProductPojo(1, "a1110", 1, "polo tshirt", 2100.00);
        productPojo.setId(1);
        InventoryPojo inventoryPojo = new InventoryPojo(productPojo.getId(), 100);
        InventoryData inventoryData = ConversionUtil.convertPojoToData(inventoryPojo, productPojo);
        assertEquals((Integer) 100, inventoryData.getQuantity());
        assertEquals("a1110", inventoryData.getBarcode());
        assertEquals("polo tshirt", inventoryData.getProductName());
    }

    @Test
    public void convertToInventoryPojoTest() {
        ProductPojo productPojo = new ProductPojo(1, "a1110", 1, "polo tshirt", 2100.00);
        productPojo.setId(1);
        InventoryForm inventoryForm = new InventoryForm("a1110", 100);
        InventoryPojo inventoryPojo = ConversionUtil.convertFormToPojo(inventoryForm, productPojo);
        assertEquals((Integer) 1, inventoryPojo.getProductId());
        assertEquals((Integer) 100, inventoryPojo.getQuantity());
    }

    @Test
    public void convertToOrderItemDataFromOrderItemProductTest() {
        ProductPojo productPojo = new ProductPojo(1, "a1110", 1, "polo tshirt", 2100.00);
        productPojo.setId(1);
        OrderItemPojo orderItemPojo = new OrderItemPojo(1, 2, 3, 1500.0, 50);
        OrderItemData orderItemData = ConversionUtil.convertPojoToData(orderItemPojo, productPojo);
        assertEquals("a1110", orderItemData.getBarcode());
        assertEquals("polo tshirt", orderItemData.getName());
        assertEquals(Double.valueOf(1500.00), orderItemData.getSellingPrice());
        assertEquals(Integer.valueOf(50), orderItemData.getQuantity());
    }

    @Test
    public void convertToOrderItemTest() {
        ProductPojo productPojo = new ProductPojo(1, "a1110", 1, "polo tshirt", 2100.00);
        productPojo.setId(1);
        OrderItemForm orderItemForm = new OrderItemForm("a1110", 100, 1500.00);
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setId(1);
        OrderItemPojo orderItemPojo = ConversionUtil.convertFromToPojo(orderPojo.getId(), orderItemForm, productPojo);
        assertEquals((Integer) 1, orderItemPojo.getOrderId());
        assertEquals((Integer) 100, orderItemPojo.getQuantity());
        assertEquals((Integer) 1, orderItemPojo.getProductId());
        assertEquals((Double) 1500.00, orderItemPojo.getSellingPrice());
    }

    @Test
    public void convertToOrderData() {
        LocalDateTime time = LocalDateTime.now();
        OrderPojo orderPojo = new OrderPojo(10, time, "billPath");
        OrderData orderData = ConversionUtil.convertPojoToData(orderPojo);
        assertEquals(time, orderData.getTime());
    }

    @Test
    public void convertToDaySalesDataListTest() {
        LocalDateTime date = LocalDateTime.now();
        PerDaySalePojo perDaySalePojo = new PerDaySalePojo(1, date, 2, 3, 4, 100.0);
        PerDaySaleData perDaySaleData = ConversionUtil.convertPojoToData(perDaySalePojo);

        assertEquals(date.toLocalDate(), perDaySaleData.getDate());
        assertEquals(Integer.valueOf(2), perDaySaleData.getOrdersCount());
        assertEquals(Integer.valueOf(3), perDaySaleData.getItemsCount());
        assertEquals(Double.valueOf(100.00), perDaySaleData.getTotalRevenue());
    }

    @Test
    public void convertSignUpFormToUserTest() {
        UserForm signUpForm = new UserForm("xyz@increff.com", "Pass1234", "supervisor");
        UserPojo userPojoPojo = ConversionUtil.convertFormToPojo(signUpForm);
        assertEquals("xyz@increff.com", userPojoPojo.getEmail());
        assertEquals("Pass1234", userPojoPojo.getPassword());
    }

    @Test
    public void convertUserToUserDataTest() {
        UserPojo userPojoPojo = new UserPojo();
        userPojoPojo.setEmail("xyz@increff.com");
        userPojoPojo.setPassword("Pass1234");
        userPojoPojo.setRole(UserRole.OPERATOR);
        UserData userData = ConversionUtil.convertPojoToData(userPojoPojo);
        assertEquals("xyz@increff.com", userData.getEmail());
        assertEquals("operator", userData.getRole().toLowerCase());
    }

    @Test
    public void convertUserFormToUserTest() {
        UserForm userForm = new UserForm();
        userForm.setEmail("xyz@increff.com");
        userForm.setPassword("Pass1234");

        UserPojo userPojoPojo = ConversionUtil.convertFormToPojo(userForm);
        assertEquals("xyz@increff.com", userPojoPojo.getEmail());
        assertEquals("Pass1234", userPojoPojo.getPassword());
    }


    @Test
    public void testConvertBrandFormToBrandPojo() {
        BrandForm brandForm = new BrandForm("brand name", "Category");
        BrandPojo expected = new BrandPojo(null, "brand name", "Category");
        BrandPojo actual = ConversionUtil.convertFormToPojo(brandForm);
        AssertUtils.assertEqualBrands(expected, actual);
    }

}