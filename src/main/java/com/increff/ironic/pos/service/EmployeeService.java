package com.increff.ironic.pos.service;

import java.util.List;
import javax.transaction.Transactional;

import com.increff.ironic.pos.dao.EmployeeDao;
import com.increff.ironic.pos.pojo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.increff.ironic.pos.util.StringUtil;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDao dao;

    @Transactional(rollbackOn = ApiException.class)
    public void add(Employee p) throws ApiException {
        normalize(p);
        if (StringUtil.isEmpty(p.getName())) {
            throw new ApiException("name cannot be empty");
        }
        dao.insert(p);
    }

    @Transactional
    public void delete(int id) throws ApiException {
        dao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public Employee get(int id) throws ApiException {
        return getCheck(id);
    }

    @Transactional
    public List<Employee> getAll() {
        return dao.selectAll();
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(int id, Employee p) throws ApiException {
        dao.update(id, p);
    }

    @Transactional
    public Employee getCheck(int id) throws ApiException {
        Employee p = dao.select(id);
        if (p == null) {
            throw new ApiException("Employee with given ID does not exit, id: " + id);
        }
        return p;
    }

    protected static void normalize(Employee p) {
        p.setName(StringUtil.toLowerCase(p.getName()));
    }
}
