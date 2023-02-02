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
        Brand Brand = new Brand(1, "allen solly", "tshirts");
        BrandData data = ConversionUtil.convertPojoToData(Brand);
        assertEquals("allen solly", data.getName());
        assertEquals("tshirts", data.getCategory());
    }

    @Test
    public void convertBrandFormToBrandTest() {
        BrandForm brandCategoryForm = new BrandForm("  allen solly", " tshirts   ");
        Brand Brand = ConversionUtil.convertFormToPojo(brandCategoryForm);
        assertEquals("  allen solly", Brand.getBrand());
        assertEquals(" tshirts   ", Brand.getCategory());
    }

    @Test
    public void convertToProductDataTest() {
        Brand Brand = new Brand(1, "allen solly", "tshirts");
        Product product = new Product(1, "a1110", 1, "polo tshirt", 2100.00);
        ProductData productData = ConversionUtil.convertPojoToData(product, Brand);
        assertEquals("polo tshirt", productData.getName());
        assertEquals("a1110", productData.getBarcode());
        assertEquals("tshirts", productData.getCategory());
        assertEquals("allen solly", productData.getBrandName());
        assertEquals((Double) 2100.00, productData.getPrice());
    }

    @Test
    public void convertToProductTest() {
        Brand Brand = new Brand(1, "allen solly", "tshirts");
        ProductForm productForm = new ProductForm(
                "polo tshirt",
                "allen solly",
                "tshirts",
                2100.0,
                "a1110"
        );
        Product product = ConversionUtil.convertFormToPojo(productForm, Brand);
        assertEquals("polo tshirt", product.getName());
        assertEquals("a1110", product.getBarcode());
        assertEquals(Double.valueOf(2100.00), product.getPrice());
        assertEquals(Integer.valueOf(1), product.getBrandId());
    }

    @Test
    public void convertToInventoryDataTest() {
        Product product = new Product(1, "a1110", 1, "polo tshirt", 2100.00);
        product.setId(1);
        Inventory inventoryPojo = new Inventory(product.getId(), 100);
        InventoryData inventoryData = ConversionUtil.convertPojoToData(inventoryPojo, product);
        assertEquals((Integer) 100, inventoryData.getQuantity());
        assertEquals("a1110", inventoryData.getBarcode());
        assertEquals("polo tshirt", inventoryData.getProductName());
    }

    @Test
    public void convertToInventoryPojoTest() {
        Product product = new Product(1, "a1110", 1, "polo tshirt", 2100.00);
        product.setId(1);
        InventoryForm inventoryForm = new InventoryForm("a1110", 100);
        Inventory inventoryPojo = ConversionUtil.convertFormToPojo(inventoryForm, product);
        assertEquals((Integer) 1, inventoryPojo.getProductId());
        assertEquals((Integer) 100, inventoryPojo.getQuantity());
    }

    @Test
    public void convertToOrderItemDataFromOrderItemProductTest() {
        Product product = new Product(1, "a1110", 1, "polo tshirt", 2100.00);
        product.setId(1);
        OrderItem orderItemPojo = new OrderItem(1, 2, 3, 1500.0, 50);
        OrderItemData orderItemData = ConversionUtil.convertPojoToData(orderItemPojo, product);
        assertEquals("a1110", orderItemData.getBarcode());
        assertEquals("polo tshirt", orderItemData.getName());
        assertEquals(Double.valueOf(1500.00), orderItemData.getSellingPrice());
        assertEquals(Integer.valueOf(50), orderItemData.getQuantity());
    }

    @Test
    public void convertToOrderItemTest() {
        Product product = new Product(1, "a1110", 1, "polo tshirt", 2100.00);
        product.setId(1);
        OrderItemForm orderItemForm = new OrderItemForm("a1110", 100, 1500.00);
        Order order = new Order();
        order.setId(1);
        OrderItem orderItemPojo = ConversionUtil.convertFromToPojo(order.getId(), orderItemForm, product);
        assertEquals((Integer) 1, orderItemPojo.getOrderId());
        assertEquals((Integer) 100, orderItemPojo.getQuantity());
        assertEquals((Integer) 1, orderItemPojo.getProductId());
        assertEquals((Double) 1500.00, orderItemPojo.getSellingPrice());
    }

    @Test
    public void convertToOrderData() {
        LocalDateTime time = LocalDateTime.now();
        Order order = new Order(10, time, "billPath");
        OrderData orderData = ConversionUtil.convertPojoToData(order);
        assertEquals(time, orderData.getTime());
    }

    @Test
    public void convertToDaySalesDataListTest() {
        LocalDateTime date = LocalDateTime.now();
        PerDaySale perDaySale = new PerDaySale(1, date, 2, 3, 4, 100.0);
        PerDaySaleData perDaySaleData = ConversionUtil.convertPojoToData(perDaySale);

        assertEquals(date.toLocalDate(), perDaySaleData.getDate());
        assertEquals(Integer.valueOf(2), perDaySaleData.getOrdersCount());
        assertEquals(Integer.valueOf(3), perDaySaleData.getItemsCount());
        assertEquals(Double.valueOf(100.00), perDaySaleData.getTotalRevenue());
    }

    @Test
    public void convertSignUpFormToUserTest() {
        UserForm signUpForm = new UserForm("xyz@increff.com", "Pass1234", "supervisor");
        User userPojo = ConversionUtil.convertFormToPojo(signUpForm);
        assertEquals("xyz@increff.com", userPojo.getEmail());
        assertEquals("Pass1234", userPojo.getPassword());
    }

    @Test
    public void convertUserToUserDataTest() {
        User userPojo = new User();
        userPojo.setEmail("xyz@increff.com");
        userPojo.setPassword("Pass1234");
        userPojo.setRole(UserRole.OPERATOR);
        UserData userData = ConversionUtil.convertPojoToData(userPojo);
        assertEquals("xyz@increff.com", userData.getEmail());
        assertEquals("operator", userData.getRole().toLowerCase());
    }

    @Test
    public void convertUserFormToUserTest() {
        UserForm userForm = new UserForm();
        userForm.setEmail("xyz@increff.com");
        userForm.setPassword("Pass1234");

        User userPojo = ConversionUtil.convertFormToPojo(userForm);
        assertEquals("xyz@increff.com", userPojo.getEmail());
        assertEquals("Pass1234", userPojo.getPassword());
    }


    @Test
    public void testConvertBrandFormToBrandPojo() {
        BrandForm brandForm = new BrandForm("brand name", "Category");
        Brand expected = new Brand(null, "brand name", "Category");
        Brand actual = ConversionUtil.convertFormToPojo(brandForm);
        AssertUtils.assertEqualBrands(expected, actual);
    }

}