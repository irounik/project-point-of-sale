package com.increff.ironic.pos.utils;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.pojo.*;
import com.increff.ironic.pos.service.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class MockUtils {

    public static final LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);

    public static Order getNewOrder() {
        Order order = new Order();
        order.setTime(LocalDateTime.now(ZoneOffset.UTC));
        return order;
    }

    public static Brand getMockBrand() {
        Brand mock = new Brand();
        mock.setName("Mock Brand");
        mock.setCategory("Mock Category");
        return mock;
    }

    public static Inventory getNewInventory(Integer id) {
        Inventory mock = new Inventory();
        mock.setProductId(id);
        mock.setQuantity(10);
        return mock;
    }

    private static final int BRAND_APPLE_PHONE = 0;
    private static final int BRAND_SAMSUNG_PHONE = 1;
    private static final int BRAND_LENOVO_LAPTOP = 2;
    private static final int BRAND_LENOVO_APPLE = 3;
    private static final int BRAND_NIKE_SHOE = 4;

    public static final List<Brand> BRANDS = Arrays.asList(
            new Brand(null, "phone", "apple"),
            new Brand(null, "phone", "samsung"),
            new Brand(null, "laptop", "lenovo"),
            new Brand(null, "laptop", "apple"),
            new Brand(null, "shoe", "nike")
    );

    public static List<Brand> setUpBrands(BrandService brandService) {
        /*
         Total 5 mock brands are there:
         2 with category 'laptop', 2 with category 'phone' and 1 with 'shoe'
         */
        BRANDS.forEach(brand -> {
            try {
                brand.setId(null);
                brandService.add(brand); // After add ID will be set
            } catch (ApiException ignored) {
            }
        });

        return BRANDS;
    }

    private static final int PRODUCT_IPHONE_X = 0;
    private static final int PRODUCT_IPHONE_SE = 1;
    private static final int PRODUCT_GALAXY_FOLD = 2;
    private static final int PRODUCT_NOTE_9 = 3;
    private static final int PRODUCT_MAC_BOOK_PRO = 4;
    private static final int PRODUCT_LEGION_5 = 5;
    private static final int PRODUCT_AIR_JORDAN = 6;

    public static List<Product> setUpProductsAndInventory(
            List<Brand> brands,
            ProductService productService,
            InventoryService inventoryService) {

        /* Created mock products representing every brand and category */
        List<Product> products = Arrays.asList(
                getProduct("a1001", brands.get(BRAND_APPLE_PHONE).getId(), "iphone x", 150000.0),
                getProduct("a1002", brands.get(BRAND_APPLE_PHONE).getId(), "iphone se", 80000.0),
                getProduct("a1003", brands.get(BRAND_SAMSUNG_PHONE).getId(), "galaxy fold", 180000.0),
                getProduct("a1004", brands.get(BRAND_SAMSUNG_PHONE).getId(), "note 9", 130000.0),
                getProduct("a1005", brands.get(BRAND_LENOVO_APPLE).getId(), "mac book pro", 250000.0),
                getProduct("a1006", brands.get(BRAND_LENOVO_LAPTOP).getId(), "legion 5", 65000.0),
                getProduct("a1007", brands.get(BRAND_NIKE_SHOE).getId(), "air jordan", 20000.0)
        );

        products.forEach(product -> {
            try {
                productService.add(product);
                Inventory inventory = getNewInventory(product.getId());
                inventoryService.add(inventory);
            } catch (ApiException ignored) {
            }
        });

        return products;
    }

    public static List<InventoryData> getMockInventoryData() {
        return Arrays.asList(
                new InventoryData("a1001", "iphone x", 10),
                new InventoryData("a1002", "iphone se", 10),
                new InventoryData("a1003", "galaxy fold", 10),
                new InventoryData("a1004", "note 9", 10),
                new InventoryData("a1005", "mac book pro", 10),
                new InventoryData("a1006", "legion 5", 10),
                new InventoryData("a1007", "air jordan", 10)
        );
    }

    private static Product getProduct(String barcode, Integer brandId, String name, Double price) {
        return new Product(null, barcode, brandId, name, price);
    }

    private static final String MOCK_INVOICE_PATH = "mock invoice path";

    public static void addOrder(
            OrderService orderService,
            OrderItemService orderItemService,
            LocalDateTime time,
            InventoryService inventoryService,
            List<OrderItem> orderItems) throws ApiException {

        Order order = new Order(null, time, MOCK_INVOICE_PATH);
        orderService.create(order);

        orderItems.forEach(item -> {
            item.setOrderId(order.getId());
            updateInventory(inventoryService, item.getProductId(), item.getQuantity());
        });

        orderItemService.createItems(orderItems);
    }

    private static void updateInventory(InventoryService inventoryService, Integer productId, Integer required) {
        try {
            Inventory inventory = inventoryService.get(productId);
            inventory.setQuantity(inventory.getQuantity() - required);
            inventoryService.update(inventory);
        } catch (Exception ignored) {
        }
    }

    public static void setUpMockOrders(
            OrderService orderService,
            OrderItemService orderItemService,
            InventoryService inventoryService,
            List<Product> products) throws ApiException {

        // Brand: Apple | Category: Phone, Laptop
        List<OrderItem> items = Arrays.asList(
                new OrderItem(products.get(PRODUCT_IPHONE_X).getId(), 2, 150000.0), // Iphone X
                new OrderItem(products.get(PRODUCT_MAC_BOOK_PRO).getId(), 1, 250000.0) // MacBook
        );
        addOrder(orderService, orderItemService, currentDate.minusDays(2), inventoryService, items);

        // Brand: Samsung | Category: Phone
        items = Arrays.asList(
                new OrderItem(products.get(PRODUCT_GALAXY_FOLD).getId(), 1, 180000.0), // Galaxy Fold
                new OrderItem(products.get(PRODUCT_NOTE_9).getId(), 1, 130000.0)
        );
        addOrder(orderService, orderItemService, currentDate.minusDays(2), inventoryService, items); // Note 9

        // Brand: Nike | Category: Shoe
        items = Collections.singletonList(
                new OrderItem(products.get(PRODUCT_AIR_JORDAN).getId(), 3, 20000.0)
        ); // Air Jordan
        addOrder(orderService, orderItemService, currentDate.minusDays(1), inventoryService, items);

        // Brands: Apple, Lenovo | Category: Phone, Laptop
        items = Arrays.asList(
                new OrderItem(products.get(PRODUCT_IPHONE_SE).getId(), 3, 80000.0), // IPhone SE
                new OrderItem(products.get(PRODUCT_LEGION_5).getId(), 3, 65000.0) // Legion 5
        );
        addOrder(orderService, orderItemService, currentDate, inventoryService, items);
    }

    public static PerDaySale getMockPerDaySale() {
        PerDaySale perDaySale = new PerDaySale();

        perDaySale.setOrderCount(1);
        perDaySale.setTotalRevenue(100.0);
        perDaySale.setDate(currentDate.minusDays(3));
        perDaySale.setUniqueItemCount(3);
        perDaySale.setTotalQuantityCount(5);

        return perDaySale;
    }

    public static List<PerDaySale> getMockPerDaySales(int size, LocalDateTime currentDate) {
        List<PerDaySale> mock = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            PerDaySale perDaySale = MockUtils.getMockPerDaySale();
            perDaySale.setDate(currentDate.minusDays(size - i));
            mock.add(perDaySale);
        }

        return mock;
    }

}
