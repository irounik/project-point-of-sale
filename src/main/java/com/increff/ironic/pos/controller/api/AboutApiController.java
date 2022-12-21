package com.increff.ironic.pos.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.increff.ironic.pos.model.AboutAppData;
import com.increff.ironic.pos.service.AboutAppService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class AboutApiController {

    @Autowired
    private AboutAppService service;

    @ApiOperation(value = "Gives application name and version")
    @RequestMapping(path = "/api/about", method = RequestMethod.GET)
    public AboutAppData getDetails() {
        AboutAppData data = new AboutAppData();
        data.setName(service.getName());
        data.setVersion(service.getVersion());
        return data;
    }

}
