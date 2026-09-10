package reservas.presentation.clave;

import reservas.logic.AutenticacionService;
import reservas.presentation.Sesion;
import reservas.util.PdfUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CambiarClaveController {

    private final CambiarClaveView view;
    private final CambiarClaveModel model;
    private final AutenticacionService autenticacionService;

    private boolean cambioRealizado = false;

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
        List<List<String>> filas = List.of(List.of(
                Sesion.getUsuario().getId(),
                ahora.toLocalDate().toString(),
                ahora.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        ));
        try {
            PdfUtil.generar("Comprobante de cambio de clave", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }
}