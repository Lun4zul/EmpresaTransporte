package com.tienda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorDB {

    private static final String URL = "jdbc:sqlite:tienda.db";

    private static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarUsuarios() {
        try (Connection conn = conectar();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT UNIQUE, clave TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void inicializarProductos() {
        try (Connection conn = conectar();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS productos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, precio REAL)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void inicializarCompras() {
        try (Connection conn = conectar();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS compras (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT, producto TEXT, cantidad INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registrarUsuario(String usuario, String clave) {
        String sql = "INSERT INTO usuarios(usuario, clave) VALUES(?, ?)";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, clave);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // MÉTODOS DE CONSULTA Y OPERACIÓN SIN ALTERAR
    // LA ESTRUCTURA DE LA BASE DE DATOS
    // ==========================================

    /**
     * Valida si las credenciales de usuario coinciden con la tabla usuarios.
     */
    public static boolean validarLogin(String usuario, String clave) {
        String sql = "SELECT id FROM usuarios WHERE usuario = ? AND clave = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la lista completa de productos registrados.
     */
    public static List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY id ASC";
        try (Connection conn = conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Agrega un nuevo producto a la tabla productos.
     */
    public static boolean agregarProducto(String nombre, double precio) {
        String sql = "INSERT INTO productos(nombre, precio) VALUES(?, ?)";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un producto por su ID.
     */
    public static boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra una compra en la tabla compras.
     */
    public static boolean registrarCompra(String usuario, String producto, int cantidad) {
        String sql = "INSERT INTO compras(usuario, producto, cantidad) VALUES(?, ?, ?)";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, producto);
            ps.setInt(3, cantidad);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene las compras realizadas por un usuario específico.
     */
    public static List<Compra> obtenerComprasPorUsuario(String usuario) {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id, usuario, producto, cantidad FROM compras WHERE usuario = ? ORDER BY id DESC";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Compra(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            rs.getString("producto"),
                            rs.getInt("cantidad")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene el historial global de compras (para módulo de despacho/administrador).
     */
    public static List<Compra> obtenerTodasCompras() {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id, usuario, producto, cantidad FROM compras ORDER BY id DESC";
        try (Connection conn = conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Compra(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        rs.getString("producto"),
                        rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Carga datos iniciales amigables de ejemplo si las tablas están vacías,
     * sin alterar el esquema de la base de datos.
     */
    public static void inicializarDatosEjemplo() {
        try (Connection conn = conectar()) {
            // Verificar si hay usuarios
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM usuarios")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    registrarUsuario("admin", "admin123");
                    registrarUsuario("cliente1", "1234");
                    registrarUsuario("taquilla1", "1234");
                }
            }

            // Verificar si hay productos (rutas/pasajes de Expreso Bolivariano)
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM productos")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    agregarProducto("Pasaje Bogotá - Medellín (Servicio 2G)", 85000.0);
                    agregarProducto("Pasaje Bogotá - Cali (Servicio DuoBus)", 95000.0);
                    agregarProducto("Pasaje Bogotá - Bucaramanga (Servicio Royal)", 78000.0);
                    agregarProducto("Pasaje Bogotá - Pereira (Servicio 2G Gold)", 70000.0);
                    agregarProducto("Pasaje Bogotá - Barranquilla (Servicio VIP)", 135000.0);
                    agregarProducto("Pasaje Bogotá - Ibagué (Servicio Plus)", 45000.0);
                    agregarProducto("Pasaje Medellín - Cartagena (Servicio Costa)", 140000.0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
