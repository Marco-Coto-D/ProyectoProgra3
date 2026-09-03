package reservas.data.xml;

import org.junit.jupiter.api.Test;
import reservas.logic.Administrador;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorRepositorioXmlIT extends XmlITBase {

    private final AdministradorRepositorioXml repo = new AdministradorRepositorioXml();

    @Test
    void guardarYListar() {
        repo.guardar(new Administrador("admin1", "clave1"));

        List<Administrador> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("admin1", lista.get(0).getId());
    }

    @Test
    void buscarPorId() {
        repo.guardar(new Administrador("admin2", "clave2"));

        Optional<Administrador> resultado = repo.buscarPorId("admin2");
        assertTrue(resultado.isPresent());
        assertEquals("clave2", resultado.get().getClave());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(repo.buscarPorId("noexiste").isEmpty());
    }

    @Test
    void guardarActualizaExistente() {
        repo.guardar(new Administrador("admin3", "claveVieja"));
        repo.guardar(new Administrador("admin3", "claveNueva"));

        List<Administrador> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("claveNueva", lista.get(0).getClave());
    }

    @Test
    void borrar() {
        repo.guardar(new Administrador("admin4", "clave4"));
        repo.borrar("admin4");

        assertTrue(repo.buscarPorId("admin4").isEmpty());
        assertEquals(0, repo.listarTodos().size());
    }
}
