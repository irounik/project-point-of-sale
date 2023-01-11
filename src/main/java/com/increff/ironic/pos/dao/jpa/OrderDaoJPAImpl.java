package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.OrderDao;
import com.increff.ironic.pos.pojo.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderDaoJPAImpl extends AbstractJPADao<Order, Integer> implements OrderDao {

    @Override
    protected Class<Order> getEntityClass() {
        return Order.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public List<Order> getOrderDuring(LocalDateTime startDate, LocalDateTime endDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> q = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = q.from(Order.class);
        Predicate condition = criteriaBuilder.between(root.get("time"), startDate, endDate);
        return entityManager.createQuery(q.where(condition)).getResultList();
    }

}