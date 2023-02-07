package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.InventoryApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/inventory")
public class InventoryApiController {

    private final InventoryApiDto inventoryApiDto;

    @Autowired
    public InventoryApiController(InventoryApiDto inventoryApiDto) {
        this.inventoryApiDto = inventoryApiDto;
    }

    @ApiOperation(value = "Get inventory by ID")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public InventoryData get(@PathVariable Integer id) throws ApiException {
        return inventoryApiDto.get(id);
    }

    @ApiOperation(value = "Gets all inventories")
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<InventoryData> getAll() {
        return inventoryApiDto.getAll();
    }

    @ApiOperation(value = "Update inventory")
    @RequestMapping(path = "/", method = RequestMethod.PUT)
    public InventoryData update(@RequestBody InventoryForm inventoryForm) throws ApiException {
        return inventoryApiDto.update(inventoryForm);
    }

}
