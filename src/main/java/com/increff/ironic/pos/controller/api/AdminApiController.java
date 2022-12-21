package com.increff.ironic.pos.controller.api;

import java.util.ArrayList;
import java.util.List;

import com.increff.ironic.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class AdminApiController {

    @Autowired
    private UserService service;

    @ApiOperation(value = "Adds a user")
    @RequestMapping(path = "/api/admin/user", method = RequestMethod.POST)
    public void addUser(@RequestBody UserForm form) throws ApiException {
        User p = convert(form);
        service.add(p);
    }

    @ApiOperation(value = "Deletes a user")
    @RequestMapping(path = "/api/admin/user/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable int id) throws ApiException {
        service.delete(id);
    }

    @ApiOperation(value = "Gets list of all users")
    @RequestMapping(path = "/api/admin/user", method = RequestMethod.GET)
    public List<UserData> getAllUser() {
        List<User> list = service.getAll();
        List<UserData> list2 = new ArrayList<UserData>();
        for (User p : list) {
            list2.add(convert(p));
        }
        return list2;
    }

    private static UserData convert(User p) {
        UserData d = new UserData();
        d.setEmail(p.getEmail());
        d.setRole(p.getRole());
        d.setId(p.getId());
        return d;
    }

    private static User convert(UserForm f) {
        User p = new User();
        p.setEmail(f.getEmail());
        p.setRole(f.getRole());
        p.setPassword(f.getPassword());
        return p;
    }

}
