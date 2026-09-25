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

public class Login {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblEstado;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void onLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            lblEstado.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            lblEstado.setText("Por favor complete todos los campos.");
            return;
        }

        boolean exito = usuarioService.autenticar(usuario, password);
        if (exito) {
            Sesion.getInstancia().setUsuarioActual(usuario);
            lblEstado.setStyle("-fx-text-fill: #388E3C; -fx-font-weight: bold;");
            lblEstado.setText("¡Bienvenido " + usuario + "! Cargando módulos...");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tienda.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 850, 600);
                stage.setTitle("Expreso Bolivariano - Módulos de Tienda y Despacho");
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                lblEstado.setStyle("-fx-text-fill: #D32F2F;");
                lblEstado.setText("Error al cargar la vista de la tienda: " + e.getMessage());
            }
        } else {
            lblEstado.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            lblEstado.setText("Credenciales incorrectas o usuario no existe.");
        }
    }

    @FXML
    public void abrirRegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registro.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 450, 380);
            stage.setTitle("Registro de Usuario - Expreso Bolivariano");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblEstado.setStyle("-fx-text-fill: #D32F2F;");
            lblEstado.setText("Error al abrir registro: " + e.getMessage());
        }
    }
}
