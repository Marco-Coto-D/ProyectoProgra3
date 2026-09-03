package reservas.data.xml;

import org.junit.jupiter.api.Test;
import reservas.logic.Funcionario;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioRepositorioXmlIT extends XmlITBase {

    private final FuncionarioRepositorioXml repo = new FuncionarioRepositorioXml();

    @Test
    void guardarYListar() {
        repo.guardar(new Funcionario("f001", "clave1", "Ana López", "8888-0001"));

        List<Funcionario> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("f001", lista.get(0).getId());
        assertEquals("Ana López", lista.get(0).getNombre());
    }

    @Test
    void buscarPorId() {
        repo.guardar(new Funcionario("f002", "clave2", "Carlos Mora", "8888-0002"));

        Optional<Funcionario> resultado = repo.buscarPorId("f002");
        assertTrue(resultado.isPresent());
        assertEquals("Carlos Mora", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(repo.buscarPorId("noexiste").isEmpty());
    }

    @Test
    void guardarActualizaExistente() {
        repo.guardar(new Funcionario("f003", "vieja", "Luis", "0000-0000"));
        repo.guardar(new Funcionario("f003", "nueva", "Luis Actualizado", "1111-1111"));

        List<Funcionario> lista = repo.listarTodos();
        assertEquals(1, lista.size());
        assertEquals("Luis Actualizado", lista.get(0).getNombre());
    }

    @Test
    void borrar() {
        repo.guardar(new Funcionario("f004", "clave4", "Temporal", "0000-0000"));
        repo.borrar("f004");

        assertTrue(repo.buscarPorId("f004").isEmpty());
        assertEquals(0, repo.listarTodos().size());
    }

    @Test
    void buscarPorNombre() {
        repo.guardar(new Funcionario("f005", "clave5", "María Castro", "8888-0005"));
        repo.guardar(new Funcionario("f006", "clave6", "Pedro Ramírez", "8888-0006"));

        List<Funcionario> resultado = repo.buscarPorNombre("maría");
        assertEquals(1, resultado.size());
        assertEquals("f005", resultado.get(0).getId());
    }
}
