package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.InventoryService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class InventoryApiDto {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    ProductService productService;

    public List<InventoryData> getAll() {
        return inventoryService
                .getAll()
                .stream()
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public InventoryData getByBarcode(String barcode) throws ApiException {
        Product product = productService.getByBarcode(barcode);
        Inventory inventory = inventoryService.get(product.getId());
        return ConversionUtil.convertPojoToData(inventory, product);
    }

    public void update(String barcode, InventoryForm form) throws ApiException {
        validate(form);
        Product product = productService.getByBarcode(barcode);
        Inventory inventory = ConversionUtil.convertFormToPojo(form, product);
        inventoryService.update(inventory);
    }

    private InventoryData convert(Inventory inventory) {
        try {
            Product product = productService.get(inventory.getProductId());
            return ConversionUtil.convertPojoToData(inventory, product);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void validate(InventoryForm form) throws ApiException {
        if (ValidationUtil.isNegative(form.getQuantity())) {
            throw new ApiException("Invalid input: 'quantity' should not be a negative number!");
        }
    }

}
