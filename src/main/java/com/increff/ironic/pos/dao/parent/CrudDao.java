package com.increff.ironic.pos.dao.parent;

import com.increff.ironic.pos.service.ApiException;

import java.util.List;

public interface CrudDao<Entity, ID> {

    void insert(Entity entity);

    void delete(ID id) throws ApiException;

    Entity select(ID entity) throws ApiException;

    List<Entity> selectAll();

    void update(ID primaryKey, Entity updatedEntity);

}
