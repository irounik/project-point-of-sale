package com.increff.ironic.pos.dao.parent;

import java.util.List;
import java.util.Map;

public interface CrudDao<Entity, ID> {

    void insert(Entity entity);

    void delete(ID id);

    Entity select(ID id);

    List<Entity> selectAll();

    void update(ID primaryKey, Entity updatedEntity);

    List<Entity> selectWhereEquals(Map<String, Object> properties);

}
