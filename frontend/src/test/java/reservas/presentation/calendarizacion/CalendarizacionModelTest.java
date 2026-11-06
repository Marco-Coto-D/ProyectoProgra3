package reservas.presentation.calendarizacion;

import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarizacionModelTest {

    private static final CategoriaRecurso SALAS = new CategoriaRecurso("CAT-001", "Sala");
    private static final Recurso SALA_A = new Recurso("SALA-A", "Sala A", SALAS);
    private static final Recurso SALA_B = new Recurso("SALA-B", "Sala B", SALAS);
    private static final LocalDate DIA = LocalDate.of(2030, 6, 3);

    private static Reserva reserva(String actividad, int desde, int hasta, Recurso... recursos) {
        Reserva r = new Reserva(actividad, actividad, DIA, LocalTime.of(desde, 0), LocalTime.of(hasta, 0),
                new Funcionario("F-1", "x", "Ana", "1"));
        r.setRecursos(List.of(recursos));
        return r;
    }

    @Test
    void reservaEnDevuelveLaQueOcupaEseRecursoAEsaHora() {
        CalendarizacionModel model = new CalendarizacionModel();
        model.setCalendario(DIA, List.of(SALA_A, SALA_B), List.of(reserva("Reunión", 9, 11, SALA_A)));

        assertEquals("Reunión", model.reservaEn(SALA_A, LocalTime.of(9, 0)).getActividad());
        assertEquals("Reunión", model.reservaEn(SALA_A, LocalTime.of(10, 0)).getActividad());
    }

    @Test
    void reservaEnDevuelveNullSiEstaLibre() {
        CalendarizacionModel model = new CalendarizacionModel();
        model.setCalendario(DIA, List.of(SALA_A, SALA_B), List.of(reserva("Reunión", 9, 11, SALA_A)));

        assertNull(model.reservaEn(SALA_B, LocalTime.of(9, 0)));
        assertNull(model.reservaEn(SALA_A, LocalTime.of(8, 0)));
    }

    @Test
    void laHoraDeFinYaQuedaLibre() {
        CalendarizacionModel model = new CalendarizacionModel();
        model.setCalendario(DIA, List.of(SALA_A), List.of(reserva("Reunión", 9, 11, SALA_A)));

        assertNull(model.reservaEn(SALA_A, LocalTime.of(11, 0)));
    }

    @Test
    void cargarElCalendarioAvisaALaVista() {
        CalendarizacionModel model = new CalendarizacionModel();
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.setCalendario(DIA, List.of(SALA_A), List.of());

        assertEquals(List.of(CalendarizacionModel.CALENDARIO), avisos);
        assertEquals(DIA, model.getFecha());
        assertEquals(List.of(SALA_A), model.getRecursos());
    }

    @Test
    void limpiarDejaElCalendarioVacioYAvisa() {
        CalendarizacionModel model = new CalendarizacionModel();
        model.setCalendario(DIA, List.of(SALA_A), List.of(reserva("Reunión", 9, 11, SALA_A)));
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.limpiarCalendario();

        assertNull(model.getFecha());
        assertTrue(model.getRecursos().isEmpty());
        assertNull(model.reservaEn(SALA_A, LocalTime.of(9, 0)));
        assertEquals(List.of(CalendarizacionModel.CALENDARIO), avisos);
    }
}
