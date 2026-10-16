package reservas.presentation.alertas;

import javafx.application.Platform;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.protocolo.Notificacion;
import reservas.protocolo.TipoNotificacion;

public class AlertasController {
    private final AlertasView view;
    private final AlertasModel model;

    public AlertasController(AlertasView view, AlertasModel model) {
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }

    public void recibir(Notificacion notificacion) {
        if (notificacion.getTipo() == TipoNotificacion.NUEVA_RESERVA) {
            Reserva reserva = notificacion.getReserva();
            // viene del hilo del receptor, por eso el runLater
            Platform.runLater(() -> model.agregar(reserva));
        }
    }

    public void ver(Reserva reserva) {
        view.mostrarDetalle(reserva.getActividad(), detalle(reserva));
    }

    public void descartar(Reserva reserva) {
        if (view.confirmar("¿Descartar la alerta de «" + reserva.getActividad() + "»?")) {
            model.quitar(reserva);
        }
    }

    private String detalle(Reserva r) {
        StringBuilder texto = new StringBuilder();
        texto.append("Encargado: ").append(r.getFuncionario().getNombre()).append('\n');
        texto.append("Fecha: ").append(r.getFecha()).append('\n');
        texto.append("Desde: ").append(r.getHoraInicio()).append('\n');
        texto.append("Hasta: ").append(r.getHoraFin()).append('\n');
        texto.append("Recursos:");
        for (Recurso recurso : r.getRecursos()) {
            texto.append("\n  • ").append(recurso.getDescripcion())
                    .append(" (").append(recurso.getCategoria().getDescripcion()).append(')');
        }
        return texto.toString();
    }
}
