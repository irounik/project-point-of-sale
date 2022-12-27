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

    private static String normalizeString(String input) {
        return input.trim().toLowerCase();
    }

    private static Double normalizeNumber(Double input) {
        return input;
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(ProductForm productForm) throws ApiException {
        validateForm(productForm);
        duplicateCheck(productForm);

        Product product = convert(productForm);
        normalize(product);

        productService.add(product);

        // Creating new item in inventory.
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);

        inventoryService.add(inventory);
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
        // Validating brand
        Brand brand = brandService.selectByNameAndCategory(
                form.getBrandName().toLowerCase(),
                form.getCategory().toLowerCase()
        );

        if (brand == null) {
            String message = "No brand exist with name: " + form.getBrandName() + " and category: " + form.getCategory();
            throw new ApiException(message);
        }
        return brand;
    }

    private void normalize(Product product) {
        product.setName(normalizeString(product.getName()));
        product.setPrice(normalizeNumber(product.getPrice()));
        product.setBarcode(normalizeString(product.getBarcode()));
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

    private void duplicateCheck(ProductForm form) throws ApiException {
        if (productService.isDuplicate(form.getBarcode())) {
            throw new ApiException("A product with barcode: " + form.getBarcode() + " already exists!");
        }
    }

    private void throwCantBeBlank(String field) throws ApiException {
        throw new ApiException("Invalid input: " + field + " can't be blank!");
    }

}
