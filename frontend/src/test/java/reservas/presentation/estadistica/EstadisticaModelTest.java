package reservas.presentation.estadistica;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticaModelTest {

    @Test
    void alEmpezarNoHayNadaCargado() {
        EstadisticaModel model = new EstadisticaModel();

        assertNull(model.getConteoRecursos());
        assertNull(model.getConteoActividades());
    }

    @Test
    void cargarRecursosLoGuardaYAvisaSoloEseCambio() {
        EstadisticaModel model = new EstadisticaModel();
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.setConteoRecursos(Map.of("Laptop", 2L));

        assertEquals(Map.of("Laptop", 2L), model.getConteoRecursos());
        assertNull(model.getConteoActividades());
        assertEquals(List.of(EstadisticaModel.RECURSOS), avisos);
    }

    @Test
    void cargarActividadesLoGuardaYAvisaSoloEseCambio() {
        EstadisticaModel model = new EstadisticaModel();
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.setConteoActividades(Map.of("2030-06-03", 3L));

        assertEquals(Map.of("2030-06-03", 3L), model.getConteoActividades());
        assertNull(model.getConteoRecursos());
        assertEquals(List.of(EstadisticaModel.ACTIVIDADES), avisos);
    }

    @Test
    void errorAlCargarActividadesBorraLoAnteriorYDejaElMensaje() {
        EstadisticaModel model = new EstadisticaModel();
        model.setConteoActividades(Map.of("2030-06-03", 3L));
        model.setConteoRecursos(Map.of("Laptop", 2L));
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.errorAlCargarActividades("'Hasta' debe ser posterior a 'Desde'");

        assertNull(model.getConteoActividades());
        assertEquals("'Hasta' debe ser posterior a 'Desde'", model.getErrorActividades());
        assertEquals(List.of(EstadisticaModel.ACTIVIDADES, EstadisticaModel.ERROR_ACTIVIDADES), avisos);
        // la otra sección no se toca
        assertEquals(Map.of("Laptop", 2L), model.getConteoRecursos());
    }

    @Test
    void errorAlCargarRecursosBorraLoAnteriorYDejaElMensaje() {
        EstadisticaModel model = new EstadisticaModel();
        model.setConteoRecursos(Map.of("Laptop", 2L));
        model.setConteoActividades(Map.of("2030-06-03", 3L));
        List<String> avisos = new ArrayList<>();
        model.addPropertyChangeListener(e -> avisos.add(e.getPropertyName()));

        model.errorAlCargarRecursos("Seleccioná ambas fechas");

        assertNull(model.getConteoRecursos());
        assertEquals("Seleccioná ambas fechas", model.getErrorRecursos());
        assertEquals(List.of(EstadisticaModel.RECURSOS, EstadisticaModel.ERROR_RECURSOS), avisos);
        assertEquals(Map.of("2030-06-03", 3L), model.getConteoActividades());
    }
}
