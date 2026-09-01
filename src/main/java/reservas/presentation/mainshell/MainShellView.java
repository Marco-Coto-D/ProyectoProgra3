package reservas.presentation.mainshell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.function.Supplier;

public class MainShellView implements PropertyChangeListener {

    @FXML private TabPane tabs;

    private final Parent root;
    private final Stage stage;
    private MainShellModel model;

    public MainShellView(Stage stage) {
        this.stage = stage;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainShellView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar MainShellView.fxml", e);
        }

        tabs.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> cargarSiHaceFalta(actual));
    }

    public void setModel(MainShellModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void agregarPestana(String titulo, Supplier<Parent> contenido) {
        Tab tab = new Tab(titulo);
        tab.setClosable(false);
        tab.setUserData(contenido);
        tabs.getTabs().add(tab);

        if (tabs.getTabs().size() == 1) {
            cargarSiHaceFalta(tab);
        }
    }

    @SuppressWarnings("unchecked")
    private void cargarSiHaceFalta(Tab tab) {
        if (tab == null || tab.getContent() != null) {
            return;
        }
        Supplier<Parent> contenido = (Supplier<Parent>) tab.getUserData();
        tab.setContent(contenido.get());
    }

    public void mostrar() {
        stage.setTitle("Sistema de Reserva de Recursos");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (MainShellModel.USUARIO.equals(evt.getPropertyName()) && model.getUsuario() != null) {
            stage.setTitle("Sistema de Reserva de Recursos - " + model.getUsuario().getId());
        }
    }
}