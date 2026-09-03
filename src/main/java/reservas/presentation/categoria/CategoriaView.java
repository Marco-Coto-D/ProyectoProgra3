package reservas.presentation.categoria;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import reservas.logic.CategoriaRecurso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class CategoriaView implements PropertyChangeListener {

    @FXML private Label lblId;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtBusqueda;
    @FXML private Button btnGuardar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private TableView<CategoriaRecurso> tablaCategorias;
    @FXML private TableColumn<CategoriaRecurso, String> colId;
    @FXML private TableColumn<CategoriaRecurso, String> colDescripcion;

    private final Parent root;
    private CategoriaModel model;
    private CategoriaRecurso seleccionada;

    public CategoriaView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CategoriaView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar CategoriaView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(CategoriaController controller) {
        btnGuardar.setOnAction(e -> controller.guardar(seleccionada, txtDescripcion.getText()));
        btnBorrar.setOnAction(e -> controller.borrar(tablaCategorias.getSelectionModel().getSelectedItem()));
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        btnBuscar.setOnAction(e -> controller.buscarPorDescripcion(txtBusqueda.getText()));
        btnPdf.setOnAction(e -> controller.print());

        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((obs, anterior, item) -> {
            if (item != null) {
                seleccionada = item;
                lblId.setText(item.getId());
                txtDescripcion.setText(item.getDescripcion());
            }
        });
    }

    public void setModel(CategoriaModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

        tablaCategorias.setItems(model.getCategorias());
    }

    public void limpiarFormulario() {
        seleccionada = null;
        lblId.setText("");
        txtDescripcion.clear();
        txtBusqueda.clear();
        tablaCategorias.getSelectionModel().clearSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CategoriaModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}