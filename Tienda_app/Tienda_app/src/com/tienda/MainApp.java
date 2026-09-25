package com.tienda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inicialización de las 3 tablas de la base de datos (esquema intacto)
        GestorDB.inicializarUsuarios();
        GestorDB.inicializarProductos();
        GestorDB.inicializarCompras();

        // Carga inicial de datos de ejemplo si las tablas están vacías
        GestorDB.inicializarDatosEjemplo();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 420, 380);
        stage.setTitle("Expreso Bolivariano - Plataforma de Tienda y Pasajes");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
