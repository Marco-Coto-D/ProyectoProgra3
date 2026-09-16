package reservas.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class ReglasReserva {

    public static boolean puedeCancelarse(Reserva reserva, LocalDate hoy, LocalTime ahora) {
        return validarCancelacion(reserva, hoy, ahora).isEmpty();
    }

    public static Optional<String> validarCancelacion(Reserva reserva, LocalDate hoy, LocalTime ahora) {
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            return Optional.of("Esta reserva ya está cancelada");
        }
        if (reserva.getFecha().isBefore(hoy)) {
            return Optional.of("No se puede cancelar una reserva pasada");
        }
        if (reserva.getFecha().equals(hoy) && !ahora.isBefore(reserva.getHoraInicio())) {
            return Optional.of("Esta reserva ya inició, no se puede cancelar");
        }
        return Optional.empty();
    }
}
