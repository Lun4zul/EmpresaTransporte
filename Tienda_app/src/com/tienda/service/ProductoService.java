package com.tienda.service;

import com.tienda.GestorDB;
import com.tienda.Producto;

import java.util.List;

/**
 * Servicio SOA para consulta, catálogo y administración de productos / pasajes.
 */
public class ProductoService {

    public List<Producto> listarProductos() {
        return GestorDB.obtenerProductos();
    }

    public boolean guardarProducto(String nombre, double precio) {
        if (nombre == null || nombre.trim().isEmpty() || precio <= 0) {
            return false;
        }
        return GestorDB.agregarProducto(nombre.trim(), precio);
    }

    public boolean eliminarProducto(int id) {
        if (id <= 0) {
            return false;
        }
        return GestorDB.eliminarProducto(id);
    }
}
