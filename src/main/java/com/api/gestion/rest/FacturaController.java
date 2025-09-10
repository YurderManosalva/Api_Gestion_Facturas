package com.api.gestion.rest;

import com.api.gestion.model.Factura;
import com.api.gestion.security.jwt.JwtFilter;
import com.api.gestion.service.impl.FacturaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private FacturaServiceImpl facturaServiceImpl;

    @Autowired
    private JwtFilter jwtFilter;

    @PostMapping("/crear")
    public ResponseEntity<Factura> crearFactura(@RequestBody Factura factura) {
        factura.setCreadoPor(jwtFilter.getCurrentUser());
        return ResponseEntity.ok(facturaServiceImpl.crearFactura(factura));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarFacturas() {
        return ResponseEntity.ok(facturaServiceImpl.listarFacturas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerFactura(@PathVariable Long id) {
        return ResponseEntity.ok(facturaServiceImpl.buscarFactura(id));
    }
}
