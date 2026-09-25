package com.tienda.backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tienda.Compra;
import com.tienda.GestorDB;
import com.tienda.Producto;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP REST embebido para la solución SOA de Expreso Bolivariano.
 * Expone la API v1 descrita en el documento arquitectónico Word.
 */
public class RestBackendServer {

    private static HttpServer server;
    private static final int PORT = 8080;
    private static boolean running = false;

    // Almacenamiento en memoria para bloqueos temporales de sillas con TTL (simulando Redis)
    // Clave: viajeId + "_" + numSilla -> Objeto Bloqueo
    private static final Map<String, BloqueoAsiento> bloqueosTemporales = new ConcurrentHashMap<>();

    public static class BloqueoAsiento {
        public String viajeId;
        public int numSilla;
        public String usuario;
        public long timestampExpiracion;
        public String tokenReserva;

        public BloqueoAsiento(String viajeId, int numSilla, String usuario, int ttlSegundos) {
            this.viajeId = viajeId;
            this.numSilla = numSilla;
            this.usuario = usuario;
            this.timestampExpiracion = System.currentTimeMillis() + (ttlSegundos * 1000L);
            this.tokenReserva = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        public boolean estaExpirado() {
            return System.currentTimeMillis() > timestampExpiracion;
        }

        public long getSegundosRestantes() {
            long diff = (timestampExpiracion - System.currentTimeMillis()) / 1000;
            return Math.max(0, diff);
        }
    }

    public static synchronized void startServer() {
        if (running) return;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Registrar Endpoints de la API v1 (Según documento Word)
            server.createContext("/api/v1/auth/login", new LoginHandler());
            server.createContext("/api/v1/auth/register", new RegisterHandler());
            server.createContext("/api/v1/catalogo/rutas", new RutasHandler());
            server.createContext("/api/v1/asientos/disponibilidad", new DisponibilidadAsientosHandler());
            server.createContext("/api/v1/asientos/bloqueo-temporal", new BloqueoAsientoHandler());
            server.createContext("/api/v1/ventas/crear-orden", new CrearOrdenHandler());
            server.createContext("/api/v1/ventas/historial", new HistorialVentasHandler());
            server.createContext("/api/v1/despachos/planilla", new PlanillaDespachoHandler());
            server.createContext("/api/v1/status", new StatusHandler());

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            running = true;
            System.out.println("[REST Backend] Servidor SOA escuchando en http://localhost:" + PORT + "/api/v1/");
        } catch (Exception e) {
            System.err.println("[REST Backend] No se pudo iniciar el servidor en el puerto " + PORT + ": " + e.getMessage());
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static Map<String, BloqueoAsiento> getBloqueosTemporales() {
        // Limpiar expirados
        bloqueosTemporales.entrySet().removeIf(entry -> entry.getValue().estaExpirado());
        return bloqueosTemporales;
    }

    // =========================================================================
    // HANDLERS HTTP PARA CADA ENDPOINT DE LA ARQUITECTURA SOA
    // =========================================================================

    private static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String json = "{\"status\":\"ONLINE\",\"version\":\"v1.0\",\"server\":\"Expreso Bolivariano SOA REST\"}";
            enviarRespuestaJSON(exchange, 200, json);
        }
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);
                String usuario = params.get("usuario");
                String clave = params.get("clave");

