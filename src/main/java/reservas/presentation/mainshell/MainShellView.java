package reservas.presentation.mainshell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainShellView implements PropertyChangeListener {

    @FXML private TabPane tabs;

    private final Parent root;
    private final Stage stage;
    private final Map<Tab, Runnable> refrescos = new HashMap<>();
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
                .addListener((obs, anterior, actual) -> seleccionarPestana(actual));
    }

    public void setModel(MainShellModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void agregarPestana(String titulo, String icono, Supplier<Parent> contenido) {
        agregarPestana(titulo, icono, contenido, null);
    }

    public void agregarPestana(String titulo, String icono, Supplier<Parent> contenido, Runnable refrescar) {
        Tab tab = new Tab(titulo);
        tab.setClosable(false);
        tab.setUserData(contenido);
        tab.setGraphic(icono(icono, 18));
        if (refrescar != null) {
            refrescos.put(tab, refrescar);
        }
        tabs.getTabs().add(tab);

        if (tabs.getTabs().size() == 1) {
            cargarSiHaceFalta(tab);
        }
    }

    private void seleccionarPestana(Tab tab) {
        if (tab == null) {
            return;
        }
        if (tab.getContent() == null) {
            cargarSiHaceFalta(tab);
            return;
        }
        Runnable refrescar = refrescos.get(tab);
        if (refrescar != null) {
            refrescar.run();
        }
    }

    private ImageView icono(String archivo, double tamano) {
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/" + archivo)));
        iv.setFitWidth(tamano);
        iv.setFitHeight(tamano);
        return iv;
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
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/reservas/presentation/iconos/icon.png")));
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