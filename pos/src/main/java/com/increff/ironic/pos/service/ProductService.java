package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

import static com.increff.ironic.pos.util.NormalizationUtil.normalize;

@Service
public class ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public ProductPojo get(Integer id) throws ApiException {
        ProductPojo productPojo = productDao.select(id);
        if (productPojo == null) {
            throw new ApiException("Can't find any product with ID: " + id);
        }
        return productPojo;
    }

    public ProductPojo getByBarcode(String barcode) throws ApiException {
        ProductPojo productPojo = productDao.getByBarcode(barcode);
        if (productPojo == null) {
            throw new ApiException("Can't find any product with barcode: " + barcode);
        }
        return productPojo;
    }

    public List<ProductPojo> getAll() {
        return productDao.selectAll();
    }

    private void normalizeProduct(ProductPojo productPojo) {
        productPojo.setName(normalize(productPojo.getName()));
        productPojo.setPrice(normalize(productPojo.getPrice()));
        productPojo.setBarcode(normalize(productPojo.getBarcode()));
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(ProductPojo productPojo) throws ApiException {
        normalizeProduct(productPojo);

        String barcode = productPojo.getBarcode();
        boolean isDuplicate = productDao.getByBarcode(barcode) != null;

        if (isDuplicate) {
            String message = "A product with barcode: " + productPojo.getBarcode() + " already exists!";
            throw new ApiException(message);
        }

        productDao.insert(productPojo);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(ProductPojo updatedProductPojo) throws ApiException {
        get(updatedProductPojo.getId()); // check if product exists
        normalizeProduct(updatedProductPojo);
        ProductPojo productPojo = productDao.getByBarcode(updatedProductPojo.getBarcode());

        boolean isBarcodeAlreadyUsed = productPojo != null && !productPojo.getId().equals(updatedProductPojo.getId());
        if (isBarcodeAlreadyUsed) {
            throw new ApiException("Barcode " + updatedProductPojo.getBarcode() + " is already being used!");
        }

        productDao.update(updatedProductPojo);
    }

    public List<ProductPojo> getProductsByIds(List<Integer> idList) throws ApiException {
        List<ProductPojo> productEntities = new LinkedList<>();

        for (Integer id : idList) {
            ProductPojo productPojo = get(id);
            productEntities.add(productPojo);
        }

        return productEntities;
    }

    public void validateSellingPrice(ProductPojo productPojo, Double sellingPrice) throws ApiException {
        boolean isPriceGreaterThanMRP = sellingPrice > productPojo.getPrice();
        if (isPriceGreaterThanMRP) {
            throw new ApiException("Selling price can't be more than MRP!, for ");
        }
    }

}
