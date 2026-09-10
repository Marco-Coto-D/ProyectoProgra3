package reservas.presentation.calendarizacion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;

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

    private static final double ALTURA_FILA = 45;
    private static final int MAX_LARGO_TEXTO = 60;
    private static final double ANCHO_COLUMNA_HORA = 60;
    private static final double ANCHO_COLUMNA_RECURSO = 160;

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
        gridCalendario.getRowConstraints().add(crearFilaFija());
        gridCalendario.getColumnConstraints().add(crearColumnaFija(ANCHO_COLUMNA_HORA));

        for (int col = 0; col < recursos.size(); col++) {
            Label lblRecurso = new Label(recursos.get(col).getDescripcion());
            lblRecurso.setStyle("-fx-font-weight: bold; -fx-background-color: #d5d8dc; -fx-padding: 5; -fx-min-width: 160;");
            gridCalendario.add(lblRecurso, col + 1, 0);
            gridCalendario.getColumnConstraints().add(crearColumnaFija(ANCHO_COLUMNA_RECURSO));
        }

        for (int h = 6; h <= 22; h++) {
            int fila = h - 5;
            LocalTime hora = LocalTime.of(h, 0);
            gridCalendario.getRowConstraints().add(crearFilaFija());

            Label lblHoraFila = new Label(hora.toString());
            lblHoraFila.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-min-width: 60;");
            gridCalendario.add(lblHoraFila, 0, fila);

            for (int col = 0; col < recursos.size(); col++) {
                Reserva reservaEnCelda = encontrarReserva(reservas, recursos.get(col), hora);

                Label celda;
                if (reservaEnCelda != null) {
                    String texto = reservaEnCelda.getActividad() + "\n" + reservaEnCelda.getFuncionario().getNombre();
                    celda = new Label(truncar(texto, MAX_LARGO_TEXTO));
                    celda.setStyle("-fx-background-color: #aed6f1; -fx-padding: 5; -fx-min-width: 160; -fx-min-height: 40; -fx-wrap-text: true;");
                    limitarAltura(celda);
                    Tooltip.install(celda, new Tooltip(texto));
                } else {
                    celda = new Label("");
                    celda.setStyle("-fx-padding: 5; -fx-min-width: 160; -fx-min-height: 40; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                }
                gridCalendario.add(celda, col + 1, fila);
            }
        }
    }

    private RowConstraints crearFilaFija() {
        RowConstraints fila = new RowConstraints();
        fila.setMinHeight(ALTURA_FILA);
        fila.setPrefHeight(ALTURA_FILA);
        fila.setMaxHeight(ALTURA_FILA);
        return fila;
    }

    private ColumnConstraints crearColumnaFija(double ancho) {
        ColumnConstraints columna = new ColumnConstraints();
        columna.setMinWidth(ancho);
        columna.setPrefWidth(ancho);
        columna.setMaxWidth(ancho);
        return columna;
    }

    private String truncar(String texto, int maxLargo) {
        if (texto.length() <= maxLargo) return texto;
        return texto.substring(0, maxLargo - 3) + "...";
    }

    private void limitarAltura(Label celda) {
        celda.setMinHeight(ALTURA_FILA);
        celda.setMaxHeight(ALTURA_FILA);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(celda.widthProperty());
        clip.heightProperty().bind(celda.heightProperty());
        celda.setClip(clip);
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