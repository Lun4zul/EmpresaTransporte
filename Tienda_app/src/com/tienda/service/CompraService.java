package com.tienda.service;

import com.tienda.Compra;
import com.tienda.GestorDB;

import java.util.List;

/**
 * Servicio SOA para ventas, transacciones y registro de compras / pasajes.
 */
public class CompraService {

    public boolean procesarCompra(String usuario, String producto, int cantidad) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return false;
        }
        if (producto == null || producto.trim().isEmpty()) {
            return false;
        }
        if (cantidad <= 0) {
            return false;
        }
        return GestorDB.registrarCompra(usuario.trim(), producto.trim(), cantidad);
    }

    public List<Compra> listarComprasPorUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return GestorDB.obtenerTodasCompras();
        }
        return GestorDB.obtenerComprasPorUsuario(usuario.trim());
    }

    public List<Compra> listarTodasLasCompras() {
        return GestorDB.obtenerTodasCompras();
    }
}
