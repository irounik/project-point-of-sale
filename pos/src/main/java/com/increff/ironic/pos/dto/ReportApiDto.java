package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.BrandOrderItems;
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

import static com.increff.ironic.pos.util.Constants.ALL_BRANDS;
import static com.increff.ironic.pos.util.Constants.ALL_CATEGORIES;
import static com.increff.ironic.pos.util.DateTimeUtil.formatEndDate;
import static com.increff.ironic.pos.util.DateTimeUtil.formatStartDate;

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
        List<BrandOrderItems> brandOrderItemMap = getBrandToOrderItemsMap(productIdBrandMap, orderItems);

        // Calculate revenue and quantity
        List<SalesReportData> dataList = getSaleReportData(brandOrderItemMap);

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

    private List<SalesReportData> getSaleReportData(List<BrandOrderItems> brandOrderItems) {
        List<SalesReportData> dataList = new LinkedList<>();

        for (BrandOrderItems brandOrderItem : brandOrderItems) {
            Brand brand = brandOrderItem.getBrand();
            List<OrderItem> orderItems = brandOrderItem.getOrderItems();

            int quantities = 0;
            double revenue = 0;

            for (OrderItem item : orderItems) {
                quantities += item.getQuantity();
                revenue += item.getQuantity() * item.getSellingPrice();
            }

            SalesReportData data = new SalesReportData();
            data.setBrandName(brand.getBrand());
            data.setCategory(brand.getCategory());
            data.setQuantity(quantities);
            data.setRevenue(revenue);
            dataList.add(data);
        }

        return dataList;
    }

    public List<BrandOrderItems> getBrandToOrderItemsMap(
            Map<Integer, Brand> productIdBrandMap,
            List<OrderItem> orderItems) {

        Map<Integer, BrandOrderItems> brandOrderItemsMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            Brand brand = productIdBrandMap.get(item.getProductId());
            BrandOrderItems brandOrderItem = new BrandOrderItems(brand, new LinkedList<>());
            brandOrderItemsMap.putIfAbsent(brand.getId(), brandOrderItem);
            brandOrderItemsMap.get(brand.getId()).getOrderItems().add(item);
        }

        return new ArrayList<>(brandOrderItemsMap.values());
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
            brandStream = brandStream.filter(it -> it.getBrand().equals(brandName));
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

        Map<Integer, Brand> brandIdToBrandMap = getBrandIdToBrandMap(productIdBrandMap.values());

        // Inventory per item
        Map<Integer, Integer> brandItemCountMap = getBrandToItemCountMap(productIdBrandMap);

        // Converting Brand to Inventory count mapping to InventoryReportData
        List<InventoryReportData> inventoryReportDataList = new LinkedList<>();

        brandItemCountMap.forEach((brandId, count) -> {
            Brand brand = brandIdToBrandMap.get(brandId);
            if (brand == null) return;
            InventoryReportData data = new InventoryReportData(brand.getBrand(), brand.getCategory(), count);
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

    private Map<Integer, Brand> getBrandIdToBrandMap(Collection<Brand> values) {
        Map<Integer, Brand> brandIdToBrandMap = new HashMap<>();
        values.forEach(brand -> brandIdToBrandMap.put(brand.getId(), brand));
        return brandIdToBrandMap;
    }

    private Map<Integer, Integer> getBrandToItemCountMap(Map<Integer, Brand> productIdToBrandMap) {
        Map<Integer, Integer> brandItemCountMap = new HashMap<>();
        List<Brand> brands = brandService.getAll();

        brands.forEach(it -> brandItemCountMap.put(it.getId(), 0));

        List<Inventory> inventoryList = inventoryService.getAll();
        for (Inventory inventory : inventoryList) {
            Integer productId = inventory.getProductId();
            Brand brand = productIdToBrandMap.get(productId);

            Integer newQuantity = brandItemCountMap.get(brand.getId()) + inventory.getQuantity();
            brandItemCountMap.put(brand.getId(), newQuantity);
        }

        return brandItemCountMap;
    }

}