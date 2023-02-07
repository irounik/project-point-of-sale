package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySalePojo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PerDaySaleDaoJPAImpl extends AbstractJPADao<PerDaySalePojo, Integer> implements PerDaySaleDao {

    @Override
    protected Class<PerDaySalePojo> getEntityClass() {
        return PerDaySalePojo.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public List<PerDaySalePojo> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return selectWhereBetween("date", startDate, endDate);
    }
}
