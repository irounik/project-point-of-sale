package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.pojo.BaseEntity;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rounik
 */
public abstract class AbstractJPADao<Entity extends BaseEntity<ID>, ID extends Serializable> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<Entity> clazz;
    private CriteriaBuilder criteriaBuilder;

    @PostConstruct
    private void init() {
        criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    @SuppressWarnings("unchecked")
    public AbstractJPADao() {
        this.clazz = (Class<Entity>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Entity insert(Entity entity) {
        entityManager.persist(entity);
        return entity; // Returning entity after generating ID
    }

    // Return boolean
    public Boolean delete(ID id) {
        Entity entity = select(id);
        if (entity == null) return false;
        entityManager.remove(entity);
        return true;
    }

    public Entity select(ID id) {
        return entityManager.find(clazz, id);
    }

    public List<Entity> selectAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Entity> query = cb.createQuery(clazz);
        query.from(clazz);

        return entityManager.createQuery(query).getResultList();
    }

    public Entity update(Entity entity) {
        entityManager.merge(entity);
        return entity;
    }

    public List<Entity> selectWhereEquals(Map<String, Object> conditions) {
        CriteriaQuery<Entity> q = criteriaBuilder.createQuery(clazz);
        Root<Entity> root = q.from(clazz);
        Predicate[] predicates = getEqualityPredicates(root, conditions);
        return selectWhere(q, predicates);
    }

    protected List<Entity> selectWhere(CriteriaQuery<Entity> q, Predicate[] predicates) {
        return entityManager
                .createQuery(q.where(predicates))
                .getResultList();
    }

    <Value extends Comparable<? super Value>> List<Entity> selectWhereBetween(
            String key,
            Value lowerLimit,
            Value upperLimit
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Entity> q = criteriaBuilder.createQuery(clazz);
        Root<Entity> root = q.from(clazz);
        Predicate condition = criteriaBuilder.between(root.get(key), lowerLimit, upperLimit);
        return entityManager.createQuery(q.where(condition)).getResultList();
    }

    private Predicate[] getEqualityPredicates(Root<Entity> root, Map<String, Object> conditions) {
        List<Predicate> predicates = new LinkedList<>();
        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            Predicate predicate = criteriaBuilder.equal(root.get(condition.getKey()), condition.getValue());
            predicates.add(predicate);
        }

        Predicate[] predicateArray = new Predicate[predicates.size()];
        return predicates.toArray(predicateArray);
    }

}
