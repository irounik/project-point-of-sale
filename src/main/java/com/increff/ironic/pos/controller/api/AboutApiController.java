package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.model.data.AboutAppData;
import com.increff.ironic.pos.service.AboutAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(path = "api/about")
public class AboutApiController {

    @Autowired
    private AboutAppService service;

    @ApiOperation(value = "Gives application name and version")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public AboutAppData getDetails() {
        AboutAppData data = new AboutAppData();
        data.setName(service.getName());
        data.setVersion(service.getVersion());
        return data;
    }

}
