package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.User;

public interface UserDao extends CrudDao<User, Integer> {

    User selectByEmail(String email);

}
