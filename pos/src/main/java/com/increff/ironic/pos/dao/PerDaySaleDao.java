package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.PerDaySalePojo;

import java.time.LocalDateTime;
import java.util.List;

public interface PerDaySaleDao extends CrudDao<PerDaySalePojo, Integer> {

    List<PerDaySalePojo> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate);

}
