package com.increff.ironic.pos.util;

import com.increff.ironic.pos.model.auth.UserPrincipal;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.model.data.*;
import com.increff.ironic.pos.model.form.*;
import com.increff.ironic.pos.model.report.BrandReportData;
import com.increff.ironic.pos.model.report.PerDaySaleData;
import com.increff.ironic.pos.pojo.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;

public class ConversionUtil {

    public static InventoryData convertPojoToData(InventoryPojo inventoryPojo, ProductPojo productPojo) {
        InventoryData data = new InventoryData();

        data.setId(inventoryPojo.getId());
        data.setQuantity(inventoryPojo.getQuantity());
        data.setBarcode(productPojo.getBarcode());
        data.setProductName(productPojo.getName());

        return data;
    }

    public static InventoryPojo convertFormToPojo(InventoryForm form, ProductPojo productPojo) {
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId(productPojo.getId());
        inventoryPojo.setQuantity(form.getQuantity());
        return inventoryPojo;
    }

    public static ProductData convertPojoToData(ProductPojo product, BrandPojo brand) {
        ProductData data = new ProductData();

        data.setId(product.getId());
        data.setName(product.getName());
        data.setCategory(brand.getCategory());
        data.setBrandName(brand.getBrand());
        data.setPrice(product.getPrice());
        data.setBarcode(product.getBarcode());

        return data;
    }

    public static ProductPojo convertFormToPojo(ProductForm form, BrandPojo brandPojo) {
        ProductPojo productPojo = new ProductPojo();

        productPojo.setBrandId(brandPojo.getId());
        productPojo.setBarcode(form.getBarcode());
        productPojo.setName(form.getName());
        productPojo.setPrice(form.getPrice());

        return productPojo;
    }

    public static BrandPojo convertFormToPojo(BrandForm form) {
        BrandPojo brandPojo = new BrandPojo();
        brandPojo.setCategory(form.getCategory());
        brandPojo.setBrand(form.getName());
        return brandPojo;
    }

    public static BrandData convertPojoToData(BrandPojo brandPojo) {
        BrandData data = new BrandData();
        data.setCategory(brandPojo.getCategory());
        data.setId(brandPojo.getId());
        data.setName(brandPojo.getBrand());
        return data;
    }

    public static OrderData convertPojoToData(OrderPojo orderPojo) {
        OrderData data = new OrderData();
        data.setTime(orderPojo.getTime());
        data.setId(orderPojo.getId());
        return data;
    }

    public static OrderItemData convertPojoToData(OrderItemPojo orderItemPojo, ProductPojo productPojo) {
        OrderItemData data = new OrderItemData();
        data.setProductId(productPojo.getId());
        data.setBarcode(productPojo.getBarcode());
        data.setQuantity(orderItemPojo.getQuantity());
        data.setSellingPrice(orderItemPojo.getSellingPrice());
        data.setName(productPojo.getName());
        return data;
    }

    public static UserPojo convertFormToPojo(UserForm userForm) {
        UserPojo userPojo = new UserPojo();
        userPojo.setEmail(userForm.getEmail());
        userPojo.setRole(UserRole.getRole(userForm.getRole()));
        userPojo.setPassword(userForm.getPassword());
        return userPojo;
    }

    public static UserData convertPojoToData(UserPojo userPojo) {
        UserData data = new UserData();
        data.setEmail(userPojo.getEmail());
        data.setRole(userPojo.getRole().toString());
        data.setId(userPojo.getId());
        return data;
    }

    public static OrderItemPojo convertFromToPojo(Integer orderId, OrderItemForm form, ProductPojo productPojo) {
        OrderItemPojo item = new OrderItemPojo();
        item.setOrderId(orderId);
        item.setProductId(productPojo.getId());
        item.setSellingPrice(form.getSellingPrice());
        item.setQuantity(form.getQuantity());
        return item;
    }

    public static PerDaySaleData convertPojoToData(PerDaySalePojo perDaySalePojo) {
        return new PerDaySaleData(
                perDaySalePojo.getDate().toLocalDate(),
                perDaySalePojo.getOrderCount(),
                perDaySalePojo.getUniqueItemCount(),
                perDaySalePojo.getTotalRevenue()
        );
    }

    public static BrandReportData convertBrandToReport(BrandPojo brandPojo) {
        return new BrandReportData(
                brandPojo.getId(),
                brandPojo.getBrand(),
                brandPojo.getCategory()
        );
    }

    public static Authentication convertToAuth(UserPojo userPojo) {
        // Create principal
        UserPrincipal principal = new UserPrincipal();
        principal.setEmail(userPojo.getEmail());
        principal.setId(userPojo.getId());

        // Create Authorities
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userPojo.getRole().toString().toLowerCase()));
        // you can add more roles if required
        // Create Authentication
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    public static UserPojo convertFormToPojo(SignUpForm signUpForm) {
        UserPojo userPojo = new UserPojo();
        userPojo.setEmail(signUpForm.getEmail());
        userPojo.setPassword(signUpForm.getPassword());
        userPojo.setRole(UserRole.NONE);
        return userPojo;
    }

}
