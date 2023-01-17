package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.*;
import com.increff.ironic.pos.service.ReportService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportApiDto {

    private final ReportService reportService;

    @Autowired
    public ReportApiDto(ReportService reportService) {
        this.reportService = reportService;
    }

    public List<SalesReportData> getSalesReport(SalesReportForm salesReportForm) throws ApiException {
        String brandName = formatBrandName(salesReportForm.getBrandName());
        String category = formatCategory(salesReportForm.getCategory());

        LocalDateTime startDate = formatStartDate(salesReportForm.getStartDate());
        LocalDateTime endDate = formatEndDate(salesReportForm.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new ApiException("Start date must be before end date!");
        }

        return reportService.getBrandWiseSaleReport(startDate, endDate, brandName, category);
    }

    private static LocalDateTime formatStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            startDate = LocalDateTime.MIN;
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
            brandName = ReportService.ALL_BRANDS;
        }
        return brandName;
    }

    private static String formatCategory(String category) {
        if (ValidationUtil.isBlank(category)) {
            category = ReportService.ALL_CATEGORIES;
        }
        return category;
    }

    public void updatePerDaySale() {
        reportService.updatePerDaySale();
    }

    public List<PerDaySaleData> getPerDaySales(PerDaySaleForm form) {
        LocalDateTime startDate = formatStartDate(form.getStartDate());
        LocalDateTime endDate = formatEndDate(form.getEndDate());

        return reportService.getPerDaySale(startDate, endDate)
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public List<BrandReportData> getBrandReport(BrandCategoryFrom brandReportForm) {
        String brandName = formatBrandName(brandReportForm.getBrand());
        String category = formatCategory(brandReportForm.getCategory());
        return reportService
                .getBrandReport(brandName, category)
                .stream()
                .map(ConversionUtil::convertBrandToReport)
                .collect(Collectors.toList());
    }

    public List<InventoryReportData> getInventoryReport() throws ApiException {
        return reportService.getInventoryReport();
    }
}