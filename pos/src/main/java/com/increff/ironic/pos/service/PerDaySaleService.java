package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PerDaySaleService {

    private final PerDaySaleDao perDaySaleDao;

    @Autowired
    public PerDaySaleService(PerDaySaleDao perDaySaleDao) {
        this.perDaySaleDao = perDaySaleDao;
    }

    public List<PerDaySale> getAll() {
        return perDaySaleDao.selectAll();
    }

    public void add(PerDaySale perDaySale) {
        perDaySaleDao.insert(perDaySale);
    }

    public List<PerDaySale> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return perDaySaleDao.getPerDaySaleBetween(startDate, endDate);
    }
}
