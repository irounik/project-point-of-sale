package com.increff.ironic.pos.utils;

import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.PerDaySale;
import org.junit.Assert;

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
        Assert.assertEquals(form.getBrand(), data.getBrand());
        Assert.assertEquals(form.getCategory(), data.getCategory());
    }

    public static void assertEqualInventoryData(InventoryData expected, InventoryData actual) {
        Assert.assertEquals(expected.getBarcode(), actual.getBarcode());
        Assert.assertEquals(expected.getProductName(), actual.getProductName());
        Assert.assertEquals(expected.getQuantity(), actual.getQuantity());
    }

    public interface AssertEqual<T> {

        void invoke(T expected, T actual);

    }

}
