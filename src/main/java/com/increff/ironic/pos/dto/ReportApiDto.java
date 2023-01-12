package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.PerDaySaleData;
import com.increff.ironic.pos.model.report.PerDaySaleForm;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.model.report.SalesReportForm;
import com.increff.ironic.pos.service.ReportService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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
        String brandName = getBrandName(salesReportForm);
        String category = getCategory(salesReportForm);

        LocalDateTime startDate = formatStartDate(salesReportForm.getStartDate());
        LocalDateTime endDate = formatEndDate(salesReportForm.getEndDate());

        if (endDate.isBefore(startDate)) {
            throw new ApiException("Start date must be before end date!");
        }

        return reportService.getBrandWiseSaleReport(startDate, endDate, brandName, category);
    }

    private static LocalDateTime getLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    private static LocalDateTime formatStartDate(Date date) {
        LocalDateTime startDate = getLocalDate(date);
        if (startDate == null) {
            startDate = LocalDateTime.MIN;
        }
        // Start date should have time: 12:00:00 AM
        return startDate.toLocalDate().atTime(0, 0, 0);
    }

    private static LocalDateTime formatEndDate(Date date) {
        LocalDateTime endDate = getLocalDate(date);
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

}