package reservas.protocolo;

import reservas.logic.Reserva;

import java.io.Serializable;

/**
 * Mensaje asíncrono que el Backend empuja por el socket de notificaciones
 * a todos los Frontends conectados, salvo el que originó el cambio.
 * Viaja con la Reserva completa para que el panel de Alertas pueda mostrar
 * el detalle sin volver a llamar al Backend.
 */
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TipoNotificacion tipo;
    private final Reserva reserva;

    public Notificacion(TipoNotificacion tipo, Reserva reserva) {
        this.tipo = tipo;
        this.reserva = reserva;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public Reserva getReserva() {
        return reserva;
    }

    @Override
    public String toString() {
        return "Notificacion{" + tipo + ", reserva=" + (reserva != null ? reserva.getId() : null) + "}";
    }
}
