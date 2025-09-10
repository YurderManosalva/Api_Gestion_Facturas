package com.api.gestion.service.impl;

import com.api.gestion.model.DetalleFactura;
import com.api.gestion.model.Factura;
import com.api.gestion.model.Product;
import com.api.gestion.repository.FacturaRepository;
import com.api.gestion.repository.ProductRepository;
import com.api.gestion.service.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Factura crearFactura(Factura factura) {
        double total = 0.0;

        for (DetalleFactura detalle : factura.getDetalles()) {
            Product product = productRepository.findById(detalle.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            detalle.setProduct(product);

            detalle.setPrecioUnitario(product.getPrecio());

            double subtotal = product.getPrecio() * detalle.getCantidad();
            detalle.setSubtotal(subtotal);

            detalle.setFactura(factura);

            total += subtotal;
        }

        factura.setTotal(total);

        return facturaRepository.save(factura);
    }

    @Override
    public List<Factura> listarFacturas() {
        return facturaRepository.findAll();
    }

    @Override
    public Factura buscarFactura(Long id) {
        return facturaRepository.findById(id).orElseThrow(() -> new RuntimeException("Factura no encontrada"));
    }
}
