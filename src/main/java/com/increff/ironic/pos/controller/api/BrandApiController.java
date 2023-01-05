package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.BrandApiDto;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.exceptions.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping("/api/brands")
public class BrandApiController {

    @Autowired
    private BrandApiDto brandApiDto;

    @ApiOperation(value = "Adds an brand")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public void add(@RequestBody BrandForm form) throws ApiException {
        brandApiDto.add(form);
    }

    @ApiOperation(value = "Gets an brand by ID")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public BrandData get(@PathVariable Integer id) throws ApiException {
        return brandApiDto.get(id);
    }

    @ApiOperation(value = "Gets list of all categories")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<BrandData> getAll() {
        return brandApiDto.getAll();
    }

    @ApiOperation(value = "Updates an brand")
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable int id, @RequestBody BrandForm form) throws ApiException {
        brandApiDto.update(id, form);
    }

}
