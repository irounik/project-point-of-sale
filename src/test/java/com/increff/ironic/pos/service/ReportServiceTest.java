package com.increff.ironic.pos.service;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.utils.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.increff.ironic.pos.utils.TestUtils.assertEqualSaleReportData;

public class ReportServiceTest extends AbstractUnitTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private InventoryService inventoryService;

    private LocalDateTime currentDate;

    @Before
    public void setUp() throws ApiException {
        currentDate = LocalDateTime.now(ZoneOffset.UTC);
        List<Brand> brands = TestUtils.setUpBrands(brandService);
        List<Product> products = TestUtils.setUpProducts(brands, productService, inventoryService);
        TestUtils.setUpOrders(currentDate, orderService, orderItemService, products);
    }
    
    @Test
    public void getSaleReportForAllSales() throws ApiException {
        LocalDateTime startDate = currentDate.minusDays(5), endDate = currentDate;
        String brandName = ReportService.ALL_BRANDS, category = ReportService.ALL_CATEGORIES;

        List<SalesReportData> brandWiseSaleReport = reportService.getBrandWiseSaleReport(
                startDate, endDate, brandName, category
        );

        int expectedSize = 5, actualSize = brandWiseSaleReport.size();
        Assert.assertEquals("Number of brands should be same", expectedSize, actualSize);

        List<SalesReportData> expectedSaleReportData = Arrays.asList(
                new SalesReportData("laptop", "apple", 1, 250000.0),
                new SalesReportData("phone", "apple", 5, 540000.0),
                new SalesReportData("laptop", "lenovo", 3, 195000.0),
                new SalesReportData("shoe", "nike", 3, 60000.0),
                new SalesReportData("phone", "samsung", 2, 310000.0)
        );

        sortSalesReportData(brandWiseSaleReport);
        sortSalesReportData(expectedSaleReportData);

        for (int i = 0; i < expectedSaleReportData.size(); i++) {
            assertEqualSaleReportData(expectedSaleReportData.get(i), brandWiseSaleReport.get(i));
        }
    }

    @Test
    public void getSaleReportForBrandAppleAllCategories() throws ApiException {
        LocalDateTime startDate = currentDate.minusDays(5), endDate = currentDate;
        String brandName = "apple", category = ReportService.ALL_CATEGORIES;

        List<SalesReportData> brandWiseSaleReport = reportService.getBrandWiseSaleReport(
                startDate, endDate, brandName, category
        );

        int expectedSize = 2, actualSize = brandWiseSaleReport.size();
        Assert.assertEquals("Number of brands should be same", expectedSize, actualSize);

        List<SalesReportData> expectedSaleReportData = Arrays.asList(
                new SalesReportData("laptop", "apple", 1, 250000.0),
                new SalesReportData("phone", "apple", 5, 540000.0)
        );

        sortSalesReportData(brandWiseSaleReport);
        sortSalesReportData(expectedSaleReportData);

        for (int i = 0; i < expectedSaleReportData.size(); i++) {
            assertEqualSaleReportData(expectedSaleReportData.get(i), brandWiseSaleReport.get(i));
        }
    }

    @Test
    public void getSaleReportForAllBrandsCategoryPhone() throws ApiException {
        LocalDateTime startDate = currentDate.minusDays(5), endDate = currentDate;
        String brandName = ReportService.ALL_BRANDS, category = "phone";

        List<SalesReportData> brandWiseSaleReport = reportService.getBrandWiseSaleReport(
                startDate, endDate, brandName, category
        );

        int expectedSize = 2, actualSize = brandWiseSaleReport.size();
        Assert.assertEquals("Number of brands should be same", expectedSize, actualSize);

        List<SalesReportData> expectedSaleReportData = Arrays.asList(
                new SalesReportData("phone", "samsung", 2, 310000.0),
                new SalesReportData("phone", "apple", 5, 540000.0)
        );

        sortSalesReportData(brandWiseSaleReport);
        sortSalesReportData(expectedSaleReportData);

        for (int i = 0; i < expectedSaleReportData.size(); i++) {
            assertEqualSaleReportData(expectedSaleReportData.get(i), brandWiseSaleReport.get(i));
        }
    }

    @Test
    public void getSaleReportForBrandAppleCategoryPhone() throws ApiException {
        LocalDateTime startDate = currentDate.minusDays(5), endDate = currentDate;
        String brandName = "apple", category = "phone";

        List<SalesReportData> brandWiseSaleReport = reportService.getBrandWiseSaleReport(
                startDate, endDate, brandName, category
        );

        int expectedSize = 1, actualSize = brandWiseSaleReport.size();
        Assert.assertEquals("Number of brands should be same", expectedSize, actualSize);

        List<SalesReportData> expectedSaleReportData = Collections.singletonList(
                new SalesReportData("phone", "apple", 5, 540000.0)
        );

        sortSalesReportData(brandWiseSaleReport);
        sortSalesReportData(expectedSaleReportData);

        for (int i = 0; i < expectedSaleReportData.size(); i++) {
            assertEqualSaleReportData(expectedSaleReportData.get(i), brandWiseSaleReport.get(i));
        }
    }

    private void sortSalesReportData(List<SalesReportData> brandWiseSaleReport) {
        brandWiseSaleReport.sort(
                Comparator.comparing(SalesReportData::getBrandName).thenComparing(SalesReportData::getCategory)
        );
    }

}