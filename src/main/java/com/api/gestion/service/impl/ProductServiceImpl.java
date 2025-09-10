package com.api.gestion.service.impl;

import com.api.gestion.model.Product;
import com.api.gestion.repository.ProductRepository;
import com.api.gestion.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product crearProducto(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> listarProductos() {
        return productRepository.findAll();
    }

    @Override
    public Product buscarProducto(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Factura no encontrada"));
    }
}
