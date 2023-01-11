package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.ReportApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.report.PerDaySaleData;
import com.increff.ironic.pos.model.report.PerDaySaleForm;
import com.increff.ironic.pos.model.report.SalesReportData;
import com.increff.ironic.pos.model.report.SalesReportForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/report")
public class ReportApiController {

    @Autowired
    ReportApiDto reportApiDto;

    @RequestMapping(path = "/sale", method = RequestMethod.GET)
    public List<SalesReportData> getSaleReport(@RequestBody SalesReportForm salesReportForm) throws ApiException {
        return reportApiDto.getSalesReport(salesReportForm);
    }

    /**
     * updatePerDaySale will be called every night at 12:00:00 AM (midnight)
     */
    @Scheduled(cron = "0 0 0 ? * * *")
    public void updatePerDaySale() {
        reportApiDto.updatePerDaySale();
    }

    @RequestMapping(path = "/per-day-sale", method = RequestMethod.GET)
    public List<PerDaySaleData> getPerDaySale(@RequestBody PerDaySaleForm form) {
        return reportApiDto.getPerDaySales(form);
    }

}
