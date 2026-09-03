package reservas.data.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecursoRepositorioXmlIT extends XmlITBase {

    private final RecursoRepositorioXml repo = new RecursoRepositorioXml();
    private final CategoriaRecursoRepositorioXml repoCategoria = new CategoriaRecursoRepositorioXml();

    private CategoriaRecurso catSalas;
    private CategoriaRecurso catEquipos;

    @BeforeEach
    void prepararCategorias() {
        // Las categorías deben existir en el XML antes de guardar recursos
        // porque Recurso.categoria usa @XmlIDREF
        catSalas = new CategoriaRecurso("catS", "Salas");
        catEquipos = new CategoriaRecurso("catE", "Equipos");
        repoCategoria.guardar(catSalas);
        repoCategoria.guardar(catEquipos);
    }

    @Test
    void guardarYListar() {
        repo.guardar(new Recurso("r001", "Sala A", catSalas));

        List<Recurso> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("r001", lista.get(0).getId());
        assertEquals("Sala A", lista.get(0).getDescripcion());
    }

    @Test
    void buscarPorId() {
        repo.guardar(new Recurso("r002", "Proyector", catEquipos));

        Optional<Recurso> resultado = repo.buscarPorId("r002");
        assertTrue(resultado.isPresent());
        assertEquals("Proyector", resultado.get().getDescripcion());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(repo.buscarPorId("noexiste").isEmpty());
    }

    @Test
    void guardarActualizaExistente() {
        repo.guardar(new Recurso("r003", "Original", catSalas));
        repo.guardar(new Recurso("r003", "Actualizado", catSalas));

        List<Recurso> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("Actualizado", lista.get(0).getDescripcion());
    }

    @Test
    void borrar() {
        repo.guardar(new Recurso("r004", "Temporal", catSalas));
        repo.borrar("r004");

        assertTrue(repo.buscarPorId("r004").isEmpty());
        assertEquals(0, repo.listarTodos().size());
    }

    @Test
    void buscarPorCategoria() {
        repo.guardar(new Recurso("r005", "Sala B", catSalas));
        repo.guardar(new Recurso("r006", "Laptop", catEquipos));

        List<Recurso> salas = repo.buscarPorCategoria("catS");
        assertEquals(1, salas.size());
        assertEquals("r005", salas.get(0).getId());

        List<Recurso> equipos = repo.buscarPorCategoria("catE");
        assertEquals(1, equipos.size());
        assertEquals("r006", equipos.get(0).getId());
    }

    @Test
    void categoriaResueltaAlCargar() {
        repo.guardar(new Recurso("r007", "Sala C", catSalas));

        Recurso cargado = repo.buscarPorId("r007").get();
        assertNotNull(cargado.getCategoria());
        assertEquals("catS", cargado.getCategoria().getId());
        assertEquals("Salas", cargado.getCategoria().getDescripcion());
    }
}
