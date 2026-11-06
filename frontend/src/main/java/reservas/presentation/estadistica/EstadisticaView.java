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
import reservas.util.FechaConverter;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

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

        FechaConverter.aplicar(dpDesdeRecursos, dpHastaRecursos, dpDesdeActividades, dpHastaActividades);
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(EstadisticaController controller) {
        btnCargarRecursos.setOnAction(e -> {
            if (FechaConverter.esInvalida(dpDesdeRecursos) || FechaConverter.esInvalida(dpHastaRecursos)) {
                controller.fechaInvalidaRecursos();
                return;
            }
            controller.cargarRecursos(dpDesdeRecursos.getValue(), dpHastaRecursos.getValue());
        });
        btnPdfRecursos.setOnAction(e -> controller.printRecursos());
        btnCargarActividades.setOnAction(e -> {
            if (FechaConverter.esInvalida(dpDesdeActividades) || FechaConverter.esInvalida(dpHastaActividades)) {
                controller.fechaInvalidaActividades();
                return;
            }
            controller.cargarActividades(dpDesdeActividades.getValue(), dpHastaActividades.getValue());
        });
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

    /** el gráfico tal como se ve en pantalla, en PNG, para ponerlo en el PDF */
    public byte[] graficoRecursos() throws IOException {
        return png(chartRecursos);
    }

    public byte[] graficoActividades() throws IOException {
        return png(chartActividades);
    }

    private byte[] png(Node nodo) throws IOException {
        // al doble de tamaño para que no se vea borroso en el PDF
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setTransform(Transform.scale(2, 2));
        WritableImage imagen = nodo.snapshot(parametros, null);
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        int[] pixeles = new int[ancho * alto];
        imagen.getPixelReader().getPixels(0, 0, ancho, alto, PixelFormat.getIntArgbInstance(), pixeles, 0, ancho);
        BufferedImage copia = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        copia.setRGB(0, 0, ancho, alto, pixeles, 0, ancho);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(copia, "png", salida);
        return salida.toByteArray();
    }

    private void mostrarRecursos(Map<String, Long> conteo) {
        if (conteo == null) {
            tablaRecursos.getItems().clear();
            chartRecursos.getData().clear();
            return;
        }
        tablaRecursos.setItems(FXCollections.observableArrayList(conteo.entrySet()));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartRecursos.getData().setAll(serie);
    }

    private void mostrarActividades(Map<String, Long> conteo) {
        if (conteo == null) {
            tablaActividades.getItems().clear();
            chartActividades.getData().clear();
            return;
        }
        tablaActividades.setItems(FXCollections.observableArrayList(conteo.entrySet()));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chartActividades.getData().setAll(serie);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EstadisticaModel.RECURSOS.equals(evt.getPropertyName())) {
            mostrarRecursos(model.getConteoRecursos());
        } else if (EstadisticaModel.ACTIVIDADES.equals(evt.getPropertyName())) {
            mostrarActividades(model.getConteoActividades());
        } else if (EstadisticaModel.ERROR_RECURSOS.equals(evt.getPropertyName())) {
            lblErrorRecursos.setText(model.getErrorRecursos());
        } else if (EstadisticaModel.ERROR_ACTIVIDADES.equals(evt.getPropertyName())) {
            lblErrorActividades.setText(model.getErrorActividades());
        }
    }
}