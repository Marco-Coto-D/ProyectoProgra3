package reservas.protocolo;

import reservas.logic.Reserva;

import java.io.Serializable;

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
