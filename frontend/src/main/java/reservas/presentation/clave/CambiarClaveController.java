package reservas.presentation.clave;

import reservas.presentation.Sesion;
import reservas.proxy.BackendProxy;
import reservas.util.PdfUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CambiarClaveController {
    private final CambiarClaveView view;
    private final CambiarClaveModel model;
    private final BackendProxy proxy;

    private boolean cambioRealizado = false;

    public CambiarClaveController(CambiarClaveView view, CambiarClaveModel model,
                                  BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);
    }

    public void cambiarClave(String claveActual, String claveNueva, String confirmarClave) {
        if (claveActual.isBlank() || claveNueva.isBlank() || confirmarClave.isBlank()) {
            model.setError("Completá todos los campos");
            return;
        }
        if (!claveNueva.equals(confirmarClave)) {
            model.setError("Las claves nuevas no coinciden");
            return;
        }

        try {
            proxy.cambiarClave(Sesion.getUsuario().getId(), claveActual, claveNueva);
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }
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