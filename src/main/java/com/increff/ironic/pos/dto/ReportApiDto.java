package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.model.report.SalesReportForm;
import com.increff.ironic.pos.service.ReportService;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class ReportApiDto {

    private final ReportService reportService;

    @Autowired
    public ReportApiDto(ReportService reportService) {
        this.reportService = reportService;
    }

    public List<SalesReportData> getSalesReport(SalesReportForm salesReportForm) throws ApiException {
        String brandName = getBrandName(salesReportForm);
        String category = getCategory(salesReportForm);

        LocalDateTime startDate = getStartDate(salesReportForm);
        LocalDateTime endDate = getEndDate(salesReportForm);

        if (endDate.isBefore(startDate)) {
            throw new ApiException("Start date must be before end date!");
        }

        return reportService.getBrandWiseSaleReport(startDate, endDate, brandName, category);
    }

    private static LocalDateTime getStartDate(SalesReportForm salesReportForm) {
        LocalDateTime startDate = salesReportForm.getStartDate();
        if (startDate == null) {
            startDate = LocalDateTime.MIN;
        }
        // Start date should have time: 12:00:00 AM
        return startDate.toLocalDate().atTime(0, 0, 0);
    }

    private static LocalDateTime getEndDate(SalesReportForm salesReportForm) {
        LocalDateTime endDate = salesReportForm.getEndDate();
        if (endDate == null) {
            endDate = LocalDateTime.now(ZoneOffset.UTC);
        }
        // End date should have time: 11:59:59 PM
        return endDate.toLocalDate().atTime(23, 59, 59);
    }

    private static String getBrandName(SalesReportForm salesReportForm) {
        String brandName = salesReportForm.getBrandName();
        if (ValidationUtil.isBlank(brandName)) {
            brandName = ReportService.ALL_BRANDS;
        }
        return brandName;
    }

    private static String getCategory(SalesReportForm salesReportForm) {
        String category = salesReportForm.getCategory();
        if (ValidationUtil.isBlank(category)) {
            category = ReportService.ALL_CATEGORIES;
        }
        return category;
    }

}