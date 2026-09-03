package reservas.presentation.calendarizacion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import javafx.scene.control.Button;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class CalendarizacionView implements PropertyChangeListener {

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<CategoriaRecurso> cmbCategoria;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private GridPane gridCalendario;

    private final Parent root;
    private CalendarizacionModel model;

    public CalendarizacionView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CalendarizacionView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar CalendarizacionView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(CalendarizacionController controller) {
        dpFecha.valueProperty().addListener((obs, anterior, nuevo) ->
                controller.cargarCalendario(nuevo, cmbCategoria.getValue()));
        cmbCategoria.setOnAction(e ->
                controller.cargarCalendario(dpFecha.getValue(), cmbCategoria.getValue()));
        btnPdf.setOnAction(e -> controller.print());
    }

    public void setModel(CalendarizacionModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        cmbCategoria.setItems(model.getCategorias());
        cmbCategoria.setCellFactory(lv -> celdaCategoria());
        cmbCategoria.setButtonCell(celdaCategoria());
    }

    private ListCell<CategoriaRecurso> celdaCategoria() {
        return new ListCell<>() {
            @Override protected void updateItem(CategoriaRecurso c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(vacio || c == null ? null : c.getDescripcion());
            }
        };
    }

    public void limpiarGrid() {
        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();
        gridCalendario.getRowConstraints().clear();
    }

    public void dibujarGrid(List<Recurso> recursos, List<Reserva> reservas) {
        limpiarGrid();

        if (recursos.isEmpty()) {
            gridCalendario.add(new Label("No hay recursos en esta categoría"), 0, 0);
            return;
        }

        Label lblEncabezadoHora = new Label("Hora");
        lblEncabezadoHora.setStyle("-fx-font-weight: bold; -fx-background-color: #d5d8dc; -fx-padding: 5;");
        gridCalendario.add(lblEncabezadoHora, 0, 0);

        for (int col = 0; col < recursos.size(); col++) {
            Label lblRecurso = new Label(recursos.get(col).getDescripcion());
            lblRecurso.setStyle("-fx-font-weight: bold; -fx-background-color: #d5d8dc; -fx-padding: 5; -fx-min-width: 160;");
            gridCalendario.add(lblRecurso, col + 1, 0);
        }

        for (int h = 6; h <= 22; h++) {
            int fila = h - 5;
            LocalTime hora = LocalTime.of(h, 0);

            Label lblHoraFila = new Label(hora.toString());
            lblHoraFila.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-min-width: 60;");
            gridCalendario.add(lblHoraFila, 0, fila);

            for (int col = 0; col < recursos.size(); col++) {
                Reserva reservaEnCelda = encontrarReserva(reservas, recursos.get(col), hora);

                Label celda;
                if (reservaEnCelda != null) {
                    String texto = reservaEnCelda.getActividad() + "\n" + reservaEnCelda.getFuncionario().getNombre();
                    celda = new Label(texto);
                    celda.setStyle("-fx-background-color: #aed6f1; -fx-padding: 5; -fx-min-width: 160; -fx-min-height: 40; -fx-wrap-text: true;");
                } else {
                    celda = new Label("");
                    celda.setStyle("-fx-padding: 5; -fx-min-width: 160; -fx-min-height: 40; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                }
                gridCalendario.add(celda, col + 1, fila);
            }
        }
    }

    private Reserva encontrarReserva(List<Reserva> reservas, Recurso recurso, LocalTime hora) {
        for (Reserva r : reservas) {
            if (hora.isBefore(r.getHoraInicio()) || !hora.isBefore(r.getHoraFin())) continue;
            for (Recurso rec : r.getRecursos()) {
                if (rec.getId().equals(recurso.getId())) return r;
            }
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CalendarizacionModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}