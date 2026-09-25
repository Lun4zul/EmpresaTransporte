package com.tienda;

import com.tienda.service.UsuarioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class Registro {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnRegistrar;

    @FXML
    private Label lblEstado;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void onRegistrar(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            lblEstado.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            lblEstado.setText("Complete todos los campos requeridos.");
            return;
        }

        boolean exito = usuarioService.registrar(usuario, password);
        if (exito) {
            lblEstado.setStyle("-fx-text-fill: #388E3C; -fx-font-weight: bold;");
            lblEstado.setText("¡Registro exitoso! Ya puede iniciar sesión.");
            txtUsuario.clear();
            txtPassword.clear();
        } else {
            lblEstado.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            lblEstado.setText("Error: El usuario ya existe o hubo un fallo.");
        }
    }

    @FXML
    public void volverLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 400, 300);
            stage.setTitle("Sistema de Tienda - Iniciar Sesión");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblEstado.setText("Error al volver a la pantalla de login.");
        }
    }
}
