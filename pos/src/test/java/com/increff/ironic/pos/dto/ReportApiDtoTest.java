package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.PerDaySale;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.utils.AssertUtils;
import com.increff.ironic.pos.utils.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

public class ReportApiDtoTest extends AbstractUnitTest {

    @Autowired
    private ReportApiDto reportApiDto;

    @Autowired
    private ReportService reportService;

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

    @Before
    public void setUp() throws ApiException {
        currentDate = LocalDateTime.now(ZoneOffset.UTC);
        List<Brand> brands = MockUtils.setUpBrands(brandService);
        List<Product> products = MockUtils.setUpProducts(brands, productService, inventoryService);
        MockUtils.setUpMockOrders(currentDate, orderService, orderItemService, products);
    }

    @Test
    @Rollback
    public void allInputsNull() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(null);
        form.setEndDate(null);
        form.setCategory(null);
        form.setBrandName(null);

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = reportService.getBrandWiseSaleReport(
                LocalDateTime.of(1000, 1, 1, 1, 1),
                LocalDateTime.now(ZoneOffset.UTC),
                ReportService.ALL_BRANDS,
                ReportService.ALL_CATEGORIES
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            AssertUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
    @Rollback
    public void getBrandWiseSaleReportWhenBrandNotSpecifiedGivesReportForAllBrands() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(currentDate.minusDays(10));
        form.setEndDate(currentDate);
        form.setCategory("");
        form.setBrandName("");

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = reportService.getBrandWiseSaleReport(
                currentDate.minusDays(10),
                currentDate,
                ReportService.ALL_BRANDS,
                ReportService.ALL_CATEGORIES
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            AssertUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
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
        List<SalesReportData> expected = reportService.getBrandWiseSaleReport(
                currentDate.minusDays(10),
                currentDate,
                "apple",
                ReportService.ALL_CATEGORIES
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            AssertUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
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
        List<SalesReportData> expected = reportService.getBrandWiseSaleReport(
                currentDate.minusDays(10),
                currentDate,
                ReportService.ALL_BRANDS,
                "phone"
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            AssertUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
    @Rollback
    public void testUpdatePerDaySale() {
        reportApiDto.updatePerDaySale();

        PerDaySaleForm form = new PerDaySaleForm();
        LocalDateTime startDate = currentDate.minusDays(1);
        LocalDateTime endDate = currentDate;

        form.setStartDate(currentDate.minusDays(1));
        form.setEndDate(currentDate);

        List<PerDaySale> perDaySales = reportService.getPerDaySale(startDate, endDate);

        Assert.assertEquals(1, perDaySales.size());

        PerDaySale sale = perDaySales.get(0);

        Assert.assertEquals(currentDate.minusDays(1).toLocalDate(), sale.getDate().toLocalDate());
        Assert.assertEquals(Integer.valueOf(2), sale.getUniqueItemCount());
        Assert.assertEquals(Integer.valueOf(1), sale.getOrderCount());
        Assert.assertEquals(Double.valueOf(435000.0), sale.getTotalRevenue());
    }

    @Test
    @Rollback
    public void getInventoryReportTest() throws ApiException {
        List<InventoryReportData> actualInventoryReport = reportApiDto.getInventoryReport();
        List<InventoryReportData> expectedInventoryReport = reportService.getInventoryReport();

        Assert.assertEquals(expectedInventoryReport.size(), actualInventoryReport.size());

        for (int i = 0; i < expectedInventoryReport.size(); i++) {
            AssertUtils.assertEqualInventoryReportDate(
                    expectedInventoryReport.get(i),
                    actualInventoryReport.get(i)
            );
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
        brands.sort(Comparator.comparing(Brand::getName));

        for (int i = 0; i < brandReportList.size(); i++) {
            BrandReportData actual = brandReportList.get(i);
            Brand expected = brands.get(i);

            Assert.assertEquals(expected.getName(), actual.getBrand());
            Assert.assertEquals(expected.getCategory(), actual.getCategory());
            Assert.assertEquals(expected.getId(), actual.getId());
        }
    }

    @Test
    @Rollback
    public void getPerDaySalesTest() {
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