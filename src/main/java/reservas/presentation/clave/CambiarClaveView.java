package reservas.presentation.clave;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class CambiarClaveView implements PropertyChangeListener {

    @FXML private PasswordField pfClaveActual;
    @FXML private PasswordField pfClaveNueva;
    @FXML private PasswordField pfConfirmar;
    @FXML private Button btnCambiar;
    @FXML private Label lblError;
    @FXML private Label lblExito;

    private final Parent root;
    private CambiarClaveModel model;

    public CambiarClaveView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CambiarClaveView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar CambiarClaveView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(CambiarClaveController controller) {
        btnCambiar.setOnAction(e -> controller.cambiarClave(
                pfClaveActual.getText(), pfClaveNueva.getText(), pfConfirmar.getText()));
    }

    public void setModel(CambiarClaveModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
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
        if (CambiarClaveModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
            lblExito.setText("");
        }
    }
}