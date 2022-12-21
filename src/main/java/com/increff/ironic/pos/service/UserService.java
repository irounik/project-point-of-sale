package com.increff.ironic.pos.service;

import java.util.List;

import javax.transaction.Transactional;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserDao dao;

    @Transactional
    public void add(User p) throws ApiException {
        normalize(p);
        User existing = dao.select(p.getEmail());
        if (existing != null) {
            throw new ApiException("User with given email already exists");
        }
        dao.insert(p);
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

    protected static void normalize(User p) {
        p.setEmail(p.getEmail().toLowerCase().trim());
        p.setRole(p.getRole().toLowerCase().trim());
    }

}
