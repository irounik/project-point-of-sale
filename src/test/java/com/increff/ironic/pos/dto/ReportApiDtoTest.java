package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.model.report.SalesReportForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    private LocalDateTime currentDate;

    @Before
    public void setUp() throws ApiException {
        currentDate = LocalDateTime.now(ZoneOffset.UTC);
        List<Brand> brands = TestUtils.setUpBrands(brandService);
        List<Product> products = TestUtils.setUpProducts(brands, productService, inventoryService);
        TestUtils.setUpOrders(currentDate, orderService, orderItemService, products);
    }

    @Test
    public void allInputsNull() throws ApiException {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(null);
        form.setEndDate(null);
        form.setCategory(null);
        form.setBrandName(null);

        List<SalesReportData> actual = reportApiDto.getSalesReport(form);
        List<SalesReportData> expected = reportService.getBrandWiseSaleReport(
                LocalDateTime.MIN,
                LocalDateTime.now(ZoneOffset.UTC),
                ReportService.ALL_BRANDS,
                ReportService.ALL_CATEGORIES
        );

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
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
            TestUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
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
            TestUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

    @Test
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
            TestUtils.assertEqualSaleReportData(expected.get(i), actual.get(i));
        }
    }

}