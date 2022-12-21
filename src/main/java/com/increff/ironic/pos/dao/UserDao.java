package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.parent.CrudDao;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.ApiException;

public interface UserDao extends CrudDao<User, Integer> {

    User select(String email) throws ApiException;

}
