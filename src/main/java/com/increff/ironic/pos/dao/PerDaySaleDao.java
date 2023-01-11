package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.PerDaySale;

import java.time.LocalDateTime;
import java.util.List;

public interface PerDaySaleDao extends CrudDao<PerDaySale, Integer> {

    List<PerDaySale> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate);

}
