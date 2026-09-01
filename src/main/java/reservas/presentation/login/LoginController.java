package reservas.presentation.login;

import reservas.logic.AutenticacionService;
import reservas.logic.Usuario;
import reservas.presentation.Sesion;

public class LoginController {

    private final LoginView view;
    private final LoginModel model;
    private final AutenticacionService servicio;

    public LoginController(LoginView view, LoginModel model, AutenticacionService servicio) {
        this.view = view;
        this.model = model;
        this.servicio = servicio;
        view.setController(this);
        view.setModel(model);
    }

    public void intentarLogin(String id, String clave) {
        Usuario usuario = servicio.autenticar(id, clave);

        if (usuario == null) {
            model.setError("Usuario o clave incorrectos");
        } else {
            model.setError("");
            Sesion.setUsuario(usuario);
            view.cerrar();
        }
    }
}