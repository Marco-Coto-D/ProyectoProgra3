package reservas.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AsignadorRecursosTest {

    private final AsignadorRecursos asignador = new AsignadorRecursos();

    private CategoriaRecurso salas;
    private CategoriaRecurso laptops;
    private Recurso sala1;
    private Recurso sala2;
    private Recurso laptop1;
    private List<Recurso> recursosDelSistema;

    @BeforeEach
    void prepararDatos() {
        salas = new CategoriaRecurso("CAT-001", "Sala de Juntas");
        laptops = new CategoriaRecurso("CAT-002", "Laptop Windows 11");

        sala1 = new Recurso("R-001", "Sala 1", salas);
        sala2 = new Recurso("R-002", "Sala 2", salas);
        laptop1 = new Recurso("R-003", "Laptop #1", laptops);

        recursosDelSistema = List.of(sala1, sala2, laptop1);
    }

    @Test
    void todasLasCategoriasTienenRecursoDisponible() {
        ResultadoAsignacion resultado = asignador.asignar(
                List.of(salas, laptops), recursosDelSistema, Set.of());

        assertTrue(resultado.categoriasSinDisponibilidad().isEmpty());
        assertEquals(2, resultado.asignados().size());
        assertTrue(resultado.asignados().contains(sala1));
        assertTrue(resultado.asignados().contains(laptop1));
    }

    @Test
    void categoriaSinNingunRecursoLibreQuedaSinDisponibilidad() {
        // laptop1 (unico recurso de la categoria laptops) ya esta ocupado
        ResultadoAsignacion resultado = asignador.asignar(
                List.of(salas, laptops), recursosDelSistema, Set.of("R-003"));

        assertEquals(List.of("Laptop Windows 11"), resultado.categoriasSinDisponibilidad());
        // de "salas" si se asigno algo; de "laptops" no se asigno nada
        assertEquals(1, resultado.asignados().size());
        assertEquals(sala1, resultado.asignados().get(0));
    }

    @Test
    void recursosYaOcupadosNoSeVuelvenAAsignar() {
        // sala1 ocupada -> debe asignar sala2, no sala1
        ResultadoAsignacion resultado = asignador.asignar(
                List.of(salas), recursosDelSistema, Set.of("R-001"));

        assertTrue(resultado.categoriasSinDisponibilidad().isEmpty());
        assertEquals(List.of(sala2), resultado.asignados());
    }

    @Test
    void conMasDeUnRecursoDisponibleEligeElPrimeroDeLaLista() {
        // ni sala1 ni sala2 estan ocupadas -> debe elegir sala1 (primera en recursosDelSistema)
        ResultadoAsignacion resultado = asignador.asignar(
                List.of(salas), recursosDelSistema, Set.of());

        assertEquals(List.of(sala1), resultado.asignados());
    }

    @Test
    void todasLasCategoriasSinDisponibilidadNoAsignaNada() {
        ResultadoAsignacion resultado = asignador.asignar(
                List.of(salas, laptops), recursosDelSistema, Set.of("R-001", "R-002", "R-003"));

        assertTrue(resultado.asignados().isEmpty());
        assertEquals(2, resultado.categoriasSinDisponibilidad().size());
        assertTrue(resultado.categoriasSinDisponibilidad().contains("Sala de Juntas"));
        assertTrue(resultado.categoriasSinDisponibilidad().contains("Laptop Windows 11"));
    }

    @Test
    void listaDeCategoriasVaciaNoAsignaNadaYNoFalla() {
        ResultadoAsignacion resultado = asignador.asignar(List.of(), recursosDelSistema, Set.of());

        assertTrue(resultado.asignados().isEmpty());
        assertTrue(resultado.categoriasSinDisponibilidad().isEmpty());
    }

    @Test
    void noMutaElSetDeIdsOcupadosQueLePasan() {
        Set<String> idsOcupados = new java.util.HashSet<>(Set.of("R-002"));
        asignador.asignar(List.of(salas), recursosDelSistema, idsOcupados);

        // el set original que le pasamos no debe haber sido modificado por el metodo
        assertEquals(Set.of("R-002"), idsOcupados);
    }
}
