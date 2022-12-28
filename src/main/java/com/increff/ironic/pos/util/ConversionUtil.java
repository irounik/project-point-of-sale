package com.increff.ironic.pos.util;

import com.increff.ironic.pos.model.data.*;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.pojo.*;

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
        data.setPrice(orderItem.getSellingPrice());
        data.setName(product.getName());
        return data;
    }
}
