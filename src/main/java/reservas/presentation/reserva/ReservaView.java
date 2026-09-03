package reservas.presentation.reserva;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservaView implements PropertyChangeListener {

    @FXML private TextField txtFrase;
    @FXML private Button btnExtraer;
    @FXML private TextField txtActividad;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<LocalTime> cmbHoraInicio;
    @FXML private ComboBox<LocalTime> cmbHoraFin;
    @FXML private ListView<CategoriaRecurso> listCategorias;
    @FXML private Button btnReservar;
    @FXML private Button btnCancelar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colActividad;
    @FXML private TableColumn<Reserva, String> colFecha;
    @FXML private TableColumn<Reserva, String> colHorario;
    @FXML private TableColumn<Reserva, String> colEstado;

    private final Parent root;
    private ReservaModel model;

    public ReservaView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReservaView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar ReservaView.fxml", e);
        }

        listCategorias.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        for (int h = 6; h <= 22; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            cmbHoraInicio.getItems().add(hora);
            cmbHoraFin.getItems().add(hora);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(ReservaController controller) {
        btnExtraer.setOnAction(e -> controller.extraerConIA(txtFrase.getText()));
        btnReservar.setOnAction(e -> controller.crear(
                txtActividad.getText(), dpFecha.getValue(),
                cmbHoraInicio.getValue(), cmbHoraFin.getValue(),
                listCategorias.getSelectionModel().getSelectedItems()));
        btnCancelar.setOnAction(e -> controller.cancelar(tablaReservas.getSelectionModel().getSelectedItem()));
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        btnPdf.setOnAction(e -> controller.print());
    }

    public void setModel(ReservaModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        listCategorias.setItems(model.getCategorias());
        listCategorias.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(CategoriaRecurso c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(vacio || c == null ? null : c.getDescripcion());
            }
        });

        colActividad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActividad()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colHorario.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio() + " - " + d.getValue().getHoraFin()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));

        tablaReservas.setItems(model.getMisReservas());
    }

    public void setBtnExtraerDeshabilitado(boolean valor) {
        btnExtraer.setDisable(valor);
    }

    public void llenarDesdeIA(String actividad, LocalDate fecha,
                              LocalTime horaInicio, LocalTime horaFin,
                              List<CategoriaRecurso> categorias) {
        txtActividad.setText(actividad);
        dpFecha.setValue(fecha);
        cmbHoraInicio.setValue(horaInicio);
        cmbHoraFin.setValue(horaFin);
        listCategorias.getSelectionModel().clearSelection();
        for (CategoriaRecurso cat : categorias) {
            for (int i = 0; i < listCategorias.getItems().size(); i++) {
                if (listCategorias.getItems().get(i).getId().equals(cat.getId())) {
                    listCategorias.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    public void limpiarFormulario() {
        txtActividad.clear();
        dpFecha.setValue(null);
        cmbHoraInicio.setValue(null);
        cmbHoraFin.setValue(null);
        listCategorias.getSelectionModel().clearSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ReservaModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}