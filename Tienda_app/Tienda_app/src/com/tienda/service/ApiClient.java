package com.tienda.service;

import com.tienda.Compra;
import com.tienda.GestorDB;
import com.tienda.Producto;
import com.tienda.backend.RestBackendServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente de servicios RESTful utilizado por la interfaz gráfica (Frontend JavaFX)
 * para comunicarse mediante peticiones HTTP con la API del Backend SOA (RestBackendServer).
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/v1";

    public ApiClient() {
        // Garantizar que el servidor Backend REST esté corriendo
        if (!RestBackendServer.isRunning()) {
            RestBackendServer.startServer();
        }
    }

    public boolean login(String usuario, String clave) {
        try {
            String jsonInput = "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}";
            String respuesta = enviarHTTP("POST", "/auth/login", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            // Fallback directo si falla la red
            return GestorDB.validarLogin(usuario, clave);
        }
    }

    public boolean registrar(String usuario, String clave) {
        try {
            String jsonInput = "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}";
            String respuesta = enviarHTTP("POST", "/auth/register", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            return GestorDB.registrarUsuario(usuario, clave);
        }
    }

    public List<Producto> obtenerProductos() {
        try {
            String json = enviarHTTP("GET", "/catalogo/rutas", null);
            List<Producto> lista = new ArrayList<>();

            // Parsear array de productos
            if (json.startsWith("[") && json.endsWith("]")) {
                String content = json.substring(1, json.length() - 1);
                if (!content.trim().isEmpty()) {
                    String[] items = content.split("(?<=\\}),\\s*(?=\\{)");
                    for (String item : items) {
                        int id = Integer.parseInt(extraerValor(item, "id", "0"));
                        String nombre = extraerValor(item, "nombre", "Ruta");
                        double precio = Double.parseDouble(extraerValor(item, "precio", "0.0"));
                        lista.add(new Producto(id, nombre, precio));
                    }
                }
            }
            if (!lista.isEmpty()) return lista;
        } catch (Exception e) {
            // Fallback a base de datos
        }
        return GestorDB.obtenerProductos();
    }

    public boolean agregarProducto(String nombre, double precio) {
        try {
            String jsonInput = "{\"nombre\":\"" + nombre + "\",\"precio\":\"" + precio + "\"}";
            String respuesta = enviarHTTP("POST", "/catalogo/rutas", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            return GestorDB.agregarProducto(nombre, precio);
        }
    }

    public boolean eliminarProducto(int id) {
        try {
            String jsonInput = "{\"id\":\"" + id + "\"}";
            String respuesta = enviarHTTP("DELETE", "/catalogo/rutas", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            return GestorDB.eliminarProducto(id);
        }
    }

    public boolean bloquearAsientoTemporal(String viajeId, int numSilla, String usuario) {
        try {
            String jsonInput = "{\"viajeId\":\"" + viajeId + "\",\"numSilla\":\"" + numSilla + "\",\"usuario\":\"" + usuario + "\"}";
            String respuesta = enviarHTTP("POST", "/asientos/bloqueo-temporal", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            return true;
        }
    }

    public boolean crearOrdenCompra(String usuario, String producto, int cantidad, String medioPago) {
        try {
            String jsonInput = "{\"usuario\":\"" + usuario + "\",\"producto\":\"" + producto + "\",\"cantidad\":\"" + cantidad + "\",\"medioPago\":\"" + medioPago + "\"}";
            String respuesta = enviarHTTP("POST", "/ventas/crear-orden", jsonInput);
            return respuesta.contains("\"exito\":true");
        } catch (Exception e) {
            return GestorDB.registrarCompra(usuario, producto, cantidad);
        }
    }

    public List<Compra> obtenerCompras(String usuario) {
        try {
            String endpoint = "/ventas/historial" + (usuario != null ? "?usuario=" + usuario : "");
            String json = enviarHTTP("GET", endpoint, null);
            List<Compra> lista = new ArrayList<>();

            if (json.startsWith("[") && json.endsWith("]")) {
                String content = json.substring(1, json.length() - 1);
                if (!content.trim().isEmpty()) {
                    String[] items = content.split("(?<=\\}),\\s*(?=\\{)");
                    for (String item : items) {
                        int id = Integer.parseInt(extraerValor(item, "id", "0"));
                        String usr = extraerValor(item, "usuario", "Anónimo");
                        String prod = extraerValor(item, "producto", "Pasaje");
                        int cant = Integer.parseInt(extraerValor(item, "cantidad", "1"));
                        lista.add(new Compra(id, usr, prod, cant));
                    }
                }
            }
            if (!lista.isEmpty()) return lista;
        } catch (Exception e) {
            // Fallback
        }
        return usuario != null ? GestorDB.obtenerComprasPorUsuario(usuario) : GestorDB.obtenerTodasCompras();
    }

    public String obtenerPlanillaDespacho() {
        try {
            return enviarHTTP("GET", "/despachos/planilla", null);
        } catch (Exception e) {
            return "{\"empresa\":\"Expreso Bolivariano\",\"estado\":\"MODO_LOCAL\"}";
        }
    }

    // =========================================================================
    // AUXILIARES HTTP CONECTIVIDAD REST
    // =========================================================================

    private String enviarHTTP(String metodo, String endpoint, String jsonBody) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(metodo);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        conn.setRequestProperty("Accept", "application/json");

        if (jsonBody != null && !jsonBody.isEmpty()) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) return "{}";

        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "{}";
        }
    }

    private String extraerValor(String jsonItem, String clave, String valorDefecto) {
        String regex = "\"" + clave + "\":";
        if (!jsonItem.contains(regex)) return valorDefecto;

        int idx = jsonItem.indexOf(regex) + regex.length();
        String sub = jsonItem.substring(idx).trim();
        if (sub.startsWith("\"")) {
            sub = sub.substring(1);
            int end = sub.indexOf("\"");
            return end != -1 ? sub.substring(0, end) : valorDefecto;
        } else {
            int end = sub.indexOf(",");
            if (end == -1) end = sub.indexOf("}");
            return end != -1 ? sub.substring(0, end).trim() : sub;
        }
    }
}
