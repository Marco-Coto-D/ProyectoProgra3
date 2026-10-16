package reservas.presentation.login;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class CambioClaveLoginView implements PropertyChangeListener {

    @FXML private Label lblUsuario;
    @FXML private PasswordField pfClaveActual;
    @FXML private PasswordField pfClaveNueva;
    @FXML private PasswordField pfConfirmar;
    @FXML private Button btnCambiar;
    @FXML private Button btnPdf;
    @FXML private Button btnCerrar;
    @FXML private Label lblError;
    @FXML private Label lblExito;

    private final Stage stage = new Stage();
    private CambioClaveLoginModel model;

    public CambioClaveLoginView(String id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CambioClaveLoginView.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Cambiar clave");
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar CambioClaveLoginView.fxml", e);
        }

        lblUsuario.setText("Usuario: " + id);
        btnCambiar.setGraphic(icono("clave.png"));
        btnPdf.setGraphic(icono("pdf.png"));
        btnCerrar.setGraphic(icono("cancel.png"));
    }

    private ImageView icono(String archivo) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/" + archivo)));
        iv.setFitWidth(16);
        iv.setFitHeight(16);
        return iv;
    }

    public void setController(CambioClaveLoginController controller) {
        btnCambiar.setOnAction(e -> controller.cambiar( pfClaveActual.getText(), pfClaveNueva.getText(), pfConfirmar.getText()));
        btnPdf.setOnAction(e -> controller.print());
        btnCerrar.setOnAction(e -> cerrar());
    }

    public void setModel(CambioClaveLoginModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void mostrar() {
        stage.showAndWait();
    }

    public void cerrar() {
        stage.close();
    }

    public void mostrarExito() {
        pfClaveActual.clear();
        pfClaveNueva.clear();
        pfConfirmar.clear();
        lblError.setText("");
        lblExito.setText("Clave cambiada correctamente");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CambioClaveLoginModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
            lblExito.setText("");
        }
    }
}
