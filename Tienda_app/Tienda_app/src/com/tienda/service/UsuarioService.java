package com.tienda.service;

import com.tienda.GestorDB;

/**
 * Servicio SOA para autenticación y registro de usuarios.
 */
public class UsuarioService {

    public boolean autenticar(String usuario, String clave) {
        if (usuario == null || usuario.trim().isEmpty() || clave == null || clave.trim().isEmpty()) {
            return false;
        }
        return GestorDB.validarLogin(usuario.trim(), clave.trim());
    }

    public boolean registrar(String usuario, String clave) {
        if (usuario == null || usuario.trim().isEmpty() || clave == null || clave.trim().isEmpty()) {
            return false;
        }
        return GestorDB.registrarUsuario(usuario.trim(), clave.trim());
    }
}
