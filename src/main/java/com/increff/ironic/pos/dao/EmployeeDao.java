package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.pojo.EmployeePojo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@Repository
public class EmployeeDao extends AbstractDao {

    private static final String DELETE_BY_ID = "delete from EmployeePojo p where id=:id";
    private static final String SELECT_BY_ID = "select p from EmployeePojo p where id=:id";
    private static final String SELECT_ALL = "select p from EmployeePojo p";

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void insert(EmployeePojo p) {
        em.persist(p);
    }

    public int delete(int id) {
        Query query = em.createQuery(DELETE_BY_ID);
        query.setParameter("id", id);
        return query.executeUpdate();
    }

    public EmployeePojo select(int id) {
        TypedQuery<EmployeePojo> query = getQuery(SELECT_BY_ID, EmployeePojo.class);
        query.setParameter("id", id);
        return getSingle(query);
    }

    public List<EmployeePojo> selectAll() {
        TypedQuery<EmployeePojo> query = getQuery(SELECT_ALL, EmployeePojo.class);
        return query.getResultList();
    }

    public void update(EmployeePojo p) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<EmployeePojo> updateCriteria = cb.createCriteriaUpdate(EmployeePojo.class);
        Root<EmployeePojo> root = updateCriteria.from(EmployeePojo.class);
        updateCriteria.where(cb.equal(root.get("id"), p.getId()));

        updateCriteria.set("name", p.getName());
        updateCriteria.set("age", p.getAge());

        Query query = em.createQuery(updateCriteria);
        query.executeUpdate();
    }

}
