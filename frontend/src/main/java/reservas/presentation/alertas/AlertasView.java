package reservas.presentation.alertas;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import reservas.logic.Reserva;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class AlertasView {
    @FXML private TableView<Reserva> tablaAlertas;
    @FXML private TableColumn<Reserva, String> colActividad;
    @FXML private TableColumn<Reserva, Reserva> colVer;
    @FXML private TableColumn<Reserva, Reserva> colDescartar;

    private final Parent root;

    public AlertasView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AlertasView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar AlertasView.fxml", e);
        }

        colActividad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActividad()));
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(AlertasController controller) {
        colVer.setCellFactory(col -> celdaConIcono("detalles.png", "Ver detalle", controller::ver));
        colDescartar.setCellFactory(col -> celdaConIcono("descartar.png", "Descartar alerta", controller::descartar));
    }

    public void setModel(AlertasModel model) {
        tablaAlertas.setItems(model.getAlertas());
    }

    public void mostrarDetalle(String titulo, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, detalle);
        alerta.setTitle("Detalle de la alerta");
        alerta.setHeaderText(titulo);
        asociarAVentana(alerta);
        alerta.showAndWait();
    }

    public boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje);
        alerta.setTitle("Descartar alerta");
        alerta.setHeaderText(null);
        asociarAVentana(alerta);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private void asociarAVentana(Alert alerta) {
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alerta.initOwner(root.getScene().getWindow());
        }
    }

    private TableCell<Reserva, Reserva> celdaConIcono(String icono, String ayuda, Consumer<Reserva> accion) {
        return new TableCell<>() {
            private final Button boton = new Button();

            {
                ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/" + icono)));
                iv.setFitWidth(16);
                iv.setFitHeight(16);
                boton.setGraphic(iv);
                boton.setTooltip(new Tooltip(ayuda));
                boton.setOnAction(e -> accion.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Reserva item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : boton);
            }
        };
    }
}
