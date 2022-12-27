package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.ProductApiDto;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.service.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
public class ProductApiController {

    @Autowired
    ProductApiDto productApiDto;

    @ApiOperation(value = "Adds an product")
    @RequestMapping(path = "/api/products", method = RequestMethod.POST)
    public void add(@RequestBody ProductForm form) throws ApiException {
        productApiDto.add(form);
    }

    @ApiOperation(value = "Gets an product by barcode")
    @RequestMapping(path = "/api/products/{id}", method = RequestMethod.GET)
    public ProductData get(@PathVariable Integer id) throws ApiException {
        return productApiDto.getById(id);
    }

    @ApiOperation(value = "Gets list of all categories")
    @RequestMapping(path = "/api/products", method = RequestMethod.GET)
    public List<ProductData> getAll() {
        return productApiDto.getAll();
    }

    @ApiOperation(value = "Updates an product")
    @RequestMapping(path = "/api/products/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @RequestBody ProductForm form) throws ApiException {
        productApiDto.update(id, form);
    }

}
