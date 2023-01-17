package com.increff.ironic.pos.utils;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.pojo.*;
import com.increff.ironic.pos.service.*;
import org.junit.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    public static Order getNewOrder() {
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        return order;
    }

    public static Brand getNewBrand() {
        Brand mock = new Brand();
        mock.setName("Mock Brand");
        mock.setName("Mock Category");
        return mock;
    }

    public static Inventory getNewInventory(Integer id) {
        Inventory mock = new Inventory();
        mock.setProductId(id);
        mock.setQuantity(10);
        return mock;
    }

    public static List<Brand> setUpBrands(BrandService brandService) {
        /*
         Total 5 mock brands are there:
         2 with category 'laptop', 2 with category 'phone' and 1 with 'shoe'
         */
        List<Brand> brands = Arrays.asList(
                new Brand(null, "phone", "apple"),
                new Brand(null, "phone", "samsung"),
                new Brand(null, "laptop", "lenovo"),
                new Brand(null, "laptop", "apple"),
                new Brand(null, "shoe", "nike")
        );

        brands.forEach(brand -> {
            try {
                brandService.add(brand);
            } catch (ApiException ignored) {
            }
        });

        return brands;
    }

    public static List<Product> setUpProducts(
            List<Brand> brands,
            ProductService productService,
            InventoryService inventoryService
    ) {

        /* Created mock products representing every brand and category */
        List<Product> products = Arrays.asList(
                getProduct("a1001", brands.get(0).getId(), "iphone x", 150000.0),
                getProduct("a1002", brands.get(0).getId(), "iphone se", 80000.0),
                getProduct("a1003", brands.get(1).getId(), "galaxy fold", 180000.0),
                getProduct("a1004", brands.get(1).getId(), "note 9", 130000.0),
                getProduct("a1005", brands.get(3).getId(), "mac book pro", 250000.0),
                getProduct("a1006", brands.get(2).getId(), "legion 5", 65000.0),
                getProduct("a1007", brands.get(4).getId(), "air jordan", 20000.0)
        );
        products.forEach(product -> {
            try {
                productService.add(product);
                Inventory inventory = inventoryService.get(product.getId());
                inventory.setQuantity(100);
            } catch (ApiException ignored) {
            }
        });

        return products;
    }

    private static Product getProduct(String barcode, Integer brandId, String name, Double price) {
        return new Product(null, barcode, brandId, name, price);
    }

    private static final String MOCK_INVOICE_PATH = "mock invoice path";

    public static void setUpOrders(
            LocalDateTime currentDate,
            OrderService orderService,
            OrderItemService orderItemService,
            List<Product> products
    ) throws ApiException {

        // Brand: Apple | Category: Phone, Laptop
        Order order = new Order(null, currentDate.minusDays(3), MOCK_INVOICE_PATH);
        orderService.create(order);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(null, order.getId(), products.get(0).getId(), 2, 150000.0)); // Iphone X
        items.add(new OrderItem(null, order.getId(), products.get(4).getId(), 1, 250000.0)); // MacBook

        orderItemService.createItems(items);

        // Brand: Samsung | Category: Phone
        order = new Order(null, currentDate.minusDays(3), MOCK_INVOICE_PATH);
        orderService.create(order);

        items = new ArrayList<>();
        items.add(new OrderItem(null, order.getId(), products.get(2).getId(), 1, 180000.0)); // Galaxy Fold
        items.add(new OrderItem(null, order.getId(), products.get(3).getId(), 1, 130000.0)); // Note 9

        orderItemService.createItems(items);

        // Brand: Nike | Category: Shoe
        order = new Order(null, currentDate.minusDays(2), MOCK_INVOICE_PATH);
        orderService.create(order);

        items = new ArrayList<>();
        items.add(new OrderItem(null, order.getId(), products.get(6).getId(), 3, 20000.0)); // Air Jordan

        orderItemService.createItems(items);

        // Brands: Apple, Lenovo | Category: Phone, Laptop
        order = new Order(null, currentDate.minusDays(1), MOCK_INVOICE_PATH);
        orderService.create(order);

        items = new ArrayList<>();
        items.add(new OrderItem(null, order.getId(), products.get(1).getId(), 3, 80000.0)); // IPhone SE
        items.add(new OrderItem(null, order.getId(), products.get(5).getId(), 3, 65000.0)); // Legion 5
        orderItemService.createItems(items);
    }

    public static void assertEqualSaleReportData(SalesReportData expectedReport, SalesReportData actualReport) {
        Assert.assertEquals(expectedReport.getQuantity(), actualReport.getQuantity());
        Assert.assertEquals(expectedReport.getRevenue(), actualReport.getRevenue());
        Assert.assertEquals(expectedReport.getBrandName(), actualReport.getBrandName());
        Assert.assertEquals(expectedReport.getCategory(), actualReport.getCategory());
    }

}
