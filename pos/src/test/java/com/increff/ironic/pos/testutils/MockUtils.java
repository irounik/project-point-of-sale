package com.increff.ironic.pos.testutils;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.data.ProductInventoryQuantity;
import com.increff.ironic.pos.model.form.OrderItemForm;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.*;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.util.ConversionUtil;
import javafx.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MockUtils {

    public static final LocalDateTime currentDate = LocalDateTime.now(ZoneOffset.UTC);

    public static OrderPojo getNewOrder() {
        OrderPojo orderPojo = new OrderPojo();
        orderPojo.setTime(LocalDateTime.now(ZoneOffset.UTC));
        return orderPojo;
    }

    public static BrandPojo getMockBrand() {
        BrandPojo mock = new BrandPojo();
        mock.setBrand("mock brand");
        mock.setCategory("mock category");
        return mock;
    }

    public static InventoryPojo getMockInventory(Integer id) {
        InventoryPojo mock = new InventoryPojo();
        mock.setProductId(id);
        mock.setQuantity(10);
        return mock;
    }

    private static final int BRAND_APPLE_PHONE = 0;
    private static final int BRAND_SAMSUNG_PHONE = 1;
    private static final int BRAND_LENOVO_LAPTOP = 2;
    private static final int BRAND_LENOVO_APPLE = 3;
    private static final int BRAND_NIKE_SHOE = 4;

    public static final List<BrandPojo> BRAND_POJOS = Arrays.asList(
            new BrandPojo(null, "apple", "phone"),
            new BrandPojo(null, "samsung", "phone"),
            new BrandPojo(null, "lenovo", "laptop"),
            new BrandPojo(null, "apple", "laptop"),
            new BrandPojo(null, "nike", "shoe")
    );

    public static List<BrandPojo> setUpBrands(BrandService brandService) {
        /*
         Total 5 mock brands are there:
         2 with category 'laptop', 2 with category 'phone' and 1 with 'shoe'
         */
        BRAND_POJOS.forEach(brand -> {
            try {
                brand.setId(null);
                brandService.add(brand); // After add ID will be set
            } catch (ApiException ignored) {
            }
        });

        return BRAND_POJOS;
    }

    private static final int PRODUCT_IPHONE_X = 0;
    private static final int PRODUCT_IPHONE_SE = 1;
    private static final int PRODUCT_GALAXY_FOLD = 2;
    private static final int PRODUCT_NOTE_9 = 3;
    private static final int PRODUCT_MAC_BOOK_PRO = 4;
    private static final int PRODUCT_LEGION_5 = 5;
    private static final int PRODUCT_AIR_JORDAN = 6;

    public static List<ProductPojo> setupProducts(List<BrandPojo> brandEntities, ProductService productService) {

        /* Created mock products representing every brand and category */
        List<ProductPojo> productEntities = Arrays.asList(
                getProduct("a1001", brandEntities.get(BRAND_APPLE_PHONE).getId(), "iphone x", 150000.0),
                getProduct("a1002", brandEntities.get(BRAND_APPLE_PHONE).getId(), "iphone se", 80000.0),
                getProduct("a1003", brandEntities.get(BRAND_SAMSUNG_PHONE).getId(), "galaxy fold", 180000.0),
                getProduct("a1004", brandEntities.get(BRAND_SAMSUNG_PHONE).getId(), "note 9", 130000.0),
                getProduct("a1005", brandEntities.get(BRAND_LENOVO_APPLE).getId(), "mac book pro", 250000.0),
                getProduct("a1006", brandEntities.get(BRAND_LENOVO_LAPTOP).getId(), "legion 5", 65000.0),
                getProduct("a1007", brandEntities.get(BRAND_NIKE_SHOE).getId(), "air jordan", 20000.0)
        );

        productEntities.forEach(product -> {
            try {
                productService.add(product);
            } catch (ApiException ignored) {
            }
        });

        return productEntities;
    }

    public static void setUpInventory(List<Integer> productIds, InventoryService inventoryService) throws ApiException {
        for (Integer productId : productIds) {
            InventoryPojo mockInventoryPojo = getMockInventory(productId);
            inventoryService.add(mockInventoryPojo);
        }
    }

    public static List<InventoryData> getMockInventoryData() {
        return Arrays.asList(
                new InventoryData(null, "a1001", "iphone x", 10),
                new InventoryData(null, "a1002", "iphone se", 10),
                new InventoryData(null, "a1003", "galaxy fold", 10),
                new InventoryData(null, "a1004", "note 9", 10),
                new InventoryData(null, "a1005", "mac book pro", 10),
                new InventoryData(null, "a1006", "legion 5", 10),
                new InventoryData(null, "a1007", "air jordan", 10)
        );
    }

    private static ProductPojo getProduct(String barcode, Integer brandId, String name, Double price) {
        return new ProductPojo(null, barcode, brandId, name, price);
    }

    private static final String MOCK_INVOICE_PATH = "mock invoice path";

    public static Pair<OrderPojo, List<OrderItemPojo>> addOrder(
            OrderService orderService,
            OrderItemService orderItemService,
            LocalDateTime time,
            InventoryService inventoryService,
            List<OrderItemPojo> orderItemEntities) throws ApiException {

        OrderPojo orderPojo = new OrderPojo(null, time, MOCK_INVOICE_PATH);
        orderService.add(orderPojo);

        orderItemEntities.forEach(item -> {
            item.setOrderId(orderPojo.getId());
            updateInventory(inventoryService, item.getProductId(), item.getQuantity());
        });

        orderItemService.addItems(orderItemEntities);
        return new Pair<>(orderPojo, orderItemEntities);
    }

    private static void updateInventory(InventoryService inventoryService, Integer productId, Integer required) {
        try {
            InventoryPojo inventoryPojo = inventoryService.get(productId);
            inventoryPojo.setQuantity(inventoryPojo.getQuantity() - required);
            inventoryService.update(inventoryPojo);
        } catch (Exception ignored) {
        }
    }

    public static List<Pair<OrderPojo, List<OrderItemPojo>>> setUpMockOrders(
            OrderService orderService,
            OrderItemService orderItemService,
            InventoryService inventoryService,
            List<ProductPojo> productEntities) throws ApiException {

        List<Pair<OrderPojo, List<OrderItemPojo>>> orders = new LinkedList<>();

        // Brand: Apple | Category: Phone, Laptop
        List<OrderItemPojo> items = Arrays.asList(
                new OrderItemPojo(productEntities.get(PRODUCT_IPHONE_X).getId(), 2, 150000.0), // Iphone X
                new OrderItemPojo(productEntities.get(PRODUCT_MAC_BOOK_PRO).getId(), 1, 250000.0) // MacBook
        );
        Pair<OrderPojo, List<OrderItemPojo>> order = addOrder(orderService, orderItemService, currentDate.minusDays(2), inventoryService, items);
        orders.add(order);

        // Brand: Samsung | Category: Phone
        items = Arrays.asList(
                new OrderItemPojo(productEntities.get(PRODUCT_GALAXY_FOLD).getId(), 1, 180000.0), // Galaxy Fold
                new OrderItemPojo(productEntities.get(PRODUCT_NOTE_9).getId(), 1, 130000.0)
        );
        order = addOrder(orderService, orderItemService, currentDate.minusDays(2), inventoryService, items); // Note 9
        orders.add(order);

        // Brand: Nike | Category: Shoe
        items = Collections.singletonList(
                new OrderItemPojo(productEntities.get(PRODUCT_AIR_JORDAN).getId(), 3, 20000.0)
        ); // Air Jordan
        order = addOrder(orderService, orderItemService, currentDate.minusDays(1), inventoryService, items);
        orders.add(order);

        // Brands: Apple, Lenovo | Category: Phone, Laptop
        items = Arrays.asList(
                new OrderItemPojo(productEntities.get(PRODUCT_IPHONE_SE).getId(), 3, 80000.0), // IPhone SE
                new OrderItemPojo(productEntities.get(PRODUCT_LEGION_5).getId(), 3, 65000.0) // Legion 5
        );
        order = addOrder(orderService, orderItemService, currentDate, inventoryService, items);
        orders.add(order);

        return orders;
    }

    public static PerDaySalePojo getMockPerDaySale() {
        PerDaySalePojo perDaySalePojo = new PerDaySalePojo();

        perDaySalePojo.setOrderCount(1);
        perDaySalePojo.setTotalRevenue(100.0);
        perDaySalePojo.setDate(currentDate.minusDays(3));
        perDaySalePojo.setUniqueItemCount(3);
        perDaySalePojo.setTotalQuantityCount(5);

        return perDaySalePojo;
    }

    public static List<PerDaySalePojo> getMockPerDaySales(int size, LocalDateTime currentDate) {
        List<PerDaySalePojo> mock = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            PerDaySalePojo perDaySalePojo = MockUtils.getMockPerDaySale();
            perDaySalePojo.setDate(currentDate.minusDays(size - i));
            mock.add(perDaySalePojo);
        }

        return mock;
    }

    public static ProductPojo getMockProduct() {
        ProductPojo productPojo = new ProductPojo();
        productPojo.setBarcode("a1001");
        productPojo.setPrice(1800.0);
        productPojo.setName("Nike Shoes");
        productPojo.setBrandId(1);
        return productPojo;
    }

    public static ProductForm getMockProductForm() {
        ProductForm product = new ProductForm();
        product.setBarcode("a1001");
        product.setPrice(1800.0);
        product.setName("Nike Shoes");
        product.setBarcode("ni0102");
        product.setBrandName("Nike");
        product.setCategory("Shoe");
        return product;
    }

    public static List<ProductPojo> getMockProducts(int size) {
        List<ProductPojo> productEntities = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            ProductPojo productPojo = getMockProduct();
            productPojo.setBarcode(productPojo.getBarcode() + i);
            productPojo.setPrice(productPojo.getPrice() + i);
            productPojo.setName(productPojo.getName() + i);
            productEntities.add(productPojo);
        }

        return productEntities;
    }

    private static Map<Integer, BrandPojo> getBrandMap(List<BrandPojo> brandEntities) {
        Map<Integer, BrandPojo> brandMap = new HashMap<>();
        brandEntities.forEach(it -> brandMap.put(it.getId(), it));
        return brandMap;
    }

    public static List<ProductData> getMockProductDataList(List<BrandPojo> mockBrandEntities, List<ProductPojo> mockProductEntities) {
        Map<Integer, BrandPojo> brandMap = getBrandMap(mockBrandEntities);
        return mockProductEntities
                .stream()
                .map(product -> ConversionUtil.convertPojoToData(product, brandMap.get(product.getBrandId())))
                .collect(Collectors.toList());
    }

    public static OrderItemForm getMockOrderItemForm() {
        OrderItemForm orderItemForm = new OrderItemForm();
        orderItemForm.setBarcode("a1001");
        orderItemForm.setQuantity(1);
        orderItemForm.setSellingPrice(1000.0);
        return orderItemForm;
    }

    public static UserPojo getMockUser() {
        UserPojo mock = new UserPojo();
        mock.setEmail("mockuser@pos.com");
        mock.setPassword("pass@123");
        mock.setRole(UserRole.OPERATOR);
        return mock;
    }

    public static ProductInventoryQuantity getMockProductInventoryQuantity(
            ProductPojo productPojo,
            InventoryPojo inventoryPojo,
            Integer requiredQuantity
    ) {
        ProductInventoryQuantity productInventoryQuantity = new ProductInventoryQuantity();
        productInventoryQuantity.setInventoryPojo(inventoryPojo);
        productInventoryQuantity.setProductName(productPojo.getName());
        productInventoryQuantity.setRequiredQuantity(requiredQuantity);
        productInventoryQuantity.setBarcode(productPojo.getBarcode());
        return productInventoryQuantity;
    }

    public static UserForm getMockUserForm() {
        UserForm userForm = new UserForm();
        userForm.setEmail("mock@user.com");
        userForm.setPassword("Pass@mock123");
        userForm.setRole(UserRole.OPERATOR.toString());
        return userForm;
    }

}
