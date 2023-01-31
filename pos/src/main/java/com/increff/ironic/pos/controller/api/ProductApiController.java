package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.ProductApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Api
@RestController
@RequestMapping(path = "/api/products")
public class ProductApiController {

    private final ProductApiDto productApiDto;

    @Autowired
    public ProductApiController(ProductApiDto productApiDto) {
        this.productApiDto = productApiDto;
    }

    @ApiOperation(value = "Adds an product")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public void add(@RequestBody ProductForm form) throws ApiException {
        productApiDto.add(form);
    }

    @ApiOperation(value = "Gets an product by ID")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public ProductData get(@PathVariable Integer id) throws ApiException {
        return productApiDto.getById(id);
    }

    @ApiOperation(value = "Gets list of all products")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<ProductData> getAll(@RequestParam(required = false) String barcode) throws ApiException {
        if (barcode != null) {
            return Collections.singletonList(productApiDto.getByBarcode(barcode));
        } else {
            return productApiDto.getAll();
        }
    }

    @ApiOperation(value = "Updates an product")
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @RequestBody ProductForm form) throws ApiException {
        productApiDto.update(id, form);
    }

}
