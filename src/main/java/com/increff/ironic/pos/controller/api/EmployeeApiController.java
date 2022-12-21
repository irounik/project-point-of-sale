package com.increff.ironic.pos.controller.api;

import java.util.ArrayList;
import java.util.List;

import com.increff.ironic.pos.pojo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.increff.ironic.pos.model.EmployeeData;
import com.increff.ironic.pos.model.EmployeeForm;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.EmployeeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class EmployeeApiController {

    @Autowired
    private EmployeeService service;

    @ApiOperation(value = "Adds an employee")
    @RequestMapping(path = "/api/employee", method = RequestMethod.POST)
    public void add(@RequestBody EmployeeForm form) throws ApiException {
        Employee p = convert(form);
        service.add(p);
    }


    @ApiOperation(value = "Deletes and employee")
    @RequestMapping(path = "/api/employee/{id}", method = RequestMethod.DELETE)
    // /api/1
    public void delete(@PathVariable int id) throws ApiException {
        service.delete(id);
    }

    @ApiOperation(value = "Gets an employee by ID")
    @RequestMapping(path = "/api/employee/{id}", method = RequestMethod.GET)
    public EmployeeData get(@PathVariable int id) throws ApiException {
        Employee p = service.get(id);
        return convert(p);
    }

    @ApiOperation(value = "Gets list of all employees")
    @RequestMapping(path = "/api/employee", method = RequestMethod.GET)
    public List<EmployeeData> getAll() {
        List<Employee> list = service.getAll();
        List<EmployeeData> list2 = new ArrayList<EmployeeData>();
        for (Employee p : list) {
            list2.add(convert(p));
        }
        return list2;
    }

    @ApiOperation(value = "Updates an employee")
    @RequestMapping(path = "/api/employee/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable int id, @RequestBody EmployeeForm f) throws ApiException {
        Employee p = convert(f);
        service.update(id, p);
    }


    private static EmployeeData convert(Employee p) {
        EmployeeData d = new EmployeeData();
        d.setAge(p.getAge());
        d.setName(p.getName());
        d.setId(p.getId());
        return d;
    }

    private static Employee convert(EmployeeForm f) {
        Employee p = new Employee();
        p.setAge(f.getAge());
        p.setName(f.getName());
        return p;
    }

}
