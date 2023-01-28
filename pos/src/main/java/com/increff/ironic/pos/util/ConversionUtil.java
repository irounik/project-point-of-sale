package com.increff.ironic.pos.util;

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

    public static InventoryData convertPojoToData(Inventory inventory, Product product) {
        InventoryData data = new InventoryData();

        data.setQuantity(inventory.getQuantity());
        data.setBarcode(product.getBarcode());
        data.setProductName(product.getName());

        return data;
    }

    public static Inventory convertFormToPojo(InventoryForm form, Product product) {
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(form.getQuantity());
        return inventory;
    }

    public static ProductData convertPojoToData(Product product, Brand brand) {
        ProductData data = new ProductData();

        data.setId(product.getId());
        data.setName(product.getName());
        data.setCategory(brand.getCategory());
        data.setBrandName(brand.getName());
        data.setPrice(product.getPrice());
        data.setBarcode(product.getBarcode());

        return data;
    }

    public static Product convertFormToPojo(ProductForm form, Brand brand) {
        Product product = new Product();

        product.setBrandId(brand.getId());
        product.setBarcode(form.getBarcode());
        product.setName(form.getName());
        product.setPrice(form.getPrice());

        return product;
    }

    public static Brand convertFormToPojo(BrandForm form) {
        Brand brand = new Brand();
        brand.setCategory(form.getCategory());
        brand.setName(form.getName());
        return brand;
    }

    public static BrandData convertPojoToData(Brand brand) {
        BrandData data = new BrandData();
        data.setCategory(brand.getCategory());
        data.setId(brand.getId());
        data.setName(brand.getName());
        return data;
    }

    public static OrderData convertPojoToData(Order order) {
        OrderData data = new OrderData();
        data.setTime(order.getTime());
        data.setId(order.getId());
        return data;
    }

    public static OrderItemData convertPojoToData(OrderItem orderItem, Product product) {
        OrderItemData data = new OrderItemData();
        data.setBarcode(product.getBarcode());
        data.setQuantity(orderItem.getQuantity());
        data.setSellingPrice(orderItem.getSellingPrice());
        data.setName(product.getName());
        return data;
    }

    public static User convertFormToPojo(UserForm userForm) {
        User user = new User();
        user.setEmail(userForm.getEmail());
        user.setRole(UserRole.getRole(userForm.getRole()));
        user.setPassword(userForm.getPassword());
        return user;
    }

    public static UserData convertPojoToData(User user) {
        UserData data = new UserData();
        data.setEmail(user.getEmail());
        data.setRole(user.getRole().toString());
        data.setId(user.getId());
        return data;
    }

    public static OrderItem convertPojoToData(Integer orderId, OrderItemForm form, Product product) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setSellingPrice(form.getSellingPrice());
        item.setQuantity(form.getQuantity());
        return item;
    }

    public static PerDaySaleData convertPojoToData(PerDaySale perDaySale) {
        return new PerDaySaleData(
                perDaySale.getDate().toLocalDate(),
                perDaySale.getOrderCount(),
                perDaySale.getUniqueItemCount(),
                perDaySale.getTotalRevenue()
        );
    }

    public static BrandReportData convertBrandToReport(Brand brand) {
        return new BrandReportData(
                brand.getId(),
                brand.getName(),
                brand.getCategory()
        );
    }

    public static Authentication convertToAuth(User user) {
        // Create principal
        UserPrincipal principal = new UserPrincipal();
        principal.setEmail(user.getEmail());
        principal.setId(user.getId());

        // Create Authorities
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString().toLowerCase()));
        // you can add more roles if required
        // Create Authentication
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

}
