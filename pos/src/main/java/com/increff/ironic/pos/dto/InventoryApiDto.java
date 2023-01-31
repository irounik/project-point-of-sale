package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InventoryData;
import com.increff.ironic.pos.model.form.InventoryForm;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.InventoryService;
import com.increff.ironic.pos.service.InvoiceService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class InventoryApiDto {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private static final Logger logger = Logger.getLogger(InventoryApiDto.class);

    @Autowired
    public InventoryApiDto(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

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
        } catch (ApiException exception) {
            logger.error(exception.getMessage());
            return null;
        }
    }

    private void validate(InventoryForm form) throws ApiException {
        if (ValidationUtil.isNegative(form.getQuantity())) {
            throw new ApiException("Invalid input: 'quantity' should not be a negative number!");
        }
    }

}
