package com.api.gestion.rest;

import com.api.gestion.model.Product;
import com.api.gestion.service.impl.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
public class ProductController {

    @Autowired
    private ProductServiceImpl productoServiceImpl;

    @PostMapping("/crear")
    public ResponseEntity<Product> crearProducto(@RequestBody Product product){
        return ResponseEntity.ok(productoServiceImpl.crearProducto(product));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarProductos() {
        return ResponseEntity.ok(productoServiceImpl.listarProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> obtenerProducto(@PathVariable Long id){
        return ResponseEntity.ok(productoServiceImpl.buscarProducto(id));
    }
}
