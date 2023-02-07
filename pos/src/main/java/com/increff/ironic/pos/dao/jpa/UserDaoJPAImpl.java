package com.increff.ironic.pos.dao.jpa;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.pojo.UserPojo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDaoJPAImpl extends AbstractJPADao<UserPojo, Integer> implements UserDao {

    @Override
    public Class<UserPojo> getEntityClass() {
        return UserPojo.class;
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public UserPojo selectByEmail(String email) {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("email", email);
        List<UserPojo> result = selectWhereEquals(conditionMap);
        return result
                .stream()
                .findFirst()
                .orElse(null);
    }

}
