package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.exceptions.ApiException;
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

    private final UserDao dao;

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
        this.dao = dao;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(User user) throws ApiException {
        normalizeUser(user);
        User existing = dao.select(user.getEmail());
        if (existing != null) {
            throw new ApiException("User with given email already exists");
        }

        String role = adminSet.contains(user.getEmail()) ? "supervisor" : "operator";
        user.setRole(role);

        dao.insert(user);
    }

    @Transactional(rollbackOn = ApiException.class)
    public User get(String email) throws ApiException {
        return dao.select(email);
    }

    @Transactional
    public List<User> getAll() {
        return dao.selectAll();
    }

    @Transactional
    public void delete(int id) throws ApiException {
        dao.delete(id);
    }

    private static void normalizeUser(User user) {
        user.setEmail(normalize(user.getEmail()));
        user.setRole(normalize(user.getRole()));
    }

}
