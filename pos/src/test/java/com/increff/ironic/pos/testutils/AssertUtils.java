package com.increff.ironic.pos.testutils;

import com.increff.ironic.pos.model.data.*;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.pojo.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AssertUtils {

    public static void assertEqualSaleReportData(SalesReportData expectedReport, SalesReportData actualReport) {
        assertEquals(expectedReport.getQuantity(), actualReport.getQuantity());
        assertEquals(expectedReport.getRevenue(), actualReport.getRevenue());
        assertEquals(expectedReport.getBrandName(), actualReport.getBrandName());
        assertEquals(expectedReport.getCategory(), actualReport.getCategory());
    }

    public static void assertEqualInventoryReportDate(
            InventoryReportData expectedInventoryReportData,
            InventoryReportData actualInventoryReportData) {

        assertEquals(expectedInventoryReportData.getBrand(), actualInventoryReportData.getBrand());
        assertEquals(expectedInventoryReportData.getCategory(), actualInventoryReportData.getCategory());
        assertEquals(expectedInventoryReportData.getQuantity(), actualInventoryReportData.getQuantity());
    }

    public static void assertEqualPerDaySale(PerDaySale expectedItem, PerDaySaleData actualItem) {
        assertEquals(expectedItem.getDate().toLocalDate(), actualItem.getDate());
        assertEquals(expectedItem.getOrderCount(), actualItem.getOrdersCount());
        assertEquals(expectedItem.getUniqueItemCount(), actualItem.getItemsCount());
        assertEquals(expectedItem.getTotalRevenue(), actualItem.getTotalRevenue());
    }

    public static void assertEqualPerDaySale(PerDaySale expectedItem, PerDaySale actualItem) {
        assertEquals(expectedItem.getDate(), actualItem.getDate());
        assertEquals(expectedItem.getOrderCount(), actualItem.getOrderCount());
        assertEquals(expectedItem.getUniqueItemCount(), actualItem.getUniqueItemCount());
        assertEquals(expectedItem.getTotalRevenue(), actualItem.getTotalRevenue());
    }

    public static void assertEqualBrands(Brand expected, Brand actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getCategory(), actual.getCategory());
    }

    public static void assertEqualBrandData(BrandData expected, BrandData actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCategory(), actual.getCategory());
        assertEquals(expected.getName(), actual.getName());
    }

    public static void assertEqualInventory(Inventory expected, Inventory actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getProductId(), actual.getProductId());
    }

    public static <T> void assertEqualList(
            List<T> expectedList,
            List<T> actualList,
            AssertEqual<T> assertEqual) {

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            T expected = expectedList.get(i);
            T actual = actualList.get(i);
            assertEqual.invoke(expected, actual);
        }
    }

    public static void assertEqualBrandReport(BrandCategoryFrom form, BrandReportData data) {
        assertEquals(form.getBrand(), data.getBrand());
        assertEquals(form.getCategory(), data.getCategory());
    }

    public static void assertEqualInventoryData(InventoryData expected, InventoryData actual) {
        assertEquals(expected.getBarcode(), actual.getBarcode());
        assertEquals(expected.getProductName(), actual.getProductName());
        assertEquals(expected.getQuantity(), actual.getQuantity());
    }

    public static void assertEqualProducts(Product expected, Product actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBrandId(), actual.getBrandId());
        assertEquals(expected.getBarcode(), actual.getBarcode());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getName(), actual.getName());
    }

    public static void assertEqualProductData(ProductData expected, ProductData actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBrandName(), actual.getBrandName());
        assertEquals(expected.getBarcode(), actual.getBarcode());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getCategory(), actual.getCategory());
    }

    public static void assertEqualOrder(Order expected, Order actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTime(), actual.getTime());
        assertEquals(expected.getInvoicePath(), actual.getInvoicePath());
    }

    public static void assertEqualOrderItems(OrderItem expected, OrderItem actual) {
        assertEquals(expected.getOrderId(), actual.getOrderId());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getSellingPrice(), actual.getSellingPrice());
        assertEquals(expected.getProductId(), actual.getProductId());
    }

    public static void assertEqualUsers(User expected, User actual) {
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getRole(), actual.getRole());
        assertEquals(expected.getId(), actual.getId());
    }

    public static void assertEqualOrderItemData(OrderItemData expected, OrderItemData actual) {
        assertEquals(expected.getBarcode(), actual.getBarcode());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getQuantity(), actual.getQuantity());
        assertEquals(expected.getSellingPrice(), actual.getSellingPrice());
    }

    public static void assertEqualOrderData(OrderData expected, OrderData actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTime(), actual.getTime());
    }

    public interface AssertEqual<T> {

        void invoke(T expected, T actual);

    }

}
