package com.api.gestion.service;

import com.api.gestion.model.Factura;

import java.util.List;

public interface FacturaService {
    Factura crearFactura(Factura factura);

    List<Factura> listarFacturas();

    Factura buscarFactura(Long id);
}
