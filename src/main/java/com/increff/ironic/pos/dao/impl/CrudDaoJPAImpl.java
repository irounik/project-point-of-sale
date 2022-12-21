package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.CategoryDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Category;
import org.springframework.stereotype.Repository;

@Repository
public class CrudDaoJPAImpl extends AbstractJPADao<Category, Integer> implements CategoryDao {

    @Override
    protected String getEntityTableName() {
        return "category";
    }

    @Override
    protected Class<Category> getEntityClass() {
        return Category.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}