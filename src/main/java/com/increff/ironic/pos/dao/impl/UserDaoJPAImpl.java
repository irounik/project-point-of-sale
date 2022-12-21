package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.ApiException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDaoJPAImpl extends AbstractJPADao<User, Integer> implements UserDao {

    @Override
    public String getEntityTableName() {
        return "users";
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public User select(String email) throws ApiException {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("email", email);
        List<User> result = selectWhereEquals(conditionMap);
        return result
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException("Could not find any user with email: " + email));
    }

}
