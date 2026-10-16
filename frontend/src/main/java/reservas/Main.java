package reservas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import reservas.presentation.Sesion;
import reservas.presentation.mainshell.MainShellController;
import reservas.presentation.mainshell.MainShellModel;
import reservas.presentation.mainshell.MainShellView;
import reservas.presentation.login.LoginController;
import reservas.presentation.login.LoginModel;
import reservas.presentation.login.LoginView;
import reservas.proxy.BackendProxy;
import reservas.proxy.ReceptorNotificaciones;

import java.util.Properties;

public class Main extends Application {
    public static final String PROPERTIES_FILE_NAME = "/servidor.properties";

    private BackendProxy proxy;
    private final ReceptorNotificaciones receptor = new ReceptorNotificaciones();
    private String host;
    private int puertoNotificaciones;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);

        proxy = new BackendProxy();
        try {
            conectar();
        } catch (Exception e) {
            Alert alerta = new Alert(AlertType.ERROR,
                    "No se pudo conectar con el servidor (" + e.getMessage() + ")");
            alerta.setHeaderText(null);
            alerta.showAndWait();
            Platform.exit();
            return;
        }

        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun(primaryStage);
        } else {
            proxy.desconectar();
            Platform.exit();
        }
    }

    private void conectar() throws Exception {
        Properties prop = new Properties();
        prop.load(getClass().getResourceAsStream(PROPERTIES_FILE_NAME));
        host = prop.getProperty("servidor_host");
        int puerto = Integer.parseInt(prop.getProperty("servidor_puerto"));
        puertoNotificaciones = Integer.parseInt(prop.getProperty("servidor_puerto_notificaciones"));
        proxy.conectar(host, puerto);
    }

    private void doLogin() {
        LoginView view = new LoginView();
        LoginModel model = new LoginModel();
        new LoginController(view, model, proxy);

        view.mostrar();
    }

    private void doRun(Stage stage) {
        // con setImplicitExit(false) la app no se cierra sola al cerrar la ventana
        stage.setOnHidden(e -> {
            receptor.desconectar();
            proxy.desconectar();
            Platform.exit();
        });

        MainShellView view = new MainShellView(stage);
        MainShellModel model = new MainShellModel();
        MainShellController controller = new MainShellController(view, model, proxy);
        controller.iniciar();

        try {
            receptor.conectar(host, puertoNotificaciones, Sesion.getSesionId(), controller::recibirNotificacion);
        } catch (Exception e) {
            Alert alerta = new Alert(AlertType.WARNING,
                    "No se pudo conectar a las notificaciones del servidor (" + e.getMessage() + ")");
            alerta.setHeaderText(null);
            alerta.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
