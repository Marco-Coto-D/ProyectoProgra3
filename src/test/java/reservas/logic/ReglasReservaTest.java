package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasReservaTest {

    private final Funcionario funcionario = new Funcionario("F-001", "clave", "Ana", "8888-0000");
    private final LocalDate hoy = LocalDate.of(2026, 3, 5);
    private final LocalTime ahora = LocalTime.of(10, 0);

    private Reserva reserva(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        Reserva reserva = new Reserva("RES-001", "Actividad", fecha, horaInicio, horaFin, funcionario);
        reserva.setEstado(EstadoReserva.ACTIVADA);
        return reserva;
    }

    @Test
    void reservaCanceladaNoPuedeCancelarseDeNuevo() {
        Reserva reserva = reserva(hoy.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        reserva.setEstado(EstadoReserva.CANCELADA);

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertEquals(Optional.of("Esta reserva ya está cancelada"), error);
        assertFalse(ReglasReserva.puedeCancelarse(reserva, hoy, ahora));
    }

    @Test
    void reservaDeFechaPasadaNoPuedeCancelarse() {
        Reserva reserva = reserva(hoy.minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertEquals(Optional.of("No se puede cancelar una reserva pasada"), error);
    }

    @Test
    void reservaDeHoyQueYaInicioNoPuedeCancelarse() {
        Reserva reserva = reserva(hoy, LocalTime.of(9, 0), LocalTime.of(11, 0));

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertEquals(Optional.of("Esta reserva ya inició, no se puede cancelar"), error);
        assertFalse(ReglasReserva.puedeCancelarse(reserva, hoy, ahora));
    }

    @Test
    void reservaDeHoyQueYaFinalizoTambienSeConsideraIniciada() {
        Reserva reserva = reserva(hoy, LocalTime.of(8, 0), LocalTime.of(9, 0));

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertEquals(Optional.of("Esta reserva ya inició, no se puede cancelar"), error);
    }

    @Test
    void reservaDeHoyQueAunNoInicioSiPuedeCancelarse() {
        Reserva reserva = reserva(hoy, LocalTime.of(11, 0), LocalTime.of(12, 0));

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertTrue(error.isEmpty());
        assertTrue(ReglasReserva.puedeCancelarse(reserva, hoy, ahora));
    }

    @Test
    void reservaFuturaSiPuedeCancelarse() {
        Reserva reserva = reserva(hoy.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        Optional<String> error = ReglasReserva.validarCancelacion(reserva, hoy, ahora);

        assertTrue(error.isEmpty());
    }
}
