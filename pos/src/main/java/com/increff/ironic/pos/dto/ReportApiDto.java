package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.pojo.*;
import com.increff.ironic.pos.service.*;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ReportApiDto {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final BrandService brandService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final PerDaySaleService perDaySaleService;

    public ReportApiDto(
            OrderService orderService,
            OrderItemService orderItemService,
            BrandService brandService,
            ProductService productService,
            InventoryService inventoryService,
            PerDaySaleService perDaySaleService) {

        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.brandService = brandService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.perDaySaleService = perDaySaleService;
    }

    public List<SalesReportData> getSalesReport(SalesReportForm salesReportForm) throws ApiException {
        String brandName = formatBrandName(salesReportForm.getBrandName());
        String category = formatCategory(salesReportForm.getCategory());

        LocalDateTime startDate = formatStartDate(salesReportForm.getStartDate());
        LocalDateTime endDate = formatEndDate(salesReportForm.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new ApiException("Start date must be before end date!");
        }

        return getBrandWiseSaleReport(startDate, endDate, brandName, category);
    }

    private static LocalDateTime formatStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            startDate = LocalDateTime.of(0, 1, 1, 1, 1);
        }
        // Start date should have time: 12:00:00 AM
        return startDate.toLocalDate().atTime(0, 0, 0);
    }

    private static LocalDateTime formatEndDate(LocalDateTime endDate) {
        if (endDate == null) {
            endDate = LocalDateTime.now(ZoneOffset.UTC);
        }
        // End date should have time: 11:59:59 PM
        return endDate.toLocalDate().atTime(23, 59, 59);
    }

    private static String formatBrandName(String brandName) {
        if (ValidationUtil.isBlank(brandName)) {
            brandName = ALL_BRANDS;
        }
        return brandName;
    }

    private static String formatCategory(String category) {
        if (ValidationUtil.isBlank(category)) {
            category = ALL_CATEGORIES;
        }
        return category;
    }

    public List<PerDaySaleData> getPerDaySales(PerDaySaleForm form) {
        LocalDateTime startDate = formatStartDate(form.getStartDate());
        LocalDateTime endDate = formatEndDate(form.getEndDate());

        return getPerDaySale(startDate, endDate)
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public List<BrandReportData> getBrandReport(BrandCategoryFrom brandReportForm) {
        String brandName = formatBrandName(brandReportForm.getBrand());
        String category = formatCategory(brandReportForm.getCategory());
        return getBrandReport(brandName, category)
                .stream()
                .map(ConversionUtil::convertBrandToReport)
                .collect(Collectors.toList());
    }


    public static final String ALL_BRANDS = "all_brands";
    public static final String ALL_CATEGORIES = "all_categories";

    @Transactional
    public void updatePerDaySale() {
        LocalDateTime today = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime yesterday = today.minusDays(1);
        List<Order> orders = orderService.getOrderBetween(yesterday, today);
        List<OrderItem> orderItems = getOrderItems(orders);

        PerDaySale perDaySale = new PerDaySale();
        perDaySale.setDate(yesterday);
        perDaySale.setOrderCount(orders.size());
        perDaySale.setUniqueItemCount(orderItems.size());

        int quantity = 0;
        double revenue = 0;

        for (OrderItem item : orderItems) {
            quantity += item.getQuantity();
            revenue += item.getQuantity() * item.getSellingPrice();
        }

        perDaySale.setTotalQuantityCount(quantity);
        perDaySale.setTotalRevenue(revenue);
        perDaySaleService.add(perDaySale);
    }

    private List<SalesReportData> getBrandWiseSaleReport(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String brandName,
            String category) throws ApiException {

        // Get orders in range
        List<Order> orders = orderService.getOrderBetween(startDate, endDate);

        // Get order items for the orders
        List<OrderItem> orderItems = getOrderItems(orders);

        // Get products for the order items
        List<Integer> productIds = orderItems.stream().map(OrderItem::getProductId).collect(Collectors.toList());
        List<Product> products = productService.getProductsByIds(productIds);
        Map<Integer, Brand> productIdBrandMap = getProductIdToBrandMap(products);

        // Creating mapping from Brand to Order Items with the given brand
        Map<Brand, List<OrderItem>> brandOrderItemsMap = getBrandToOrderItemsMap(productIdBrandMap, orderItems);

        // Calculate revenue and quantity
        List<SalesReportData> dataList = getSaleReportData(brandOrderItemsMap);

        Stream<SalesReportData> salesReportStream = dataList.stream();

        // Filter brand
        if (!brandName.equalsIgnoreCase(ALL_BRANDS)) {
            salesReportStream = salesReportStream.filter(data -> data.getBrandName().equalsIgnoreCase(brandName));
        }

        // Filter category
        if (!category.equalsIgnoreCase(ALL_CATEGORIES)) {
            salesReportStream = salesReportStream.filter(data -> data.getCategory().equalsIgnoreCase(category));
        }

        return salesReportStream.collect(Collectors.toList());
    }

    private List<OrderItem> getOrderItems(List<Order> orders) {
        List<OrderItem> orderItems = new LinkedList<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemService.getByOrderId(order.getId());
            orderItems.addAll(items);
        }

        return orderItems;
    }

    private List<SalesReportData> getSaleReportData(Map<Brand, List<OrderItem>> brandOrderItemsMap) {
        List<SalesReportData> dataList = new LinkedList<>();

        for (Map.Entry<Brand, List<OrderItem>> entry : brandOrderItemsMap.entrySet()) {
            Brand brand = entry.getKey();
            List<OrderItem> brandOrderItems = entry.getValue();
            int quantities = 0;
            double revenue = 0;

            for (OrderItem item : brandOrderItems) {
                quantities += item.getQuantity();
                revenue += item.getQuantity() * item.getSellingPrice();
            }

            SalesReportData data = new SalesReportData();
            data.setBrandName(brand.getName());
            data.setCategory(brand.getCategory());
            data.setQuantity(quantities);
            data.setRevenue(revenue);
            dataList.add(data);
        }

        return dataList;
    }

    public Map<Brand, List<OrderItem>> getBrandToOrderItemsMap(
            Map<Integer, Brand> productIdBrandMap,
            List<OrderItem> orderItems) {

        Map<Brand, List<OrderItem>> brandOrderItemsMap = new HashMap<>();
        for (OrderItem item : orderItems) {
            Brand brand = productIdBrandMap.get(item.getProductId());
            brandOrderItemsMap.putIfAbsent(brand, new LinkedList<>());
            brandOrderItemsMap.get(brand).add(item);
        }
        return brandOrderItemsMap;
    }

    private Map<Integer, Brand> getProductIdToBrandMap(List<Product> products) throws ApiException {
        Map<Integer, Brand> productIdBrandMap = new HashMap<>();

        for (Product product : products) {
            Brand brand = brandService.get(product.getBrandId());
            productIdBrandMap.put(product.getId(), brand);
        }

        return productIdBrandMap;
    }

    private List<PerDaySale> getPerDaySale(LocalDateTime startDate, LocalDateTime endDate) {
        return perDaySaleService.getPerDaySaleBetween(startDate, endDate);
    }

    private List<Brand> getBrandReport(String brandName, String category) {
        Stream<Brand> brandStream = brandService.getAll().stream();

        if (!brandName.equalsIgnoreCase(ALL_BRANDS)) {
            brandStream = brandStream.filter(it -> it.getName().equals(brandName));
        }

        if (!category.equalsIgnoreCase(ALL_CATEGORIES)) {
            brandStream = brandStream.filter(it -> it.getCategory().equals(category));
        }

        return brandStream.collect(Collectors.toList());
    }

    public List<InventoryReportData> getInventoryReport() throws ApiException {

        // Product ID to Brand
        List<Product> products = productService.getAll();
        Map<Integer, Brand> productIdBrandMap = getProductIdToBrandMap(products);

        // Inventory per item
        Map<Brand, Integer> brandItemCountMap = getBrandToItemCountMap(productIdBrandMap);

        // Converting Brand to Inventory count mapping to InventoryReportData
        List<InventoryReportData> inventoryReportDataList = new LinkedList<>();

        brandItemCountMap.forEach((brand, count) -> {
            InventoryReportData data = new InventoryReportData(brand.getName(), brand.getCategory(), count);
            inventoryReportDataList.add(data);
        });

        // Sorting by brand name & category
        Comparator<InventoryReportData> comparator = Comparator
                .comparing(InventoryReportData::getBrand)
                .thenComparing(InventoryReportData::getCategory);
        inventoryReportDataList.sort(comparator);

        // Returning final list
        return inventoryReportDataList;
    }

    private Map<Brand, Integer> getBrandToItemCountMap(Map<Integer, Brand> productIdToBrandMap) {
        Map<Brand, Integer> brandItemCountMap = new HashMap<>();
        List<Brand> brands = brandService.getAll();

        brands.forEach(it -> brandItemCountMap.put(it, 0)); // Initializing by 0

        List<Inventory> inventoryList = inventoryService.getAll();
        for (Inventory inventory : inventoryList) {
            Integer productId = inventory.getProductId();
            Brand brand = productIdToBrandMap.get(productId);

            Integer newQuantity = brandItemCountMap.get(brand) + inventory.getQuantity();
            brandItemCountMap.put(brand, newQuantity);
        }

        return brandItemCountMap;
    }

}