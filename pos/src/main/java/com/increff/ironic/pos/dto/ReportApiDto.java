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

    public List<PerDaySaleData> getPerDaySales(PerDaySaleForm form) throws ApiException {
        LocalDateTime startDate = formatStartDate(form.getStartDate());
        LocalDateTime endDate = formatEndDate(form.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new ApiException("Start date can't be after end date!");
        }

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
        List<OrderPojo> orderEntities = orderService.getOrderBetween(yesterday, today);
        List<OrderItemPojo> orderItemEntities = getOrderItems(orderEntities);

        PerDaySalePojo perDaySalePojo = new PerDaySalePojo();
        perDaySalePojo.setDate(yesterday);
        perDaySalePojo.setOrderCount(orderEntities.size());
        perDaySalePojo.setUniqueItemCount(orderItemEntities.size());

        int quantity = 0;
        double revenue = 0;

        for (OrderItemPojo item : orderItemEntities) {
            quantity += item.getQuantity();
            revenue += item.getQuantity() * item.getSellingPrice();
        }

        perDaySalePojo.setTotalQuantityCount(quantity);
        perDaySalePojo.setTotalRevenue(revenue);
        perDaySaleService.add(perDaySalePojo);
    }

    private List<SalesReportData> getBrandWiseSaleReport(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String brandName,
            String category) throws ApiException {

        // Get orders in range
        List<OrderPojo> orderEntities = orderService.getOrderBetween(startDate, endDate);

        // Get order items for the orders
        List<OrderItemPojo> orderItemEntities = getOrderItems(orderEntities);

        // Get products for the order items
        List<Integer> productIds = orderItemEntities.stream().map(OrderItemPojo::getProductId).collect(Collectors.toList());
        List<ProductPojo> productEntities = productService.getProductsByIds(productIds);
        Map<Integer, BrandPojo> productIdBrandMap = getProductIdToBrandMap(productEntities);

        // Creating mapping from Brand to Order Items with the given brand
        List<BrandOrderItems> brandOrderItemMap = getBrandToOrderItemsMap(productIdBrandMap, orderItemEntities);

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

    private List<OrderItemPojo> getOrderItems(List<OrderPojo> orderEntities) {
        List<OrderItemPojo> orderItemEntities = new LinkedList<>();

        for (OrderPojo orderPojo : orderEntities) {
            List<OrderItemPojo> items = orderItemService.getByOrderId(orderPojo.getId());
            orderItemEntities.addAll(items);
        }

        return orderItemEntities;
    }

    private List<SalesReportData> getSaleReportData(List<BrandOrderItems> brandOrderItems) {
        List<SalesReportData> dataList = new LinkedList<>();

        for (BrandOrderItems brandOrderItem : brandOrderItems) {
            BrandPojo brandPojo = brandOrderItem.getBrandPojo();
            List<OrderItemPojo> orderItemEntities = brandOrderItem.getOrderItemEntities();

            int quantities = 0;
            double revenue = 0;

            for (OrderItemPojo item : orderItemEntities) {
                quantities += item.getQuantity();
                revenue += item.getQuantity() * item.getSellingPrice();
            }

            SalesReportData data = new SalesReportData();
            data.setBrandName(brandPojo.getBrand());
            data.setCategory(brandPojo.getCategory());
            data.setQuantity(quantities);
            data.setRevenue(revenue);
            dataList.add(data);
        }

        return dataList;
    }

    public List<BrandOrderItems> getBrandToOrderItemsMap(
            Map<Integer, BrandPojo> productIdBrandMap,
            List<OrderItemPojo> orderItemEntities) {

        Map<Integer, BrandOrderItems> brandOrderItemsMap = new HashMap<>();

        for (OrderItemPojo item : orderItemEntities) {
            BrandPojo brandPojo = productIdBrandMap.get(item.getProductId());
            BrandOrderItems brandOrderItem = new BrandOrderItems(brandPojo, new LinkedList<>());
            brandOrderItemsMap.putIfAbsent(brandPojo.getId(), brandOrderItem);
            brandOrderItemsMap.get(brandPojo.getId()).getOrderItemEntities().add(item);
        }

        return new ArrayList<>(brandOrderItemsMap.values());
    }

    private Map<Integer, BrandPojo> getProductIdToBrandMap(List<ProductPojo> productEntities) throws ApiException {
        Map<Integer, BrandPojo> productIdBrandMap = new HashMap<>();

        for (ProductPojo productPojo : productEntities) {
            BrandPojo brandPojo = brandService.get(productPojo.getBrandId());
            productIdBrandMap.put(productPojo.getId(), brandPojo);
        }

        return productIdBrandMap;
    }

    private List<PerDaySalePojo> getPerDaySale(LocalDateTime startDate, LocalDateTime endDate) {
        return perDaySaleService.getPerDaySaleBetween(startDate, endDate);
    }

    private List<BrandPojo> getBrandReport(String brandName, String category) {
        Stream<BrandPojo> brandStream = brandService.getAll().stream();

        if (!brandName.equalsIgnoreCase(ALL_BRANDS)) {
            brandStream = brandStream.filter(it -> it.getBrand().equals(brandName));
        }

        if (!category.equalsIgnoreCase(ALL_CATEGORIES)) {
            brandStream = brandStream.filter(it -> it.getCategory().equals(category));
        }

        return brandStream.collect(Collectors.toList());
    }

    public List<InventoryReportData> getInventoryReport(BrandCategoryFrom brandCategoryFrom) throws ApiException {
        String brandName = formatBrandName(brandCategoryFrom.getBrand());
        String category = formatCategory(brandCategoryFrom.getCategory());

        // Product ID to Brand
        List<ProductPojo> productEntities = productService.getAll();

        Map<Integer, BrandPojo> productIdBrandMap = getProductIdToBrandMap(productEntities);

        Map<Integer, BrandPojo> brandIdToBrandMap = getBrandIdToBrandMap(productIdBrandMap.values());

        // Inventory per item
        Map<Integer, Integer> brandItemCountMap = getBrandToItemCountMap(productIdBrandMap);

        // Converting Brand to Inventory count mapping to InventoryReportData
        List<InventoryReportData> inventoryReportDataList = new LinkedList<>();

        brandItemCountMap.forEach((brandId, count) -> {
            BrandPojo brandPojo = brandIdToBrandMap.get(brandId);
            if (!shouldAdd(brandPojo, brandName, category)) return;
            InventoryReportData data = new InventoryReportData(brandPojo.getBrand(), brandPojo.getCategory(), count);
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

    private boolean shouldAdd(BrandPojo brandPojo, String brandName, String category) {
        if (brandPojo == null) {
            return false;
        }

        boolean allBrands = brandName.equalsIgnoreCase(ALL_BRANDS);
        boolean allCategories = category.equalsIgnoreCase(ALL_CATEGORIES);

        if (allBrands && allCategories) {
            return true;
        }

        boolean categoryMatched = category.equalsIgnoreCase(brandPojo.getCategory());
        boolean brandMatched = brandName.equalsIgnoreCase(brandPojo.getBrand());

        if (allBrands) {
            return categoryMatched;
        }

        if (allCategories) {
            return brandMatched;
        }

        return brandMatched && categoryMatched;
    }

    private Map<Integer, BrandPojo> getBrandIdToBrandMap(Collection<BrandPojo> values) {
        Map<Integer, BrandPojo> brandIdToBrandMap = new HashMap<>();
        values.forEach(brand -> brandIdToBrandMap.put(brand.getId(), brand));
        return brandIdToBrandMap;
    }

    private Map<Integer, Integer> getBrandToItemCountMap(Map<Integer, BrandPojo> productIdToBrandMap) {
        Map<Integer, Integer> brandItemCountMap = new HashMap<>();
        List<BrandPojo> brandEntities = brandService.getAll();

        brandEntities.forEach(it -> brandItemCountMap.put(it.getId(), 0));

        List<InventoryPojo> inventoryPojoList = inventoryService.getAll();
        for (InventoryPojo inventoryPojo : inventoryPojoList) {
            Integer productId = inventoryPojo.getProductId();
            BrandPojo brandPojo = productIdToBrandMap.get(productId);

            Integer newQuantity = brandItemCountMap.get(brandPojo.getId()) + inventoryPojo.getQuantity();
            brandItemCountMap.put(brandPojo.getId(), newQuantity);
        }

        return brandItemCountMap;
    }

}