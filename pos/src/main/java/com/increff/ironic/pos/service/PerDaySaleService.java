package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySalePojo;
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

    public List<PerDaySalePojo> getAll() {
        return perDaySaleDao.selectAll();
    }

    public void add(PerDaySalePojo perDaySalePojo) {
        perDaySaleDao.insert(perDaySalePojo);
    }

    public List<PerDaySalePojo> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return perDaySaleDao.getPerDaySaleBetween(startDate, endDate);
    }
}
