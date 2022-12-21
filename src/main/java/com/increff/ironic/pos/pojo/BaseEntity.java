package com.increff.ironic.pos.pojo;

import java.io.Serializable;

/**
 * Base class for all the entities.
 *
 * @param <ID> Data type of the primary key
 */
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    abstract public void setId(ID id);

    abstract public ID getId();

}
