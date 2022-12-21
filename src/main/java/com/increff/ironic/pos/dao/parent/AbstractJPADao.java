package com.increff.ironic.pos.dao.parent;

import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Map;

public abstract class AbstractJPADao<Entity, ID> implements CrudDao<Entity, ID> {

    @PersistenceContext
    private EntityManager entityManager;

    private Class<Entity> clazz;
    private CriteriaBuilder criteriaBuilder;
    private String primaryKeyName;

    @PostConstruct
    private void init() {
        criteriaBuilder = entityManager.getCriteriaBuilder();
        primaryKeyName = getPrimaryKeyColumnName();
        clazz = getEntityClass();
    }

    protected abstract String getEntityTableName();

    protected abstract Class<Entity> getEntityClass();

    protected abstract String getPrimaryKeyColumnName();

    @Override
    public void insert(Entity entity) {
        entityManager.persist(entity);
    }

    @Override
    public void delete(ID id) throws ApiException {
        CriteriaDelete<Entity> delete = criteriaBuilder.createCriteriaDelete(clazz);
        Root<Entity> root = delete.from(clazz);

        Path<Object> primaryKey = root.get(primaryKeyName);
        delete.where(criteriaBuilder.equal(primaryKey, id));

        Query query = entityManager.createQuery(delete);
        query.executeUpdate();
    }

    @Override
    public Entity select(ID id) throws ApiException {
        Entity entity = entityManager.find(getEntityClass(), id);
        if (entity == null) throw new ApiException(
                "Can't find " + getEntityTableName() + " with primary key: " + id
        );
        return entity;
    }

    @Override
    public List<Entity> selectAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class<Entity> clazz = getEntityClass();

        CriteriaQuery<Entity> query = cb.createQuery(clazz);
        query.from(clazz);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void update(ID id, Entity entity) {
        Map<String, Object> updatedAttributes = SerializationUtils.getAttributeMap(entity);
        updatedAttributes.put(primaryKeyName, id);

        Class<Entity> clazz = getEntityClass();

        CriteriaUpdate<Entity> update = criteriaBuilder.createCriteriaUpdate(clazz);
        Root<Entity> root = update.from(clazz);
        Path<Object> primaryKey = root.get(primaryKeyName);

        update.where(criteriaBuilder.equal(primaryKey, id));

        for (Map.Entry<String, Object> attribute : updatedAttributes.entrySet()) {
            update.set(attribute.getKey(), attribute.getValue());
        }

        Query query = entityManager.createQuery(update);
        query.executeUpdate();
    }

    protected List<Entity> selectWhereEquals(Map<String, Object> conditions) {
        CriteriaQuery<Entity> q = criteriaBuilder.createQuery(clazz);
        Root<Entity> root = q.from(clazz);

        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            criteriaBuilder.equal(root.get(condition.getKey()), condition.getValue());
        }

        return entityManager.createQuery(q).getResultList();
    }

}
