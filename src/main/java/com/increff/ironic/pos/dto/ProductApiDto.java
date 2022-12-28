package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.ProductData;
import com.increff.ironic.pos.model.form.ProductForm;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import com.increff.ironic.pos.service.ApiException;
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

    @Autowired
    BrandService brandService;

    @Autowired
    ProductService productService;

    @Autowired
    InventoryService inventoryService;

    @Transactional(rollbackOn = ApiException.class)
    public void add(ProductForm productForm) throws ApiException {
        Product product = preprocess(productForm);
        productService.add(product);

        // Creating new item in inventory.
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);

        inventoryService.add(inventory);
    }

    private Product preprocess(ProductForm productForm) throws ApiException {
        validateForm(productForm);
        Product product = convert(productForm);
        normalizeProduct(product);
        return product;
    }

    public ProductData getById(Integer id) throws ApiException {
        Product product = productService.get(id);
        return convert(product);
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

    public Product convert(ProductForm form) throws ApiException {
        Brand brand = getBrand(form);
        return ConversionUtil.convertFormToPojo(form, brand);
    }

    private Brand getBrand(ProductForm form) throws ApiException {
        validateForm(form);
        return brandService.selectByNameAndCategory(
                form.getBrandName().toLowerCase(),
                form.getCategory().toLowerCase()
        );
    }

    private void normalizeProduct(Product product) {
        product.setName(normalize(product.getName()));
        product.setPrice(normalize(product.getPrice()));
        product.setBarcode(normalize(product.getBarcode()));
    }

    public ProductData convert(Product product) {
        try {
            Brand brand = brandService.get(product.getBrandId());
            return ConversionUtil.convertPojoToData(product, brand);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void validateForm(ProductForm form) throws ApiException {

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

    private void throwCantBeBlank(String field) throws ApiException {
        throw new ApiException("Invalid input: " + field + " can't be blank!");
    }

}
