package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.parent.CrudDao;
import com.increff.ironic.pos.pojo.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryDao extends CrudDao<Category, Integer> {
}
