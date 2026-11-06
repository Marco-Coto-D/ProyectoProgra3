package reservas.presentation.reserva;

import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;
import reservas.logic.EstadoReserva;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaControllerTest {

    private static Reserva reservaCon(Recurso... recursos) {
        Reserva reserva = new Reserva("R-1", "Reunión", LocalDate.of(2030, 6, 3), LocalTime.of(9, 0), LocalTime.of(10, 0),
                new Funcionario("101", "x", "Ana", "1"));
        reserva.setRecursos(List.of(recursos));
        return reserva;
    }

    @Test
    void mensajeDeExitoNombraElRecursoAsignado() {
        Recurso sala = new Recurso("SALA-02", "Sala B", new CategoriaRecurso("CAT-001", "Sala de reuniones"));

        assertEquals("Reserva creada con éxito. Se asignó el primer recurso disponible de cada categoría. "
                        + "Sala de reuniones: SALA-02 - Sala B",
                ReservaController.mensajeExito(reservaCon(sala)));
    }

    @Test
    void mensajeDeExitoNombraUnRecursoPorCadaCategoria() {
        Recurso sala = new Recurso("SALA-02", "Sala B", new CategoriaRecurso("CAT-001", "Sala de reuniones"));
        Recurso proyector = new Recurso("PROY-01", "Epson", new CategoriaRecurso("CAT-002", "Proyector"));

        String mensaje = ReservaController.mensajeExito(reservaCon(sala, proyector));

        assertTrue(mensaje.endsWith("Sala de reuniones: SALA-02 - Sala B; Proyector: PROY-01 - Epson"));
    }

    @Test
    void unaReservaFuturaActivaSePuedeCancelar() {
        assertNull(ReservaController.motivoParaNoCancelar(reservaCon(), LocalDate.of(2030, 6, 2), LocalTime.of(15, 0)));
        assertNull(ReservaController.motivoParaNoCancelar(reservaCon(), LocalDate.of(2030, 6, 3), LocalTime.of(8, 59)));
    }

    @Test
    void noSeCancelaLaYaCanceladaNiLaPasadaNiLaQueYaInicio() {
        Reserva cancelada = reservaCon();
        cancelada.setEstado(EstadoReserva.CANCELADA);

        assertEquals("Esta reserva ya está cancelada",
                ReservaController.motivoParaNoCancelar(cancelada, LocalDate.of(2030, 6, 2), LocalTime.of(15, 0)));
        assertEquals("No se puede cancelar una reserva pasada",
                ReservaController.motivoParaNoCancelar(reservaCon(), LocalDate.of(2030, 6, 4), LocalTime.of(8, 0)));
        assertEquals("Esta reserva ya inició, no se puede cancelar",
                ReservaController.motivoParaNoCancelar(reservaCon(), LocalDate.of(2030, 6, 3), LocalTime.of(9, 0)));
    }

    @Test
    void elExitoAvisaConSuPropioEvento() {
        ReservaModel model = new ReservaModel();
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.setExito("Reserva creada con éxito");

        assertEquals("Reserva creada con éxito", model.getExito());
        assertEquals(List.of(ReservaModel.EXITO), avisos);
    }
}
