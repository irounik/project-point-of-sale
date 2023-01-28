package com.increff.ironic.pos.controller.api;

import com.increff.ironic.pos.dto.InventoryApiDto;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.exceptions.ApiException;
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

    // TODO: 24/01/23 any reason why you're using barcode instead of product id?
    @ApiOperation(value = "Gets an inventory by product barcode")
    @RequestMapping(path = "/{barcode}", method = RequestMethod.GET)
    public InventoryData get(@PathVariable String barcode) throws ApiException {
        return inventoryApiDto.getByBarcode(barcode);
    }

    @ApiOperation(value = "Gets list of the product with quantities")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<InventoryData> getAll() {
        return inventoryApiDto.getAll();
    }

    // TODO: 24/01/23 any reason why you're using barcode instead of product id?
    @ApiOperation(value = "Updates an inventory")
    @RequestMapping(path = "/{barcode}", method = RequestMethod.PUT)
    public void update(
            @PathVariable String barcode,
            @RequestBody InventoryForm inventoryForm
    ) throws ApiException {
        inventoryApiDto.update(barcode, inventoryForm);
    }

}
