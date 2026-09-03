package reservas.presentation.recurso;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class RecursoView implements PropertyChangeListener {

    @FXML private TextField txtId;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<CategoriaRecurso> cmbCategoria;
    @FXML private ComboBox<CategoriaRecurso> cmbFiltro;
    @FXML private Button btnGuardar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private TableView<Recurso> tablaRecursos;
    @FXML private TableColumn<Recurso, String> colId;
    @FXML private TableColumn<Recurso, String> colDescripcion;
    @FXML private TableColumn<Recurso, String> colCategoria;

    private final Parent root;
    private RecursoModel model;

    public RecursoView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RecursoView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar RecursoView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(RecursoController controller) {
        btnGuardar.setOnAction(e -> controller.guardar(
                txtId.getText(), txtDescripcion.getText(), cmbCategoria.getValue()));
        btnBorrar.setOnAction(e -> controller.borrar(tablaRecursos.getSelectionModel().getSelectedItem()));
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        cmbFiltro.setOnAction(e -> controller.buscarPorCategoria(cmbFiltro.getValue()));
        btnPdf.setOnAction(e -> controller.print());

        tablaRecursos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtId.setText(seleccionado.getId());
                txtId.setEditable(false);
                txtDescripcion.setText(seleccionado.getDescripcion());
                cmbCategoria.setValue(seleccionado.getCategoria());
            }
        });
    }

    public void setModel(RecursoModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        cmbCategoria.setItems(model.getCategorias());
        cmbCategoria.setCellFactory(lv -> celdaCategoria());
        cmbCategoria.setButtonCell(celdaCategoria());

        cmbFiltro.setItems(model.getCategorias());
        cmbFiltro.setCellFactory(lv -> celdaCategoria());
        cmbFiltro.setButtonCell(celdaCategoria());

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria().getDescripcion()));

        tablaRecursos.setItems(model.getRecursos());
    }

    private ListCell<CategoriaRecurso> celdaCategoria() {
        return new ListCell<>() {
            @Override protected void updateItem(CategoriaRecurso c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(vacio || c == null ? null : c.getDescripcion());
            }
        };
    }

    public void limpiarFormulario() {
        txtId.clear();
        txtId.setEditable(true);
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        tablaRecursos.getSelectionModel().clearSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RecursoModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}