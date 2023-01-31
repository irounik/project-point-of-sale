package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.ReportApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api
@RestController
@RequestMapping(path = "/api/reports")
public class ReportApiController {

    @Autowired
    private ReportApiDto reportApiDto;

    @ApiOperation(value = "Get sales report")
    @RequestMapping(path = "/sales", method = RequestMethod.POST)
    public List<SalesReportData> getSaleReport(@RequestBody SalesReportForm salesReportForm) throws ApiException {
        return reportApiDto.getSalesReport(salesReportForm);
    }

    /**
     * updatePerDaySale will be called every night at 12:00:00 AM (midnight)
     */
    @Scheduled(cron = "0 0 0 ? * *")
    public void updatePerDaySale() {
        reportApiDto.updatePerDaySale();
    }

    @ApiOperation(value = "Get per day sale report")
    @RequestMapping(path = "/per-day-sale", method = RequestMethod.POST)
    public List<PerDaySaleData> getPerDaySale(@RequestBody PerDaySaleForm form) {
        return reportApiDto.getPerDaySales(form);
    }

    @ApiOperation(value = "Get brand report")
    @RequestMapping(path = "/brand", method = RequestMethod.POST)
    public List<BrandReportData> getBrandReport(@RequestBody BrandCategoryFrom brandReportForm) {
        return reportApiDto.getBrandReport(brandReportForm);
    }

    @ApiOperation(value = "Get inventory report")
    @RequestMapping(path = "/inventory", method = RequestMethod.GET)
    public List<InventoryReportData> getInventoryReport() throws ApiException {
        return reportApiDto.getInventoryReport();
    }

}
