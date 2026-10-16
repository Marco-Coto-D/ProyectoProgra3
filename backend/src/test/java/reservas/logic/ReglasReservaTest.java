package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final CategoriaRecurso categoria = new CategoriaRecurso("CAT-1", "Proyectores");

    private Reserva reservaCon(String id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                               EstadoReserva estado, String... recursos) {
        Reserva reserva = new Reserva(id, "Actividad", fecha, horaInicio, horaFin, funcionario);
        reserva.setEstado(estado);
        for (String recurso : recursos) {
            reserva.getRecursos().add(new Recurso(recurso, "Recurso " + recurso, categoria));
        }
        return reserva;
    }

    private Set<String> ocupadosDe10a11(Reserva... reservas) {
        return ReglasReserva.recursosOcupados(List.of(reservas), hoy, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Test
    void reservaSuperpuestaOcupaSusRecursos() {
        Reserva otra = reservaCon("R-1", hoy, LocalTime.of(9, 30), LocalTime.of(10, 30), EstadoReserva.ACTIVADA, "P1", "P2");

        assertEquals(Set.of("P1", "P2"), ocupadosDe10a11(otra));
    }

    @Test
    void otraQueTerminaALas10NoSeSuperponeConUnaQueEmpiezaALas10() {
        Reserva otra = reservaCon("R-1", hoy, LocalTime.of(9, 0), LocalTime.of(10, 0), EstadoReserva.ACTIVADA, "P1");

        assertTrue(ocupadosDe10a11(otra).isEmpty());
    }

    @Test
    void otraQueEmpiezaALas11NoSeSuperponeConUnaQueTerminaALas11() {
        Reserva otra = reservaCon("R-1", hoy, LocalTime.of(11, 0), LocalTime.of(12, 0), EstadoReserva.ACTIVADA, "P1");

        assertTrue(ocupadosDe10a11(otra).isEmpty());
    }

    @Test
    void otraQueContieneOEstaContenidaEnElHorarioSeSuperpone() {
        Reserva contiene = reservaCon("R-1", hoy, LocalTime.of(8, 0), LocalTime.of(12, 0), EstadoReserva.ACTIVADA, "P1");
        Reserva contenida = reservaCon("R-2", hoy, LocalTime.of(10, 15), LocalTime.of(10, 45), EstadoReserva.ACTIVADA, "P2");

        assertEquals(Set.of("P1", "P2"), ocupadosDe10a11(contiene, contenida));
    }

    @Test
    void mismoHorarioExactoSeSuperpone() {
        Reserva otra = reservaCon("R-1", hoy, LocalTime.of(10, 0), LocalTime.of(11, 0), EstadoReserva.ACTIVADA, "P1");

        assertEquals(Set.of("P1"), ocupadosDe10a11(otra));
    }

    @Test
    void reservaCanceladaNoOcupaRecursos() {
        Reserva otra = reservaCon("R-1", hoy, LocalTime.of(10, 0), LocalTime.of(11, 0), EstadoReserva.CANCELADA, "P1");

        assertTrue(ocupadosDe10a11(otra).isEmpty());
    }

    @Test
    void reservaDeOtroDiaNoOcupaRecursos() {
        Reserva otra = reservaCon("R-1", hoy.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), EstadoReserva.ACTIVADA, "P1");

        assertTrue(ocupadosDe10a11(otra).isEmpty());
    }

    @Test
    void recursoRepetidoEnVariasReservasApareceUnaSolaVez() {
        Reserva una = reservaCon("R-1", hoy, LocalTime.of(9, 30), LocalTime.of(10, 15), EstadoReserva.ACTIVADA, "P1");
        Reserva otra = reservaCon("R-2", hoy, LocalTime.of(10, 45), LocalTime.of(11, 30), EstadoReserva.ACTIVADA, "P1", "P3");

        assertEquals(Set.of("P1", "P3"), ocupadosDe10a11(una, otra));
    }

    @Test
    void sinReservasNoHayNadaOcupado() {
        assertTrue(ocupadosDe10a11().isEmpty());
    }
}
