package com.increff.ironic.pos.dao;

import com.increff.ironic.pos.pojo.Employee;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmployeeInMemDao implements EmployeeDao {

    private HashMap<Integer, Employee> rows;
    private int lastId;

    @PostConstruct
    public void init() {
        rows = new HashMap<>();
    }

    public void insert(Employee p) {
        lastId++;
        p.setId(lastId);
        rows.put(lastId, p);
    }


    @Override
    public void delete(Integer id) {
        rows.remove(id);
    }

    @Override
    public Employee select(Integer id) {
        return rows.get(id);
    }

    @Override
    public List<Employee> selectAll() {
        return new ArrayList<>(rows.values());
    }


    @Override
    public void update(Integer id, Employee updated) {
        rows.put(id, updated);
    }

}
