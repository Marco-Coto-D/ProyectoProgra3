package reservas.presentation.actividad;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import reservas.logic.Reserva;
import javafx.scene.control.Button;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class ActividadView implements PropertyChangeListener {

    @FXML private DatePicker dpFecha;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private GridPane gridActividad;

    private final Parent root;
    private ActividadModel model;

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    public ActividadView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ActividadView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar ActividadView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(ActividadController controller) {
        dpFecha.valueProperty().addListener((obs, anterior, nuevo) ->
                controller.cargarSemana(nuevo));
        btnPdf.setOnAction(e -> controller.print());
    }

    public void setModel(ActividadModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void limpiarGrid() {
        gridActividad.getChildren().clear();
        gridActividad.getColumnConstraints().clear();
        gridActividad.getRowConstraints().clear();
    }

    public void dibujarGrid(LocalDate lunes, List<Reserva> reservas) {
        limpiarGrid();

        Label lblEncabezadoHora = new Label("Hora");
        lblEncabezadoHora.setStyle("-fx-font-weight: bold; -fx-background-color: #d5d8dc; -fx-padding: 5;");
        gridActividad.add(lblEncabezadoHora, 0, 0);

        for (int dia = 0; dia < 7; dia++) {
            LocalDate fecha = lunes.plusDays(dia);
            Label lblDia = new Label(DIAS[dia] + " " + fecha);
            lblDia.setStyle("-fx-font-weight: bold; -fx-background-color: #d5d8dc; -fx-padding: 5; -fx-min-width: 140;");
            gridActividad.add(lblDia, dia + 1, 0);
        }

        for (int h = 6; h <= 22; h++) {
            int fila = h - 5;
            LocalTime hora = LocalTime.of(h, 0);

            Label lblHoraFila = new Label(hora.toString());
            lblHoraFila.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-min-width: 60;");
            gridActividad.add(lblHoraFila, 0, fila);

            for (int dia = 0; dia < 7; dia++) {
                LocalDate fecha = lunes.plusDays(dia);
                List<Reserva> reservasEnCelda = encontrarReservas(reservas, fecha, hora);

                Label celda;
                if (!reservasEnCelda.isEmpty()) {
                    String texto = reservasEnCelda.stream()
                            .map(r -> r.getActividad() + " (" + r.getFuncionario().getNombre() + ")")
                            .collect(Collectors.joining("\n"));
                    celda = new Label(texto);
                    celda.setStyle("-fx-background-color: #a9dfbf; -fx-padding: 5; -fx-min-width: 140; -fx-min-height: 40; -fx-wrap-text: true;");
                } else {
                    celda = new Label("");
                    celda.setStyle("-fx-padding: 5; -fx-min-width: 140; -fx-min-height: 40; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                }
                gridActividad.add(celda, dia + 1, fila);
            }
        }
    }

    private List<Reserva> encontrarReservas(List<Reserva> reservas, LocalDate fecha, LocalTime hora) {
        return reservas.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .filter(r -> !hora.isBefore(r.getHoraInicio()) && hora.isBefore(r.getHoraFin()))
                .collect(Collectors.toList());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ActividadModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}