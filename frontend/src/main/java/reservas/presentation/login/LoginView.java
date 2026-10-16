package reservas.presentation.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class LoginView implements PropertyChangeListener {

    @FXML private TextField txtId;
    @FXML private PasswordField txtClave;
    @FXML private Button btnIngresar;
    @FXML private Button btnCancelar;
    @FXML private Button btnCambiar;
    @FXML private Label lblError;

    private final Stage stage = new Stage();
    private LoginController controller;
    private LoginModel model;

    public LoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ingreso al sistema");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/icon.png")));
            stage.setScene(new Scene(root));
            stage.sizeToScene();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar LoginView.fxml", e);
        }

        btnIngresar.setGraphic(icono("login.png"));
        btnCancelar.setGraphic(icono("cancel.png"));
        btnCambiar.setGraphic(icono("clave.png"));
    }

    private ImageView icono(String archivo) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/" + archivo)));
        iv.setFitWidth(16);
        iv.setFitHeight(16);
        return iv;
    }

    @FXML
    private void onIngresar() {
        controller.intentarLogin(txtId.getText(), txtClave.getText());
    }

    @FXML
    private void onCancelar() {
        controller.cancelar();
    }

    @FXML
    private void onCambiar() {
        controller.abrirCambioClave(txtId.getText());
    }

    public void setController(LoginController controller) {
        this.controller = controller;
    }

    public void setModel(LoginModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void mostrar() {
        stage.showAndWait();
    }

    public void cerrar() {
        stage.close();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (LoginModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}