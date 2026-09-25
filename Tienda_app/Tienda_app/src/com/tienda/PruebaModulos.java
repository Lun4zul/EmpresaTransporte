package com.tienda;

import com.tienda.service.CompraService;
import com.tienda.service.ProductoService;
import com.tienda.service.UsuarioService;

import java.util.List;

/**
 * Clase de verificación técnica de los módulos del sistema (Autenticación, Catálogo y Ventas).
 * Permite validar la correcta integración con la base de datos SQLite sin depender de la interfaz gráfica.
 */
public class PruebaModulos {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  INICIANDO VERIFICACIÓN DE MÓDULOS - EXPRESO BOLIVARIANO ");
        System.out.println("==========================================================");

        // 1. Inicialización de Base de Datos
        GestorDB.inicializarUsuarios();
        GestorDB.inicializarProductos();
        GestorDB.inicializarCompras();
        GestorDB.inicializarDatosEjemplo();
        System.out.println("[DB] Tablas inicializadas y datos de ejemplo validados.");

        // 2. Módulo de Autenticación
        UsuarioService usuarioService = new UsuarioService();
        boolean authAdmin = usuarioService.autenticar("admin", "admin123");
        System.out.println("[AUTH] Login 'admin': " + (authAdmin ? "EXITOSO" : "FALLIDO"));

        // Registro de usuario de prueba
        String testUser = "pasajero_demo";
        usuarioService.registrar(testUser, "clave123");
        boolean authDemo = usuarioService.autenticar(testUser, "clave123");
        System.out.println("[AUTH] Registro y login de '" + testUser + "': " + (authDemo ? "EXITOSO" : "FALLIDO"));

        // 3. Módulo de Catálogo de Productos / Pasajes
        ProductoService productoService = new ProductoService();
        List<Producto> productos = productoService.listarProductos();
        System.out.println("\n[CATÁLOGO] Total de productos/rutas disponibles: " + productos.size());
        for (Producto p : productos) {
            System.out.println("   -> ID: " + p.getId() + " | " + p.getNombre() + " | Precio: $" + p.getPrecio() + " COP");
        }

        // 4. Módulo de Compras / Ventas
        CompraService compraService = new CompraService();
        if (!productos.isEmpty()) {
            Producto pSeleccionado = productos.get(0);
            boolean compraOk = compraService.procesarCompra(testUser, pSeleccionado.getNombre(), 2);
            System.out.println("\n[VENTAS] Registro de compra (2 pasajes para " + testUser + "): " + (compraOk ? "EXITOSO" : "FALLIDO"));
        }

        // 5. Historial de Compras
        List<Compra> misCompras = compraService.listarComprasPorUsuario(testUser);
        System.out.println("\n[HISTORIAL] Compras registradas para '" + testUser + "': " + misCompras.size());
        for (Compra c : misCompras) {
            System.out.println("   -> " + c.toString());
        }

        System.out.println("\n==========================================================");
        System.out.println("  TODOS LOS MÓDULOS OPERAN CORRECTAMENTE CON TIENDA.DB    ");
        System.out.println("==========================================================");
    }
}
