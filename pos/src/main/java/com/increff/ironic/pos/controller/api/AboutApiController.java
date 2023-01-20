package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.AboutAppDto;
import com.increff.ironic.pos.model.data.AboutAppData;
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

    private final AboutAppDto dto;

    @Autowired
    public AboutApiController(AboutAppDto dto) {
        this.dto = dto;
    }

    @ApiOperation(value = "Gives application name and version")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public AboutAppData getDetails() {
        return dto.getDetails();
    }

}
