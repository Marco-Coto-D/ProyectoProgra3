package reservas.presentation.clave;

import reservas.logic.AutenticacionService;
import reservas.presentation.Sesion;

public class CambiarClaveController {

    private final CambiarClaveView view;
    private final CambiarClaveModel model;
    private final AutenticacionService autenticacionService;

    public CambiarClaveController(CambiarClaveView view, CambiarClaveModel model,
                                  AutenticacionService autenticacionService) {
        this.view = view;
        this.model = model;
        this.autenticacionService = autenticacionService;

        view.setController(this);
        view.setModel(model);
    }

    public void cambiarClave(String claveActual, String claveNueva, String confirmarClave) {
        if (claveActual.isBlank() || claveNueva.isBlank() || confirmarClave.isBlank()) {
            model.setError("Completá todos los campos");
            return;
        }
        if (!claveActual.equals(Sesion.getUsuario().getClave())) {
            model.setError("Clave actual incorrecta");
            return;
        }
        if (!claveNueva.equals(confirmarClave)) {
            model.setError("Las claves nuevas no coinciden");
            return;
        }

        autenticacionService.cambiarClave(Sesion.getUsuario(), claveNueva);
        model.setError("");
        view.mostrarExito();
    }
}