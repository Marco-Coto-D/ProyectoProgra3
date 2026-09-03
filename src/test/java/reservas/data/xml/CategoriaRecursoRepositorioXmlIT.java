package reservas.data.xml;

import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaRecursoRepositorioXmlIT extends XmlITBase {

    private final CategoriaRecursoRepositorioXml repo = new CategoriaRecursoRepositorioXml();

    @Test
    void guardarYListar() {
        repo.guardar(new CategoriaRecurso("cat1", "Salas"));

        List<CategoriaRecurso> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("cat1", lista.get(0).getId());
        assertEquals("Salas", lista.get(0).getDescripcion());
    }

    @Test
    void buscarPorId() {
        repo.guardar(new CategoriaRecurso("cat2", "Equipos"));

        Optional<CategoriaRecurso> resultado = repo.buscarPorId("cat2");
        assertTrue(resultado.isPresent());
        assertEquals("Equipos", resultado.get().getDescripcion());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(repo.buscarPorId("noexiste").isEmpty());
    }

    @Test
    void guardarActualizaExistente() {
        repo.guardar(new CategoriaRecurso("cat3", "Original"));
        repo.guardar(new CategoriaRecurso("cat3", "Actualizado"));

        List<CategoriaRecurso> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("Actualizado", lista.get(0).getDescripcion());
    }

    @Test
    void borrar() {
        repo.guardar(new CategoriaRecurso("cat4", "Temporal"));
        repo.borrar("cat4");

        assertTrue(repo.buscarPorId("cat4").isEmpty());
        assertEquals(0, repo.listarTodos().size());
    }

    @Test
    void buscarPorDescripcion() {
        repo.guardar(new CategoriaRecurso("cat5", "Sala de conferencias"));
        repo.guardar(new CategoriaRecurso("cat6", "Proyector"));

        List<CategoriaRecurso> resultado = repo.buscarPorDescripcion("sala");
        assertEquals(1, resultado.size());
        assertEquals("cat5", resultado.get(0).getId());
    }
}
