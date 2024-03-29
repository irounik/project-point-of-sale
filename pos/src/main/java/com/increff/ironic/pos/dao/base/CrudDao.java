package com.increff.ironic.pos.dao.base;

import java.util.List;
import java.util.Map;

public interface CrudDao<Entity, ID> {

    Entity insert(Entity entity);

    Boolean delete(ID id);

    Entity select(ID id);

    List<Entity> selectAll();

    Entity update(Entity updatedEntity);

    List<Entity> selectWhereEquals(Map<String, Object> properties);

}
