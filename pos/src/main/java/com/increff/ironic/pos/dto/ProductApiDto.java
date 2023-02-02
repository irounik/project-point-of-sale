package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
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
    public Product add(ProductForm productForm) throws ApiException {
        Product product = preprocess(productForm);
        productService.add(product);

        // Creating new item in inventory.
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventoryService.add(inventory);
        return product;
    }

    private Product preprocess(ProductForm productForm) throws ApiException {
        validateForm(productForm);
        String brandName = productForm.getBrandName();
        String category = productForm.getCategory();
        Brand brand = brandService.selectByNameAndCategory(brandName, category);
        return ConversionUtil.convertFormToPojo(productForm, brand);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        if (!ValidationUtil.isValidBarcode(barcode)) {
            throw new ApiException("Invalid barcode!");
        }
        Product product = productService.getByBarcode(barcode);
        Brand brand = brandService.get(product.getBrandId());
        return ConversionUtil.convertPojoToData(product, brand);
    }

    public ProductData getById(Integer id) throws ApiException {
        Product product = productService.get(id);
        Brand brand = brandService.get(product.getBrandId());
        return ConversionUtil.convertPojoToData(product, brand);
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
        Product product = preprocess(form);
        product.setId(id);
        productService.update(product);
    }

    private ProductData convert(Product product) {
        try {
            Brand brand = brandService.get(product.getBrandId());
            return ConversionUtil.convertPojoToData(product, brand);
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
