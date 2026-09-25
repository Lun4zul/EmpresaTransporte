package com.tienda;

/**
 * Gestor de sesión en memoria para mantener el usuario autenticado
 * a través de los diferentes módulos de la aplicación.
 */
public class Sesion {
    private static Sesion instancia;
    private String usuarioActual;

    private Sesion() {}

    public static synchronized Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public String getUsuarioActual() {
        return usuarioActual != null ? usuarioActual : "Invitado";
    }

    public void setUsuarioActual(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public boolean estaAutenticado() {
        return usuarioActual != null && !usuarioActual.trim().isEmpty();
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}
