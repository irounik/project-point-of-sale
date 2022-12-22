package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.BrandDtoValidator;
import com.increff.ironic.pos.model.data.BrandData;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.BrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
public class BrandApiController {

    @Autowired
    BrandService brandService;

    @ApiOperation(value = "Adds an brand")
    @RequestMapping(path = "/api/brand", method = RequestMethod.POST)
    public void add(@RequestBody BrandForm form) throws ApiException {
        Brand brand = BrandDtoValidator.convert(form);
        brandService.add(brand);
    }

    /* Not required
        @ApiOperation(value = "Deletes and brand")
        @RequestMapping(path = "/api/brand/{id}", method = RequestMethod.DELETE)
        public void delete(@PathVariable int id) throws ApiException {
            brandService.delete(id);
        }
     */

    @ApiOperation(value = "Gets an brand by ID")
    @RequestMapping(path = "/api/brand/{id}", method = RequestMethod.GET)
    public BrandData get(@PathVariable int id) throws ApiException {
        Brand p = brandService.get(id);
        return BrandDtoValidator.convert(p);
    }

    @ApiOperation(value = "Gets list of all categories")
    @RequestMapping(path = "/api/brand", method = RequestMethod.GET)
    public List<BrandData> getAll() {
        return brandService
                .getAll()
                .stream()
                .map(BrandDtoValidator::convert)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Updates an brand")
    @RequestMapping(path = "/api/brand/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable int id, @RequestBody BrandForm f) throws ApiException {
        Brand p = BrandDtoValidator.convert(f);
        brandService.update(id, p);
    }

}
