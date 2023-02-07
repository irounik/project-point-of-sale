package com.increff.ironic.pos.scheduler;

import com.increff.ironic.pos.dto.ReportApiDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
public class ReportScheduler {

    private final ReportApiDto reportApiDto;

    @Autowired
    public ReportScheduler(ReportApiDto reportApiDto) {
        this.reportApiDto = reportApiDto;
    }

    @Scheduled(cron = "0 0 0 ? * *")
    public void updatePerDaySaleSchedule() {
        reportApiDto.updatePerDaySale();
    }

}
