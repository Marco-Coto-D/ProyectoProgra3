package reservas.presentation.login;

import reservas.logic.AutenticacionService;
import reservas.logic.Usuario;
import reservas.util.PdfUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CambioClaveLoginController {

    private final CambioClaveLoginView view;
    private final CambioClaveLoginModel model;
    private final AutenticacionService autenticacionService;
    private final String id;

    private boolean cambioRealizado = false;

    public CambioClaveLoginController(CambioClaveLoginView view, CambioClaveLoginModel model, AutenticacionService autenticacionService, String id) {
        this.view = view;
        this.model = model;
        this.autenticacionService = autenticacionService;
        this.id = id;

        view.setController(this);
        view.setModel(model);
    }

    public void cambiar(String claveActual, String claveNueva, String confirmarClave) {
        if (claveActual.isBlank() || claveNueva.isBlank() || confirmarClave.isBlank()) {
            model.setError("Completá todos los campos");
            return;
        }
        if (!claveNueva.equals(confirmarClave)) {
            model.setError("Las claves nuevas no coinciden");
            return;
        }

        Usuario usuario = autenticacionService.autenticar(id, claveActual);
        if (usuario == null) {
            model.setError("Clave actual incorrecta");
            return;
        }

        autenticacionService.cambiarClave(usuario, claveNueva);
        cambioRealizado = true;
        model.setError("");
        view.mostrarExito();
    }

    public void print() {
        if (!cambioRealizado) {
            model.setError("Cambiá la clave antes de generar el comprobante");
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        List<String> encabezados = List.of("Usuario", "Fecha", "Hora");
        List<List<String>> filas = List.of(List.of(id, ahora.toLocalDate().toString(), ahora.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        ));
        try {
            PdfUtil.generar("Comprobante de cambio de clave", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }
}
