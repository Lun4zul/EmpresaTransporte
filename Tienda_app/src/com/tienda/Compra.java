package com.tienda;

public class Compra {
    private int id;
    private String usuario;
    private String producto;
    private int cantidad;

    public Compra(int id, String usuario, String producto, int cantidad) {
        this.id = id;
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "Compra #" + id + " - Usuario: " + usuario + " - Producto: " + producto + " (Cant: " + cantidad + ")";
    }
}
