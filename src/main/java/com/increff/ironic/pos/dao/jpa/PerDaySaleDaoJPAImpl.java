package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySale;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PerDaySaleDaoJPAImpl extends AbstractJPADao<PerDaySale, Integer> implements PerDaySaleDao {

    @Override
    protected Class<PerDaySale> getEntityClass() {
        return PerDaySale.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public List<PerDaySale> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return selectWhereBetween("date", startDate, endDate);
    }
}
