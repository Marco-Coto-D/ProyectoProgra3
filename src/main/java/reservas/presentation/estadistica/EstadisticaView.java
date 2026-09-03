package reservas.presentation.estadistica;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;

public class EstadisticaView implements PropertyChangeListener {

    @FXML private DatePicker dpDesdeRecursos;
    @FXML private DatePicker dpHastaRecursos;
    @FXML private Button btnCargarRecursos;
    @FXML private Button btnPdfRecursos;
    @FXML private Label lblErrorRecursos;
    @FXML private TableView<Map.Entry<String, Long>> tablaRecursos;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colCategoria;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colCantidadRecurso;
    @FXML private BarChart<String, Number> chartRecursos;

    @FXML private DatePicker dpDesdeActividades;
    @FXML private DatePicker dpHastaActividades;
    @FXML private Button btnCargarActividades;
    @FXML private Button btnPdfActividades;
    @FXML private Label lblErrorActividades;
    @FXML private TableView<Map.Entry<String, Long>> tablaActividades;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colSemana;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colCantidadActividad;
    @FXML private BarChart<String, Number> chartActividades;

    private final Parent root;
    private EstadisticaModel model;

    public EstadisticaView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EstadisticaView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar EstadisticaView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(EstadisticaController controller) {
        btnCargarRecursos.setOnAction(e ->
                controller.cargarRecursos(dpDesdeRecursos.getValue(), dpHastaRecursos.getValue()));
        btnPdfRecursos.setOnAction(e -> controller.printRecursos());
        btnCargarActividades.setOnAction(e ->
                controller.cargarActividades(dpDesdeActividades.getValue(), dpHastaActividades.getValue()));
        btnPdfActividades.setOnAction(e -> controller.printActividades());
    }

    public void setModel(EstadisticaModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        colCantidadRecurso.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getValue())));

        colSemana.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        colCantidadActividad.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getValue())));
    }

    public void mostrarRecursos(Map<String, Long> conteo) {
        tablaRecursos.setItems(FXCollections.observableArrayList(conteo.entrySet()));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartRecursos.getData().setAll(serie);
    }

    public void mostrarActividades(Map<String, Long> conteo) {
        tablaActividades.setItems(FXCollections.observableArrayList(conteo.entrySet()));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartActividades.getData().setAll(serie);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EstadisticaModel.ERROR_RECURSOS.equals(evt.getPropertyName())) {
            lblErrorRecursos.setText(model.getErrorRecursos());
        } else if (EstadisticaModel.ERROR_ACTIVIDADES.equals(evt.getPropertyName())) {
            lblErrorActividades.setText(model.getErrorActividades());
        }
    }
}