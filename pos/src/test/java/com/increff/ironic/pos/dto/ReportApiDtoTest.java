package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.PerDaySale;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReportApiDtoTest extends AbstractUnitTest {

    @Autowired
    private ReportApiDto reportApiDto;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private PerDaySaleDao perDaySaleDao;

    private LocalDateTime currentDate;

    private List<SalesReportData> allPerDaySales;

    @Before
    public void setUp() throws ApiException {
        currentDate = MockUtils.currentDate;
        List<Brand> brands = MockUtils.setUpBrands(brandService);
        List<Product> products = MockUtils.setupProducts(brands, productService);
        List<Integer> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        MockUtils.setUpInventory(productIds, inventoryService);
        MockUtils.setUpMockOrders(orderService, orderItemService, inventoryService, products);
        allPerDaySales = Arrays.asList(
                new SalesReportData("phone", "apple", 5, 540000.0),
                new SalesReportData("laptop", "apple", 1, 250000.0),
                new SalesReportData("phone", "samsung", 2, 310000.0),
                new SalesReportData("shoe", "nike", 3, 60000.0),
                new SalesReportData("laptop", "lenovo", 3, 195000.0));
    }

    @Test
    @Rollback
    public void allInputsNullReturnsAllCategories() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(null);
        form.setEndDate(null);
        form.setCategory(null);
        form.setBrandName(null);

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = allPerDaySales;

        Comparator<SalesReportData> comparator = Comparator.comparing(SalesReportData::getBrandName)
                .thenComparing(SalesReportData::getCategory);
        actual.sort(comparator);
        expected.sort(comparator);

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualSaleReportData);
    }

    @Test
    @Rollback
    public void getBrandWiseSaleReportWhenBrandNotSpecifiedGivesReportForAllBrands() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(currentDate.minusDays(2));
        form.setEndDate(currentDate);
        form.setCategory("");
        form.setBrandName("");

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = Arrays.asList(
                new SalesReportData("phone", "apple", 5, 540000.0),
                new SalesReportData("laptop", "apple", 1, 250000.0),
                new SalesReportData("phone", "samsung", 2, 310000.0),
                new SalesReportData("shoe", "nike", 3, 60000.0),
                new SalesReportData("laptop", "lenovo", 3, 195000.0)
        );

        Comparator<SalesReportData> comparator = Comparator.comparing(SalesReportData::getBrandName)
                .thenComparing(SalesReportData::getCategory);
        actual.sort(comparator);
        expected.sort(comparator);

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualSaleReportData);
    }

    @Test
    @Rollback
    public void getBrandWiseSaleReportWhenOnlyBrandIsSpecifiedGivesReportForBrandsWithAllCategories() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(currentDate.minusDays(10));
        form.setEndDate(currentDate);
        form.setBrandName("apple");
        form.setCategory("");

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = Arrays.asList(
                new SalesReportData("phone", "apple", 5, 540000.0),
                new SalesReportData("laptop", "apple", 1, 250000.0)
        );

        AssertUtils.assertEqualList(expected, actual, AssertUtils::assertEqualSaleReportData);
    }

    @Test(expected = ApiException.class)
    @Rollback
    public void getSalesReportThrowsApiExceptionForInvalidDates() throws ApiException {
        SalesReportForm salesReportForm = new SalesReportForm();

        salesReportForm.setStartDate(currentDate);
        salesReportForm.setEndDate(currentDate.minusDays(1));

        reportApiDto.getSalesReport(salesReportForm);
    }

    @Test
    @Rollback
    public void getBrandWiseSaleReportIfOnlyCategoryIsSpecifiedGivesReportForAllBrandsWithCategory() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(currentDate.minusDays(10));
        form.setEndDate(currentDate);
        form.setBrandName("");
        form.setCategory("phone");

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = Arrays.asList(
                new SalesReportData("phone", "apple", 5, 540000.0),
                new SalesReportData("phone", "samsung", 2, 310000.0)
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            AssertUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
    @Rollback
    public void testUpdatePerDaySale() throws ApiException {
        reportApiDto.updatePerDaySale();

        PerDaySaleForm form = new PerDaySaleForm();
        LocalDateTime startDate = currentDate.minusDays(1);
        LocalDateTime endDate = currentDate;

        form.setStartDate(startDate);
        form.setEndDate(endDate);

        List<PerDaySaleData> perDaySales = reportApiDto.getPerDaySales(form);

        Assert.assertEquals(1, perDaySales.size());

        PerDaySaleData sale = perDaySales.get(0);

        Assert.assertEquals(currentDate.minusDays(1).toLocalDate(), sale.getDate());
        Assert.assertEquals(Integer.valueOf(2), sale.getItemsCount());
        Assert.assertEquals(Integer.valueOf(1), sale.getOrdersCount());
        Assert.assertEquals(Double.valueOf(435000.0), sale.getTotalRevenue());
    }

    @Test
    @Rollback
    public void getInventoryReportTest() throws ApiException {
        List<InventoryReportData> actualInventoryReport = reportApiDto.getInventoryReport(new BrandCategoryFrom());
        List<InventoryReportData> expectedInventoryReport = Arrays.asList(
                new InventoryReportData("apple", "laptop", 9),
                new InventoryReportData("apple", "phone", 15),
                new InventoryReportData("lenovo", "laptop", 7),
                new InventoryReportData("nike", "shoe", 7),
                new InventoryReportData("samsung", "phone", 18)
        );

        Assert.assertEquals(expectedInventoryReport.size(), actualInventoryReport.size());

        for (int i = 0; i < expectedInventoryReport.size(); i++) {
            AssertUtils.assertEqualInventoryReportDate(expectedInventoryReport.get(i), actualInventoryReport.get(i));
        }
    }

    @Test
    @Rollback
    public void getBrandReportForNullBrandNameAndCategoryReturnsAllBrands() {
        BrandCategoryFrom brandCategoryFrom = new BrandCategoryFrom();

        List<BrandReportData> brandReportList = reportApiDto.getBrandReport(brandCategoryFrom);
        List<Brand> brands = brandService.getAll();

        Assert.assertEquals(brands.size(), brandReportList.size());

        brandReportList.sort(Comparator.comparing(BrandReportData::getBrand));
        brands.sort(Comparator.comparing(Brand::getBrand));

        for (int i = 0; i < brandReportList.size(); i++) {
            BrandReportData actual = brandReportList.get(i);
            Brand expected = brands.get(i);

            Assert.assertEquals(expected.getBrand(), actual.getBrand());
            Assert.assertEquals(expected.getCategory(), actual.getCategory());
            Assert.assertEquals(expected.getId(), actual.getId());
        }
    }

    @Test
    @Rollback
    public void getBrandReportForSpecificBrand() {
        BrandCategoryFrom form = new BrandCategoryFrom();
        form.setBrand("apple");
        form.setCategory("phone");

        List<BrandReportData> brandReportData = reportApiDto.getBrandReport(form);
        Assert.assertEquals(1, brandReportData.size());

        BrandReportData data = brandReportData.get(0);
        AssertUtils.assertEqualBrandReport(form, data);
    }

    @Test
    @Rollback
    public void getPerDaySalesTest() throws ApiException {
        int size = 4;
        List<PerDaySale> expected = MockUtils.getMockPerDaySales(size, currentDate);
        expected.forEach(perDaySaleDao::insert);

        PerDaySaleForm form = new PerDaySaleForm();
        List<PerDaySaleData> actual = reportApiDto.getPerDaySales(form);

        Assert.assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            PerDaySale expectedItem = expected.get(i);
            PerDaySaleData actualItem = actual.get(i);
            AssertUtils.assertEqualPerDaySale(expectedItem, actualItem);
        }
    }

}