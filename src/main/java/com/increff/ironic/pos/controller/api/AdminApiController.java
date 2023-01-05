package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UserService service;

    @Autowired
    public AdminApiController(UserService service) {
        this.service = service;
    }

    @ApiOperation(value = "Adds a user")
    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public void addUser(@RequestBody UserForm form) throws ApiException {
        User user = ConversionUtil.convertFormToPojo(form);
        service.add(user);
    }

    @ApiOperation(value = "Deletes a user")
    @RequestMapping(path = "/user/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable int id) throws ApiException {
        service.delete(id);
    }

    @ApiOperation(value = "Gets list of all users")
    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public List<UserData> getAllUser() {
        List<User> users = service.getAll();
        return users
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

}
