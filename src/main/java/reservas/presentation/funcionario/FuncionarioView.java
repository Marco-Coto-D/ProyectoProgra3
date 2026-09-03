package reservas.presentation.funcionario;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import reservas.logic.Funcionario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class FuncionarioView implements PropertyChangeListener {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtBusqueda;
    @FXML private Button btnGuardar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private TableView<Funcionario> tablaFuncionarios;
    @FXML private TableColumn<Funcionario, String> colId;
    @FXML private TableColumn<Funcionario, String> colNombre;
    @FXML private TableColumn<Funcionario, String> colTelefono;

    private final Parent root;
    private FuncionarioModel model;
    private Funcionario seleccionado;

    public FuncionarioView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FuncionarioView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar FuncionarioView.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public void setController(FuncionarioController controller) {
        btnGuardar.setOnAction(e -> controller.guardar(
                seleccionado, txtId.getText(), txtNombre.getText(), txtTelefono.getText()));
        btnBorrar.setOnAction(e -> controller.borrar(tablaFuncionarios.getSelectionModel().getSelectedItem()));
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        btnBuscar.setOnAction(e -> controller.buscar(txtBusqueda.getText()));
        btnPdf.setOnAction(e -> controller.print());

        tablaFuncionarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, item) -> {
            if (item != null) {
                seleccionado = item;
                txtId.setText(item.getId());
                txtId.setEditable(false);
                txtNombre.setText(item.getNombre());
                txtTelefono.setText(item.getTelefono());
            }
        });
    }

    public void setModel(FuncionarioModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));

        tablaFuncionarios.setItems(model.getFuncionarios());
    }

    public void limpiarFormulario() {
        seleccionado = null;
        txtId.clear();
        txtId.setEditable(true);
        txtNombre.clear();
        txtTelefono.clear();
        txtBusqueda.clear();
        tablaFuncionarios.getSelectionModel().clearSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (FuncionarioModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}