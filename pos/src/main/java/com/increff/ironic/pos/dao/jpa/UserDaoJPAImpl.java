package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.pojo.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDaoJPAImpl extends AbstractJPADao<User, Integer> implements UserDao {

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public User selectByEmail(String email) {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("email", email);
        List<User> result = selectWhereEquals(conditionMap);
        return result
                .stream()
                .findFirst()
                .orElse(null);
    }

}
