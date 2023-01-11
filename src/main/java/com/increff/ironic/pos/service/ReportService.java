package com.increff.ironic.pos.service;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final BrandService brandService;
    private final ProductService productService;
    private final PerDaySaleService perDaySaleService;

    public static final String ALL_BRANDS = "all_brands";
    public static final String ALL_CATEGORIES = "all_categories";

    @Autowired
    public ReportService(
            OrderService orderService,
            OrderItemService orderItemService,
            BrandService brandService,
            ProductService productService,
            PerDaySaleService perDaySaleService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.brandService = brandService;
        this.productService = productService;
        this.perDaySaleService = perDaySaleService;
    }

    public void updatePerDaySale() {
        LocalDateTime today = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime yesterday = today.minusDays(1);
        List<Order> orders = orderService.getOrderBetween(yesterday, today);
        List<OrderItem> orderItems = getOrderItems(orders);

        PerDaySale perDaySale = new PerDaySale();
        perDaySale.setDate(yesterday.toLocalDate());
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

    public List<SalesReportData> getBrandWiseSaleReport(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String brandName,
            String category) throws ApiException {

        // Get orders in range
        List<Order> orders = orderService.getOrderBetween(startDate, endDate);

        // Get order items for the orders
        List<OrderItem> orderItems = getOrderItems(orders);

        // Get products for the order items
        Map<Integer, Brand> productIdBrandMap = getProductIdToBrandMap(orderItems);

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

    private Map<Integer, Brand> getProductIdToBrandMap(List<OrderItem> orderItems) throws ApiException {
        Map<Integer, Brand> productIdBrandMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            Product product = productService.get(item.getProductId());
            Brand brand = brandService.get(product.getBrandId());
            productIdBrandMap.put(product.getId(), brand);
        }

        return productIdBrandMap;
    }

    public List<PerDaySale> getPerDaySale(LocalDateTime startDate, LocalDateTime endDate) {
        return perDaySaleService.getPerDaySaleBetween(startDate, endDate);
    }

}
