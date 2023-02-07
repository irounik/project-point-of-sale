package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.dao.base.CrudDao;
import com.increff.ironic.pos.pojo.UserPojo;

public interface UserDao extends CrudDao<UserPojo, Integer> {

    UserPojo selectByEmail(String email);

}
