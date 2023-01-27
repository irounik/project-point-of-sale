package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.increff.ironic.pos.util.NormalizationUtil.normalize;

@Service
public class UserService {

    private final UserDao userDao;

    @Value("${admins}")
    String adminEmails;

    private Set<String> adminSet;

    @PostConstruct
    private void init() {
        adminSet = new HashSet<>();
        for (String email : adminEmails.split(",")) {
            adminSet.add(email.trim());
        }
    }

    @Autowired
    public UserService(UserDao dao) {
        this.userDao = dao;
    }

    @Transactional(rollbackOn = ApiException.class)
    public User add(User user) throws ApiException {
        normalizeUser(user);
        User existing = userDao.selectByEmail(user.getEmail());
        if (existing != null) {
            throw new ApiException("User with given email already exists");
        }

        UserRole role = adminSet.contains(user.getEmail()) ? UserRole.SUPERVISOR : UserRole.OPERATOR;
        user.setRole(role);

        return userDao.insert(user);
    }

    @Transactional(rollbackOn = ApiException.class)
    public User get(String email) throws ApiException {
        return userDao.selectByEmail(email);
    }

    @Transactional
    public List<User> getAll() {
        return userDao.selectAll();
    }

    @Transactional
    public void delete(int id) throws ApiException {
        userDao.delete(id);
    }

    private static void normalizeUser(User user) {
        user.setEmail(normalize(user.getEmail()));
    }

}
