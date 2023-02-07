package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.auth.UserPrincipal;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.pojo.UserPojo;
import com.increff.ironic.pos.util.SecurityUtil;
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
    private String adminEmails;

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
    public UserPojo add(UserPojo userPojo) throws ApiException {
        normalizeUser(userPojo);
        UserPojo existing = userDao.selectByEmail(userPojo.getEmail());
        if (existing != null) {
            throw new ApiException("User with given email already exists");
        }

        UserRole role = adminSet.contains(userPojo.getEmail()) ? UserRole.SUPERVISOR : UserRole.OPERATOR;
        if (userPojo.getRole() == UserRole.NONE) {
            userPojo.setRole(role);
        }

        return userDao.insert(userPojo);
    }

    @Transactional(rollbackOn = ApiException.class)
    public UserPojo getByEmail(String email) throws ApiException {
        UserPojo userPojo = userDao.selectByEmail(email);
        if (userPojo == null) {
            throw new ApiException("No user found with email: " + email);
        }
        return userPojo;
    }

    @Transactional
    public List<UserPojo> getAll() {
        return userDao.selectAll();
    }

    @Transactional
    public void delete(int id) throws ApiException {
        UserPojo userPojo = getCheck(id);
        validateSelfDelete(userPojo);
        userDao.delete(id);
    }

    private void validateSelfDelete(UserPojo userPojoToDelete) throws ApiException {
        UserPrincipal currentUser = SecurityUtil.getPrincipal();
        if (currentUser != null && currentUser.getEmail().equals(userPojoToDelete.getEmail())) {
            throw new ApiException("User can't delete themselves!");
        }
    }

    private UserPojo getCheck(int id) throws ApiException {
        UserPojo userPojo = userDao.select(id);
        if (userPojo == null) {
            throw new ApiException("No user found with ID: " + id);
        }
        return userPojo;
    }

    private static void normalizeUser(UserPojo userPojo) {
        userPojo.setEmail(normalize(userPojo.getEmail()));
    }

}
