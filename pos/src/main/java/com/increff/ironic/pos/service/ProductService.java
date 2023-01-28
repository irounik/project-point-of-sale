package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Product;
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

    public Product get(Integer id) throws ApiException {
        Product product = productDao.select(id);
        if (product == null) {
            throw new ApiException("Can't find any product with ID: " + id);
        }
        return product;
    }

    public Product getByBarcode(String barcode) throws ApiException {
        Product product = productDao.getByBarcode(barcode);
        if (product == null) {
            throw new ApiException("Can't find any product with barcode: " + barcode);
        }
        return product;
    }

    public List<Product> getAll() {
        return productDao.selectAll();
    }

    private void normalizeProduct(Product product) {
        product.setName(normalize(product.getName()));
        product.setPrice(normalize(product.getPrice()));
        product.setBarcode(normalize(product.getBarcode()));
    }

    @Transactional(rollbackOn = ApiException.class)
    public void add(Product product) throws ApiException {
        normalizeProduct(product);

        String barcode = product.getBarcode();
        boolean isDuplicate = productDao.getByBarcode(barcode) != null;

        if (isDuplicate) {
            String message = "A product with barcode: " + product.getBarcode() + " already exists!";
            throw new ApiException(message);
        }

        productDao.insert(product);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Product updatedProduct) throws ApiException {
        Product previous = get(updatedProduct.getId()); // Checking if product exists
        if (!previous.getBarcode().equals(updatedProduct.getBarcode())) {
            throw new ApiException("Barcode can't be changed!");
        }
        productDao.update(updatedProduct);
    }

    public List<Product> getProductsByIds(List<Integer> idList) throws ApiException {
        List<Product> products = new LinkedList<>();

        for (Integer id : idList) {
            Product product = get(id);
            products.add(product);
        }

        return products;
    }

}
