package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.ProductDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.pojo.Brand;
import com.increff.ironic.pos.pojo.Inventory;
import com.increff.ironic.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

@Service
public class ProductService {

    private final ProductDao productDao;
    private final InventoryService inventoryService;
    private final BrandService brandService;

    @Autowired
    public ProductService(
            ProductDao productDao,
            InventoryService inventoryService,
            BrandService brandService
    ) {
        this.productDao = productDao;
        this.inventoryService = inventoryService;
        this.brandService = brandService;
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

    @Transactional(rollbackOn = ApiException.class)
    public void add(Product product) throws ApiException {
        if (isDuplicate(product.getBarcode())) {
            String message = "A product with barcode: " + product.getBarcode() + " already exists!";
            throw new ApiException(message);
        }

        productDao.insert(product);

        // Creating new item in inventory.
        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventoryService.add(inventory);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void delete(Integer id) throws ApiException {
        productDao.delete(id);
    }

    @Transactional(rollbackOn = ApiException.class)
    public void update(Product updatedProduct) throws ApiException {
        Product previous = get(updatedProduct.getId()); // Checking if product exists
        if (previous.getBarcode().equals(updatedProduct.getBarcode())) {
            throw new ApiException("Barcode can't be changed!");
        }
        productDao.update(updatedProduct);
    }

    public boolean isDuplicate(String barcode) {
        return productDao.getByBarcode(barcode) != null;
    }

    public List<Product> getProductsByIds(List<Integer> idList) throws ApiException {
        List<Product> products = new LinkedList<>();

        for (Integer id : idList) {
            Product product = get(id);
            products.add(product);
        }

        return products;
    }

    public Brand getBrand(Product product) throws ApiException {
        return brandService.get(product.getBrandId());
    }

    public Brand getBrand(String name, String category) throws ApiException {
        return brandService.selectByNameAndCategory(name, category);
    }

}
