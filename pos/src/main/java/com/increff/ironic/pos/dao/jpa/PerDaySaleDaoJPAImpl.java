package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.PerDaySaleDao;
import com.increff.ironic.pos.pojo.PerDaySalePojo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PerDaySaleDaoJPAImpl extends AbstractJPADao<PerDaySalePojo, Integer> implements PerDaySaleDao {

    @Override
    public List<PerDaySalePojo> getPerDaySaleBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return selectWhereBetween("date", startDate, endDate);
    }

}
