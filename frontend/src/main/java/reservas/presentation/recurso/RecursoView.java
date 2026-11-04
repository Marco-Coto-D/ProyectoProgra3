package reservas.presentation.recurso;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import reservas.presentation.Confirmacion;

public class RecursoView implements PropertyChangeListener {

    @FXML private TextField txtId;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<CategoriaRecurso> cmbCategoria;
    @FXML private ComboBox<CategoriaRecurso> cmbFiltro;
    @FXML private TextField txtBusqueda;
    @FXML private Button btnGuardar;
    @FXML private Button btnBorrar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnPdf;
    @FXML private Label lblError;
    @FXML private TableView<Recurso> tablaRecursos;
    @FXML private TableColumn<Recurso, String> colId;
    @FXML private TableColumn<Recurso, String> colDescripcion;
    @FXML private TableColumn<Recurso, String> colCategoria;

    private final Parent root;
    private RecursoModel model;
    private Recurso seleccionado;
    private RecursoController controller;
    // mientras se rearma la lista del filtro, los cambios del combo no son del usuario
    private boolean rearmandoFiltro;
    // el filtro tiene "Todas" (null) como primera opción, para poder volver a ver todos los recursos
    private final ObservableList<CategoriaRecurso> opcionesFiltro = FXCollections.observableArrayList();

    public RecursoView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RecursoView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar RecursoView.fxml", e);
        }

        btnGuardar.setGraphic(icono("save.png"));
        btnBorrar.setGraphic(icono("delete.png"));
        btnLimpiar.setGraphic(icono("clear.png"));
        btnBuscar.setGraphic(icono("search.png"));
        btnPdf.setGraphic(icono("pdf.png"));
    }

    private ImageView icono(String archivo) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/" + archivo)));
        iv.setFitWidth(16);
        iv.setFitHeight(16);
        return iv;
    }

    public Parent getRoot() {
        return root;
    }

    public boolean confirmar(String mensaje) {
        return Confirmacion.confirmar(root, "Borrar recurso", mensaje);
    }

    public void setController(RecursoController controller) {
        this.controller = controller;
        btnGuardar.setOnAction(e -> controller.guardar(
                seleccionado, txtId.getText(), txtDescripcion.getText(), cmbCategoria.getValue()));
        btnBorrar.setOnAction(e -> controller.borrar(tablaRecursos.getSelectionModel().getSelectedItem()));
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        cmbFiltro.setOnAction(e -> {
            if (!rearmandoFiltro) {
                controller.buscarPorCategoria(cmbFiltro.getValue());
            }
        });
        btnBuscar.setOnAction(e -> controller.buscar(txtBusqueda.getText()));
        btnPdf.setOnAction(e -> controller.print());

        tablaRecursos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, item) -> {
            if (item != null) {
                seleccionado = item;
                txtId.setText(item.getId());
                txtId.setEditable(false);
                txtDescripcion.setText(item.getDescripcion());
                cmbCategoria.setValue(item.getCategoria());
                cmbCategoria.setButtonCell(celdaCategoria());
            }
        });
    }

    public void setModel(RecursoModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);

        cmbCategoria.setItems(model.getCategorias());
        cmbCategoria.setCellFactory(lv -> celdaCategoria());
        cmbCategoria.setButtonCell(celdaCategoria());
        cmbCategoria.setConverter(convertidorCategoria());

        cmbFiltro.setItems(opcionesFiltro);
        model.getCategorias().addListener((ListChangeListener<CategoriaRecurso>) cambio -> rearmarFiltro());
        rearmarFiltro();
        cmbFiltro.setCellFactory(lv -> celdaFiltro());
        cmbFiltro.setButtonCell(celdaFiltro());
        cmbFiltro.setConverter(convertidorCategoria());

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colDescripcion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria().getDescripcion()));

        tablaRecursos.setItems(model.getRecursos());
    }

    // al volver a la pestaña se recargan las categorías: el filtro tiene que quedar en la misma
    // categoría que había elegido el usuario (o en "Todas"), y la tabla seguir igual
    private void rearmarFiltro() {
        CategoriaRecurso elegida = cmbFiltro.getValue();
        CategoriaRecurso misma = elegida == null ? null : model.getCategorias().stream()
                .filter(c -> c.getId().equals(elegida.getId()))
                .findFirst().orElse(null);
        rearmandoFiltro = true;
        try {
            opcionesFiltro.setAll(model.getCategorias());
            opcionesFiltro.add(0, null);
            cmbFiltro.getSelectionModel().select(opcionesFiltro.indexOf(misma));
        } finally {
            rearmandoFiltro = false;
        }
        if (elegida != null && misma == null && controller != null) {
            // la categoría del filtro ya no existe: el combo quedó en "Todas", la tabla también
            controller.buscarPorCategoria(null);
        }
    }

    private ListCell<CategoriaRecurso> celdaCategoria() {
        return new ListCell<>() {
            @Override protected void updateItem(CategoriaRecurso c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(vacio || c == null ? null : c.getDescripcion());
            }
        };
    }

    private ListCell<CategoriaRecurso> celdaFiltro() {
        return new ListCell<>() {
            @Override protected void updateItem(CategoriaRecurso c, boolean vacio) {
                super.updateItem(c, vacio);
                setText(c == null ? "Todas" : c.getDescripcion());
            }
        };
    }

    private StringConverter<CategoriaRecurso> convertidorCategoria() {
        return new StringConverter<>() {
            @Override
            public String toString(CategoriaRecurso c) {
                return c == null ? "" : c.getDescripcion();
            }
            @Override
            public CategoriaRecurso fromString(String s) {
                return null;
            }
        };
    }

    public void limpiarFormulario() {
        seleccionado = null;
        txtId.clear();
        txtId.setEditable(true);
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        txtBusqueda.clear();
        tablaRecursos.getSelectionModel().clearSelection();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RecursoModel.ERROR.equals(evt.getPropertyName())) {
            lblError.setText(model.getError());
        }
    }
}