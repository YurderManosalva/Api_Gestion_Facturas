package com.api.gestion.service;


import com.api.gestion.model.Product;

import java.util.List;

public interface ProductService {

    Product crearProducto(Product product);

    List<Product> listarProductos();

    Product buscarProducto(Long id);
}
