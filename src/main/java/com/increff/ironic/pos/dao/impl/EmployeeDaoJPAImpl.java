package com.increff.ironic.pos.dao.impl;

import com.increff.ironic.pos.dao.EmployeeDao;
import com.increff.ironic.pos.dao.parent.AbstractJPADao;
import com.increff.ironic.pos.pojo.Employee;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDaoJPAImpl extends AbstractJPADao<Employee, Integer> implements EmployeeDao {

    @Override
    protected String getEntityTableName() {
        return "employee";
    }

    @Override
    protected Class<Employee> getEntityClass() {
        return Employee.class;
    }

    @Override
    protected String getPrimaryKeyColumnName() {
        return "id";
    }

}
