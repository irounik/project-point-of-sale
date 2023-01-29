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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.increff.ironic.pos.util.NormalizationUtil.normalize;
import static com.increff.ironic.pos.util.ValidationUtil.isBlank;
import static com.increff.ironic.pos.util.ValidationUtil.isPositiveNumber;

@Component
public class ProductApiDto {

    private final ProductService productService;
    private final BrandService brandService;
    private final InventoryService inventoryService;

    @Autowired
    public ProductApiDto(ProductService productService, BrandService brandService, InventoryService inventoryService) {
        this.productService = productService;
        this.brandService = brandService;
        this.inventoryService = inventoryService;
    }

    @Transactional
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
        return convert(productForm);
    }

    public ProductData getByBarcode(String barcode) throws ApiException {
        Product product = productService.getByBarcode(barcode);
        Brand brand = brandService.get(product.getBrandId());
        return ConversionUtil.convertPojoToData(product, brand);
    }

    public List<ProductData> getAll() {
        return productService
                .getAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public void update(Integer id, ProductForm form) throws ApiException {
        validateForm(form);
        Product product = convert(form);
        product.setId(id);
        productService.update(product);
    }

    // TODO: 29/01/23 move to some conversionUtil
    private Product convert(ProductForm form) throws ApiException {
        Brand brand = getBrand(form);
        return ConversionUtil.convertFormToPojo(form, brand);
    }

    // TODO: 29/01/23 move normalise to api
    private Brand getBrand(ProductForm form) throws ApiException {
        validateForm(form);
        String brandName = normalize(form.getBrandName());
        String brandCategory = normalize(form.getCategory());
        return brandService.selectByNameAndCategory(brandName, brandCategory);
    }

    private ProductData convert(Product product) {
        try {
            Brand brand = brandService.get(product.getBrandId());
            return ConversionUtil.convertPojoToData(product, brand);
        } catch (Exception e) {
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

        if (!isPositiveNumber(form.getPrice())) {
            throw new ApiException("Invalid input: price can only be a positive number!");
        }

    }

    // TODO: 29/01/23 move to some commomn place so that you can use this from any class
    private void throwCantBeBlank(String field) throws ApiException {
        throw new ApiException("Invalid input: " + field + " can't be blank!");
    }

}
