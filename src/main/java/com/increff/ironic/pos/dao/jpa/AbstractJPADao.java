package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.pojo.BaseEntity;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractJPADao<Entity extends BaseEntity<ID>, ID extends Serializable> {

    @PersistenceContext
    protected EntityManager entityManager;

    private Class<Entity> clazz;
    private CriteriaBuilder criteriaBuilder;
    private String primaryKeyName;

    @PostConstruct
    private void init() {
        criteriaBuilder = entityManager.getCriteriaBuilder();
        primaryKeyName = getPrimaryKeyColumnName();
        clazz = getEntityClass();
    }

    protected abstract Class<Entity> getEntityClass();

    protected abstract String getPrimaryKeyColumnName();

    public Entity insert(Entity entity) {
        entityManager.persist(entity);
        return entity; // Returning entity after generating ID
    }

    public void delete(ID id) {
        CriteriaDelete<Entity> delete = criteriaBuilder.createCriteriaDelete(clazz);
        Root<Entity> root = delete.from(clazz);

        Path<Object> primaryKey = root.get(primaryKeyName);
        delete.where(criteriaBuilder.equal(primaryKey, id));

        Query query = entityManager.createQuery(delete);
        query.executeUpdate();
    }

    public Entity select(ID id) {
        return entityManager.find(getEntityClass(), id);
    }

    public List<Entity> selectAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class<Entity> clazz = getEntityClass();

        CriteriaQuery<Entity> query = cb.createQuery(clazz);
        query.from(clazz);

        return entityManager.createQuery(query).getResultList();
    }

    public void update(Entity entity) {
        entityManager.merge(entity);
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
