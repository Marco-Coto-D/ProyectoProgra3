package reservas.presentation.actividad;

import org.junit.jupiter.api.Test;
import reservas.logic.Funcionario;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActividadModelTest {

    private static final LocalDate LUNES = LocalDate.of(2030, 6, 3);

    private static Reserva reserva(String actividad, LocalDate fecha, int desde, int hasta) {
        return new Reserva(actividad, actividad, fecha, LocalTime.of(desde, 0), LocalTime.of(hasta, 0),
                new Funcionario("F-1", "x", "Ana", "1"));
    }

    private static List<String> actividades(List<Reserva> reservas) {
        return reservas.stream().map(Reserva::getActividad).toList();
    }

    @Test
    void reservasEnFiltraPorDiaYHora() {
        ActividadModel model = new ActividadModel();
        model.setSemana(LUNES, List.of(
                reserva("Lunes 9-11", LUNES, 9, 11),
                reserva("Martes 9-10", LUNES.plusDays(1), 9, 10),
                reserva("Lunes 14-15", LUNES, 14, 15)));

        assertEquals(List.of("Lunes 9-11"), actividades(model.reservasEn(LUNES, LocalTime.of(10, 0))));
        assertEquals(List.of("Martes 9-10"), actividades(model.reservasEn(LUNES.plusDays(1), LocalTime.of(9, 0))));
        assertTrue(model.reservasEn(LUNES, LocalTime.of(12, 0)).isEmpty());
    }

    @Test
    void variasReservasALaMismaHoraAparecenTodas() {
        ActividadModel model = new ActividadModel();
        model.setSemana(LUNES, List.of(reserva("A", LUNES, 9, 10), reserva("B", LUNES, 9, 11)));

        assertEquals(List.of("A", "B"), actividades(model.reservasEn(LUNES, LocalTime.of(9, 0))));
    }

    @Test
    void cargarLaSemanaAvisaALaVista() {
        ActividadModel model = new ActividadModel();
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.setSemana(LUNES, List.of());

        assertEquals(List.of(ActividadModel.SEMANA), avisos);
        assertEquals(LUNES, model.getLunes());
    }

    @Test
    void limpiarDejaLaSemanaVacia() {
        ActividadModel model = new ActividadModel();
        model.setSemana(LUNES, List.of(reserva("A", LUNES, 9, 10)));

        model.limpiarSemana();

        assertNull(model.getLunes());
        assertTrue(model.reservasEn(LUNES, LocalTime.of(9, 0)).isEmpty());
    }
}
