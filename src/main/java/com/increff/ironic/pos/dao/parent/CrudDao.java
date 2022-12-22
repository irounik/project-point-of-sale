package com.increff.ironic.pos.dao.parent;

import com.increff.ironic.pos.service.ApiException;

import java.util.List;

public interface CrudDao<Entity, ID> {

    void insert(Entity entity) throws ApiException;

    void delete(ID id) throws ApiException;

    Entity select(ID id) throws ApiException;

    List<Entity> selectAll();

    void update(ID primaryKey, Entity updatedEntity);

}
