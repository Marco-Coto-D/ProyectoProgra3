package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadisticaUtilTest {

    @Test
    void completarSemanasIncluyeLasQueNoTienenActividad() {
        LocalDate lunes1 = LocalDate.of(2026, 3, 2);
        LocalDate lunes3 = LocalDate.of(2026, 3, 16);

        Map<LocalDate, Long> conteoPorLunes = Map.of(lunes1, 3L, lunes3, 1L);

        Map<String, Long> resultado = EstadisticaUtil.completarSemanas(
                conteoPorLunes, LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 18));

        assertEquals(3, resultado.size());
        assertEquals(List.of("2026-03-02", "2026-03-09", "2026-03-16"), List.copyOf(resultado.keySet()));
        assertEquals(3L, resultado.get("2026-03-02"));
        assertEquals(0L, resultado.get("2026-03-09"));
        assertEquals(1L, resultado.get("2026-03-16"));
    }

    @Test
    void completarSemanasConRangoDeUnaSolaSemana() {
        Map<String, Long> resultado = EstadisticaUtil.completarSemanas(
                Map.of(), LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 6));

        assertEquals(Map.of("2026-03-02", 0L), resultado);
    }

    @Test
    void resolverDescripcionesUsaLaDescripcionActual() {
        CategoriaRecurso laptop = new CategoriaRecurso("CAT-001", "Laptop renombrada");
        CategoriaRecurso proyector = new CategoriaRecurso("CAT-002", "Proyector");

        Map<String, Long> conteoPorId = Map.of("CAT-001", 4L, "CAT-002", 2L);

        Map<String, Long> resultado = EstadisticaUtil.resolverDescripciones(conteoPorId, List.of(laptop, proyector));

        assertEquals(Map.of("Laptop renombrada", 4L, "Proyector", 2L), resultado);
    }

    @Test
    void resolverDescripcionesConIdInexistenteUsaElIdComoFallback() {
        Map<String, Long> conteoPorId = Map.of("CAT-999", 1L);

        Map<String, Long> resultado = EstadisticaUtil.resolverDescripciones(conteoPorId, List.of());

        assertEquals(Map.of("CAT-999", 1L), resultado);
    }

    @Test
    void resolverDescripcionesSumaCuandoDosIdsComparteDescripcion() {
        CategoriaRecurso original = new CategoriaRecurso("CAT-001", "Sala");
        CategoriaRecurso renombrada = new CategoriaRecurso("CAT-002", "Sala");

        Map<String, Long> conteoPorId = Map.of("CAT-001", 3L, "CAT-002", 2L);

        Map<String, Long> resultado = EstadisticaUtil.resolverDescripciones(conteoPorId, List.of(original, renombrada));

        assertEquals(Map.of("Sala", 5L), resultado);
    }
}
