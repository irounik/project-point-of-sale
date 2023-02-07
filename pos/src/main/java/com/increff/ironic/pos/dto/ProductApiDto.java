package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.pojo.BrandPojo;
import com.increff.ironic.pos.pojo.InventoryPojo;
import com.increff.ironic.pos.pojo.ProductPojo;
import com.increff.ironic.pos.service.BrandService;
import com.increff.ironic.pos.service.InventoryService;
import com.increff.ironic.pos.service.ProductService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.increff.ironic.pos.exceptions.ApiException.throwCantBeBlank;
import static com.increff.ironic.pos.util.ValidationUtil.isBlank;
import static com.increff.ironic.pos.util.ValidationUtil.isNegativeNumber;

@Component
public class ProductApiDto {

    private final ProductService productService;
    private final BrandService brandService;
    private final InventoryService inventoryService;
    private final Logger logger = Logger.getLogger(ProductApiDto.class);

    @Autowired
    public ProductApiDto(ProductService productService, BrandService brandService, InventoryService inventoryService) {
        this.productService = productService;
        this.brandService = brandService;
        this.inventoryService = inventoryService;
    }

    @Transactional(rollbackOn = ApiException.class)
    public ProductPojo add(ProductForm productForm) throws ApiException {
        ProductPojo productPojo = preprocess(productForm);
        productService.add(productPojo);

        // Creating new item in inventory.
        InventoryPojo inventoryPojo = new InventoryPojo();
        inventoryPojo.setProductId(productPojo.getId());
        inventoryPojo.setQuantity(0);
        inventoryService.add(inventoryPojo);
        return productPojo;
    }

    private ProductPojo preprocess(ProductForm productForm) throws ApiException {
        validateForm(productForm);
        String brandName = productForm.getBrandName();
        String category = productForm.getCategory();
        BrandPojo brandPojo = brandService.selectByNameAndCategory(brandName, category);
        return ConversionUtil.convertFormToPojo(productForm, brandPojo);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        if (!ValidationUtil.isValidBarcode(barcode)) {
            throw new ApiException("Invalid barcode!");
        }
        ProductPojo productPojo = productService.getByBarcode(barcode);
        BrandPojo brandPojo = brandService.get(productPojo.getBrandId());
        return ConversionUtil.convertPojoToData(productPojo, brandPojo);
    }

    public ProductData getById(Integer id) throws ApiException {
        ProductPojo productPojo = productService.get(id);
        BrandPojo brandPojo = brandService.get(productPojo.getBrandId());
        return ConversionUtil.convertPojoToData(productPojo, brandPojo);
    }

    public List<ProductData> getAll() {
        return productService
                .getAll()
                .stream()
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void update(Integer id, ProductForm form) throws ApiException {
        ProductPojo productPojo = preprocess(form);
        productPojo.setId(id);
        productService.update(productPojo);
    }

    private ProductData convert(ProductPojo productPojo) {
        try {
            BrandPojo brandPojo = brandService.get(productPojo.getBrandId());
            return ConversionUtil.convertPojoToData(productPojo, brandPojo);
        } catch (ApiException exception) {
            logger.error("Error occured while getting all products", exception);
            return null;
        }
    }

    private void validateForm(ProductForm form) throws ApiException {

        if (isBlank(form.getName())) {
            throwCantBeBlank("product name");
        }

        if (isBlank(form.getBarcode())) {
            throwCantBeBlank("barcode");
        }

        if (isBlank(form.getBrandName())) {
            throwCantBeBlank("brand name");
        }

        if (isBlank(form.getCategory())) {
            throwCantBeBlank("category");
        }

        if (isNegativeNumber(form.getPrice())) {
            throw new ApiException("Invalid input: price can only be a positive number!");
        }
    }
}
