package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.BrandApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping("/api/brands")
public class BrandApiController {

    private final BrandApiDto brandApiDto;

    @Autowired
    public BrandApiController(BrandApiDto brandApiDto) {
        this.brandApiDto = brandApiDto;
    }

    @ApiOperation(value = "Add brand")
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public BrandData add(@RequestBody BrandForm form) throws ApiException {
        return brandApiDto.add(form);
    }

    @ApiOperation(value = "Get brand by id")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public BrandData get(@PathVariable Integer id) throws ApiException {
        return brandApiDto.get(id);
    }

    @ApiOperation(value = "Get all brands")
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<BrandData> getAll() {
        return brandApiDto.getAll();
    }

    @ApiOperation(value = "Updates brand")
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public BrandData update(@PathVariable int id, @RequestBody BrandForm form) throws ApiException {
        return brandApiDto.update(id, form);
    }

}
