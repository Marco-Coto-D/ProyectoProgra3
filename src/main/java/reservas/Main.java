package reservas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import reservas.data.xml.AdministradorRepositorioXml;
import reservas.data.xml.FuncionarioRepositorioXml;
import reservas.logic.AutenticacionService;
import reservas.presentation.Sesion;
import reservas.presentation.mainshell.MainShellController;
import reservas.presentation.mainshell.MainShellModel;
import reservas.presentation.mainshell.MainShellView;
import reservas.presentation.login.LoginController;
import reservas.presentation.login.LoginModel;
import reservas.presentation.login.LoginView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun(primaryStage);
        } else {
            Platform.exit();
        }
    }

    private void doLogin() {
        AutenticacionService servicio = new AutenticacionService(
                new FuncionarioRepositorioXml(),
                new AdministradorRepositorioXml()
        );

        LoginView view = new LoginView();
        LoginModel model = new LoginModel();
        new LoginController(view, model, servicio);

        view.mostrar();
    }

    private void doRun(Stage stage) {
        MainShellView view = new MainShellView(stage);
        MainShellModel model = new MainShellModel();
        MainShellController controller = new MainShellController(view, model);
        controller.iniciar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}