package reservas.presentation.login;

import reservas.logic.ResultadoAutenticacion;
import reservas.presentation.Sesion;
import reservas.proxy.BackendProxy;

public class LoginController {

    private final LoginView view;
    private final LoginModel model;
    private final BackendProxy proxy;

    public LoginController(LoginView view, LoginModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;
        view.setController(this);
        view.setModel(model);
    }

    public void intentarLogin(String id, String clave) {
        try {
            ResultadoAutenticacion resultado = proxy.autenticar(id, clave);
            model.setError("");
            Sesion.setUsuario(resultado.usuario());
            Sesion.setSesionId(resultado.sesionId());
            view.cerrar();
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void cancelar() {
        view.cerrar();
    }

    public void abrirCambioClave(String id) {
        if (id == null || id.isBlank()) {
            model.setError("Escribí tu id primero");
            return;
        }
        CambioClaveLoginView dialogo = new CambioClaveLoginView(id);
        CambioClaveLoginModel dialogoModel = new CambioClaveLoginModel();
        new CambioClaveLoginController(dialogo, dialogoModel, proxy, id);
        dialogo.mostrar();
    }
}