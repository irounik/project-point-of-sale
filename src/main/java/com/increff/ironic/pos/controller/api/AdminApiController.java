package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.AdminApiDto;
import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AdminApiDto adminApiDto;

    @Autowired
    public AdminApiController(AdminApiDto adminApiDto) {
        this.adminApiDto = adminApiDto;
    }

    @ApiOperation(value = "Adds a user")
    @RequestMapping(path = "/user", method = RequestMethod.POST)
    public void addUser(@RequestBody UserForm form) throws ApiException {
        adminApiDto.add(form);
    }

    @ApiOperation(value = "Deletes a user")
    @RequestMapping(path = "/user/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable int id) throws ApiException {
        adminApiDto.delete(id);
    }

    @ApiOperation(value = "Gets list of all users")
    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public List<UserData> getAllUser() {
        return adminApiDto.getAll();
    }

}