                boolean ok = GestorDB.validarLogin(usuario, clave);
                if (ok) {
                    String token = "JWT-BOLIVARIANO-" + UUID.randomUUID().toString().substring(0, 12);
                    String resp = "{\"exito\":true,\"mensaje\":\"Autenticación exitosa\",\"token\":\"" + token + "\",\"usuario\":\"" + usuario + "\"}";
                    enviarRespuestaJSON(exchange, 200, resp);
                } else {
                    enviarRespuestaJSON(exchange, 401, "{\"exito\":false,\"mensaje\":\"Credenciales incorrectas\"}");
                }
            } else {
                enviarRespuestaJSON(exchange, 405, "{\"exito\":false,\"mensaje\":\"Método no permitido\"}");
            }
        }
    }

    private static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);
                String usuario = params.get("usuario");
                String clave = params.get("clave");

                boolean ok = GestorDB.registrarUsuario(usuario, clave);
                if (ok) {
                    enviarRespuestaJSON(exchange, 200, "{\"exito\":true,\"mensaje\":\"Usuario registrado en el backend\"}");
                } else {
                    enviarRespuestaJSON(exchange, 400, "{\"exito\":false,\"mensaje\":\"El usuario ya existe o datos inválidos\"}");
                }
            } else {
                enviarRespuestaJSON(exchange, 405, "{\"exito\":false,\"mensaje\":\"Método no permitido\"}");
            }
        }
    }

    private static class RutasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Producto> productos = GestorDB.obtenerProductos();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < productos.size(); i++) {
                    Producto p = productos.get(i);
                    json.append("{\"id\":").append(p.getId())
                            .append(",\"nombre\":\"").append(escapeJSON(p.getNombre()))
                            .append("\",\"precio\":").append(p.getPrecio()).append("}");
                    if (i < productos.size() - 1) json.append(",");
                }
                json.append("]");
                enviarRespuestaJSON(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);
                String nombre = params.get("nombre");
                double precio = Double.parseDouble(params.getOrDefault("precio", "0"));

                boolean ok = GestorDB.agregarProducto(nombre, precio);
                if (ok) {
                    enviarRespuestaJSON(exchange, 201, "{\"exito\":true,\"mensaje\":\"Ruta registrada\"}");
                } else {
                    enviarRespuestaJSON(exchange, 400, "{\"exito\":false,\"mensaje\":\"Error al registrar ruta\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);
                int id = Integer.parseInt(params.getOrDefault("id", "0"));
                boolean ok = GestorDB.eliminarProducto(id);
                if (ok) {
                    enviarRespuestaJSON(exchange, 200, "{\"exito\":true,\"mensaje\":\"Ruta eliminada\"}");
                } else {
                    enviarRespuestaJSON(exchange, 400, "{\"exito\":false,\"mensaje\":\"Error al eliminar\"}");
                }
            }
        }
    }

    private static class DisponibilidadAsientosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String query = exchange.getRequestURI().getQuery();
            String viajeId = "VIAJE-001";
            if (query != null && query.contains("viajeId=")) {
                viajeId = query.split("viajeId=")[1].split("&")[0];
            }

            // Obtener bloqueos activos
            Map<String, BloqueoAsiento> bloqueos = getBloqueosTemporales();

            // Construir respuesta de 40 sillas
            StringBuilder json = new StringBuilder("{\"viajeId\":\"" + viajeId + "\",\"asientos\":[");
            for (int i = 1; i <= 40; i++) {
                String key = viajeId + "_" + i;
                String estado = "DISPONIBLE";
                long ttl = 0;
                if (bloqueos.containsKey(key)) {
                    BloqueoAsiento b = bloqueos.get(key);
                    estado = "BLOQUEADO";
                    ttl = b.getSegundosRestantes();
                }

                json.append("{\"numero\":").append(i)
                        .append(",\"estado\":\"").append(estado)
                        .append("\",\"ttlSegundos\":").append(ttl).append("}");
                if (i < 40) json.append(",");
            }
            json.append("]}");
            enviarRespuestaJSON(exchange, 200, json.toString());
        }
    }

    private static class BloqueoAsientoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);
                String viajeId = params.getOrDefault("viajeId", "VIAJE-001");
                int numSilla = Integer.parseInt(params.getOrDefault("numSilla", "1"));
                String usuario = params.getOrDefault("usuario", "Invitado");

                String key = viajeId + "_" + numSilla;
                Map<String, BloqueoAsiento> bloqueos = getBloqueosTemporales();

                if (bloqueos.containsKey(key) && !bloqueos.get(key).estaExpirado()) {
                    // Conflicto: la silla ya está tomada
                    enviarRespuestaJSON(exchange, 409, "{\"exito\":false,\"mensaje\":\"La silla #" + numSilla + " ya está bloqueada por otro cliente.\"}");
                } else {
                    // Bloqueo exitoso en memoria con TTL de 10 minutos (600s)
                    BloqueoAsiento bloqueo = new BloqueoAsiento(viajeId, numSilla, usuario, 600);
                    bloqueos.put(key, bloqueo);

                    String json = "{\"exito\":true,\"mensaje\":\"Silla #" + numSilla + " bloqueada temporalmente\",\"tokenReserva\":\""
                            + bloqueo.tokenReserva + "\",\"ttlSegundos\":600,\"numSilla\":" + numSilla + "}";
                    enviarRespuestaJSON(exchange, 200, json);
                }
            } else {
                enviarRespuestaJSON(exchange, 405, "{\"exito\":false,\"mensaje\":\"Método no permitido\"}");
            }
        }
    }

    private static class CrearOrdenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = leerBody(exchange);
                Map<String, String> params = parseJSONSimple(body);

                String usuario = params.getOrDefault("usuario", "invitado");
                String producto = params.getOrDefault("producto", "Pasaje Bogotá - Medellín");
                int cantidad = Integer.parseInt(params.getOrDefault("cantidad", "1"));
                String medioPago = params.getOrDefault("medioPago", "Tarjeta / PSE");

                boolean ok = GestorDB.registrarCompra(usuario, producto, cantidad);
                if (ok) {
                    String tiqueteId = "TQK-" + System.currentTimeMillis() % 1000000;
                    String json = "{\"exito\":true,\"mensaje\":\"Compra registrada en el backend SOA\",\"tiqueteId\":\""
                            + tiqueteId + "\",\"usuario\":\"" + usuario + "\",\"producto\":\"" + escapeJSON(producto)
                            + "\",\"cantidad\":" + cantidad + ",\"medioPago\":\"" + medioPago + "\"}";
                    enviarRespuestaJSON(exchange, 200, json);
                } else {
                    enviarRespuestaJSON(exchange, 500, "{\"exito\":false,\"mensaje\":\"Error al registrar la orden en la base de datos.\"}");
                }
            } else {
                enviarRespuestaJSON(exchange, 405, "{\"exito\":false,\"mensaje\":\"Método no permitido\"}");
            }
        }
    }

    private static class HistorialVentasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String query = exchange.getRequestURI().getQuery();
            String usuario = null;
            if (query != null && query.contains("usuario=")) {
                usuario = query.split("usuario=")[1].split("&")[0];
            }

            List<Compra> compras;
            if (usuario != null && !usuario.trim().isEmpty()) {
                compras = GestorDB.obtenerComprasPorUsuario(usuario);
            } else {
                compras = GestorDB.obtenerTodasCompras();
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < compras.size(); i++) {
                Compra c = compras.get(i);
                json.append("{\"id\":").append(c.getId())
                        .append(",\"usuario\":\"").append(escapeJSON(c.getUsuario()))
                        .append("\",\"producto\":\"").append(escapeJSON(c.getProducto()))
                        .append("\",\"cantidad\":").append(c.getCantidad()).append("}");
                if (i < compras.size() - 1) json.append(",");
            }
            json.append("]");
            enviarRespuestaJSON(exchange, 200, json.toString());
        }
    }

    private static class PlanillaDespachoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            List<Compra> compras = GestorDB.obtenerTodasCompras();
            int totalPasajes = 0;
            for (Compra c : compras) totalPasajes += c.getCantidad();

            String json = "{\"empresa\":\"Expreso Bolivariano S.A.\",\"planillaId\":\"PLN-2026-0928\",\"busNumero\":\"MÓVIL-4052\",\"conductor\":\"Carlos Alberto Mendoza\",\"ruta\":\"Bogotá Terminal Salitre -> Medellín Terminal Norte\",\"totalPasajeros\":"
                    + totalPasajes + ",\"estado\":\"PLANILLA_ACTIVA_CONFIRMADA\"}";
            enviarRespuestaJSON(exchange, 200, json);
        }
    }

    // =========================================================================
    // MÉTODOS DE UTILIDAD HTTP & JSON PARSER LIGERO
    // =========================================================================

    private static void enviarRespuestaJSON(HttpExchange exchange, int statusCode, String json) {
        try {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String leerBody(HttpExchange exchange) {
        try (InputStream is = exchange.getRequestBody();
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String escapeJSON(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static Map<String, String> parseJSONSimple(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String val = kv[1].trim().replace("\"", "");
                map.put(key, val);
            }
        }
        return map;
    }
}
