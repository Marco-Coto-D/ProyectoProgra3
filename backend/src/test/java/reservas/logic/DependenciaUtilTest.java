package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependenciaUtilTest {

    private final CategoriaRecurso categoria = new CategoriaRecurso("CAT-001", "Salas");
    private final Recurso recursoA = new Recurso("R-001", "Sala A", categoria);
    private final Recurso recursoB = new Recurso("R-002", "Sala B", categoria);
    private final Funcionario funcionario = new Funcionario("F-001", "clave", "Ana", "8888-0000");

    private Reserva reservaConRecursos(Recurso... recursos) {
        Reserva reserva = new Reserva("RES-001", "Actividad", LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(9, 0), funcionario);
        reserva.setRecursos(List.of(recursos));
        return reserva;
    }

    @Test
    void listaVaciaNoTieneReservas() {
        assertFalse(DependenciaUtil.recursoTieneReservas(List.of(), "R-001"));
    }

    @Test
    void ningunaReservaUsaElRecurso() {
        List<Reserva> reservas = List.of(reservaConRecursos(recursoB));

        assertFalse(DependenciaUtil.recursoTieneReservas(reservas, "R-001"));
    }

    @Test
    void unaReservaUsaElRecurso() {
        List<Reserva> reservas = List.of(reservaConRecursos(recursoA));

        assertTrue(DependenciaUtil.recursoTieneReservas(reservas, "R-001"));
    }

    @Test
    void recursoUsadoEntreVariosRecursosDeUnaReserva() {
        List<Reserva> reservas = List.of(reservaConRecursos(recursoB, recursoA));

        assertTrue(DependenciaUtil.recursoTieneReservas(reservas, "R-001"));
    }
}
