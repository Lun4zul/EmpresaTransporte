package com.tienda;

import com.tienda.service.CompraService;
import com.tienda.service.ProductoService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class TiendaController implements Initializable {

    // Header
    @FXML private Label lblUsuarioActual;

    // Pestaña 1: Catálogo y Venta
    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colProdId;
    @FXML private TableColumn<Producto, String> colProdNombre;
    @FXML private TableColumn<Producto, String> colProdPrecio;
    @FXML private Label lblProductoSeleccionado;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private Button btnComprar;
    @FXML private Label lblEstadoCompra;

    // Pestaña 2: Compras
    @FXML private RadioButton rbSoloMios;
    @FXML private RadioButton rbTodasCompras;
    @FXML private TableView<Compra> tblCompras;
    @FXML private TableColumn<Compra, Integer> colCompId;
    @FXML private TableColumn<Compra, String> colCompUsuario;
    @FXML private TableColumn<Compra, String> colCompProducto;
    @FXML private TableColumn<Compra, Integer> colCompCantidad;
    @FXML private Label lblTotalTransacciones;
    @FXML private Label lblTotalTiquetes;

    // Pestaña 3: Admin
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoPrecio;
    @FXML private Label lblEstadoAdmin;

    // Servicios de Negocio (Capa SOA)
    private final ProductoService productoService = new ProductoService();
    private final CompraService compraService = new CompraService();

    // Colecciones observables
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Compra> listaCompras = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;

    private Producto productoSeleccionado = null;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar datos de usuario
        String usuario = Sesion.getInstancia().getUsuarioActual();
        lblUsuarioActual.setText("Usuario: " + usuario);

        // Configurar Spinner de cantidad (1 a 20)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        spnCantidad.setValueFactory(valueFactory);

        // Configurar ToggleGroup para filtros de compras
        ToggleGroup grupoFiltro = new ToggleGroup();
        rbSoloMios.setToggleGroup(grupoFiltro);
        rbTodasCompras.setToggleGroup(grupoFiltro);

        // Configurar columnas de Productos
        colProdId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colProdNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colProdPrecio.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatoMoneda.format(cellData.getValue().getPrecio())));

        // Configurar filtro de búsqueda
        productosFiltrados = new FilteredList<>(listaProductos, p -> true);
        txtBuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            productosFiltrados.setPredicate(producto -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return producto.getNombre().toLowerCase().contains(lowerCaseFilter);
            });
        });
        tblProductos.setItems(productosFiltrados);

        // Listener de selección en la tabla de productos
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            productoSeleccionado = newSelection;
            if (newSelection != null) {
                lblProductoSeleccionado.setText(newSelection.getNombre() + " (" + formatoMoneda.format(newSelection.getPrecio()) + ")");
                lblEstadoCompra.setText("");
            } else {
                lblProductoSeleccionado.setText("Ninguno seleccionado (haga clic en la tabla)");
            }
        });

        // Configurar columnas de Compras
        colCompId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colCompUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsuario()));
        colCompProducto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProducto()));
        colCompCantidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        tblCompras.setItems(listaCompras);

        // Cargar datos iniciales
        cargarProductos();
        cargarCompras();
    }

    @FXML
    public void cargarProductos() {
        listaProductos.clear();
        List<Producto> productos = productoService.listarProductos();
        listaProductos.addAll(productos);
    }

    @FXML
    public void realizarCompra() {
        if (productoSeleccionado == null) {
            lblEstadoCompra.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoCompra.setText("Por favor seleccione un pasaje o producto de la lista.");
            return;
        }

        int cantidad = spnCantidad.getValue();
        if (cantidad <= 0) {
            lblEstadoCompra.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoCompra.setText("La cantidad debe ser mayor a cero.");
            return;
        }

        String usuario = Sesion.getInstancia().getUsuarioActual();
        boolean exito = compraService.procesarCompra(usuario, productoSeleccionado.getNombre(), cantidad);

        if (exito) {
            double total = productoSeleccionado.getPrecio() * cantidad;
            lblEstadoCompra.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
            lblEstadoCompra.setText("¡Compra registrada con éxito! (" + cantidad + " tiquetes por " + formatoMoneda.format(total) + ")");
            cargarCompras();
        } else {
            lblEstadoCompra.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoCompra.setText("Error al procesar la compra en la base de datos.");
        }
    }

    @FXML
    public void cambiarFiltroCompras(ActionEvent event) {
        cargarCompras();
    }

    @FXML
    public void cargarCompras() {
        listaCompras.clear();
        List<Compra> compras;
        if (rbSoloMios.isSelected()) {
            String usuario = Sesion.getInstancia().getUsuarioActual();
            compras = compraService.listarComprasPorUsuario(usuario);
        } else {
            compras = compraService.listarTodasLasCompras();
        }
        listaCompras.addAll(compras);

        // Actualizar métricas
        int totalTransacciones = compras.size();
        int totalTiquetes = 0;
        for (Compra c : compras) {
            totalTiquetes += c.getCantidad();
        }
        lblTotalTransacciones.setText("Total Transacciones: " + totalTransacciones);
        lblTotalTiquetes.setText("Total Tiquetes Emitidos: " + totalTiquetes);
    }

    @FXML
    public void agregarNuevoProducto() {
        String nombre = txtNuevoNombre.getText();
        String precioStr = txtNuevoPrecio.getText();

        if (nombre == null || nombre.trim().isEmpty() || precioStr == null || precioStr.trim().isEmpty()) {
            lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoAdmin.setText("Ingrese el nombre y el precio del nuevo producto.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr.trim().replace("$", "").replace(".", "").replace(",", "."));
            if (precio <= 0) {
                lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                lblEstadoAdmin.setText("El precio debe ser un número positivo.");
                return;
            }

            boolean exito = productoService.guardarProducto(nombre, precio);
            if (exito) {
                lblEstadoAdmin.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                lblEstadoAdmin.setText("¡Producto registrado exitosamente en el catálogo!");
                txtNuevoNombre.clear();
                txtNuevoPrecio.clear();
                cargarProductos();
            } else {
                lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                lblEstadoAdmin.setText("Error al guardar el producto en la base de datos.");
            }
        } catch (NumberFormatException e) {
            lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoAdmin.setText("Formato de precio inválido. Ingrese solo números (ej: 75000).");
        }
    }

    @FXML
    public void eliminarProductoSeleccionado() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoAdmin.setText("Seleccione un producto en la tabla del catálogo para eliminar.");
            return;
        }

        boolean exito = productoService.eliminarProducto(seleccionado.getId());
        if (exito) {
            lblEstadoAdmin.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
            lblEstadoAdmin.setText("Producto eliminado del catálogo: " + seleccionado.getNombre());
            cargarProductos();
        } else {
            lblEstadoAdmin.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
            lblEstadoAdmin.setText("Error al eliminar el producto.");
        }
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        Sesion.getInstancia().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 420, 380);
            stage.setTitle("Expreso Bolivariano - Iniciar Sesión");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
