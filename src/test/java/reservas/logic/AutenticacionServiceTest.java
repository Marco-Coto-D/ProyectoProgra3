package reservas.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.data.interfaces.AdministradorRepositorio;
import reservas.data.interfaces.FuncionarioRepositorio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionServiceTest {

    private FakeFuncionarioRepo repoFuncionarios;
    private FakeAdminRepo repoAdmins;
    private AutenticacionService servicio;

    @BeforeEach
    void setUp() {
        repoFuncionarios = new FakeFuncionarioRepo();
        repoAdmins = new FakeAdminRepo();
        servicio = new AutenticacionService(repoFuncionarios, repoAdmins);

        repoFuncionarios.guardar(new Funcionario("f001", "clave123", "Ana López", "8888-0000"));
        repoAdmins.guardar(new Administrador("admin01", "admin123"));
    }

    @Test
    void autenticarFuncionarioCorrecto() {
        Usuario resultado = servicio.autenticar("f001", "clave123");
        assertNotNull(resultado);
        assertEquals("f001", resultado.getId());
        assertInstanceOf(Funcionario.class, resultado);
    }

    @Test
    void autenticarClaveIncorrecta() {
        assertNull(servicio.autenticar("f001", "claveMAL"));
    }

    @Test
    void autenticarIdInexistente() {
        assertNull(servicio.autenticar("noexiste", "clave123"));
    }

    @Test
    void autenticarAdministrador() {
        Usuario resultado = servicio.autenticar("admin01", "admin123");
        assertNotNull(resultado);
        assertEquals("admin01", resultado.getId());
        assertInstanceOf(Administrador.class, resultado);
    }

    @Test
    void cambiarClaveActualizaYPersiste() {
        Funcionario f = (Funcionario) servicio.autenticar("f001", "clave123");
        servicio.cambiarClave(f, "nuevaClave");

        assertEquals("nuevaClave", repoFuncionarios.buscarPorId("f001").get().getClave());
        assertNull(servicio.autenticar("f001", "clave123"));
        assertNotNull(servicio.autenticar("f001", "nuevaClave"));
    }

    @Test
    void cambiarClaveAdministrador() {
        Administrador a = (Administrador) servicio.autenticar("admin01", "admin123");
        servicio.cambiarClave(a, "nuevoAdmin");

        assertEquals("nuevoAdmin", repoAdmins.buscarPorId("admin01").get().getClave());
    }

    static class FakeFuncionarioRepo implements FuncionarioRepositorio {
        private final Map<String, Funcionario> datos = new HashMap<>();

        @Override
        public List<Funcionario> listarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public Optional<Funcionario> buscarPorId(String id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public List<Funcionario> buscarPorNombre(String nombre) {
            String filtro = nombre == null ? "" : nombre.toLowerCase();
            return datos.values().stream()
                    .filter(f -> f.getNombre().toLowerCase().contains(filtro))
                    .toList();
        }

        @Override
        public void guardar(Funcionario funcionario) {
            datos.put(funcionario.getId(), funcionario);
        }

        @Override
        public void borrar(String id) {
            datos.remove(id);
        }
    }

    static class FakeAdminRepo implements AdministradorRepositorio {
        private final Map<String, Administrador> datos = new HashMap<>();

        @Override
        public List<Administrador> listarTodos() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public Optional<Administrador> buscarPorId(String id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public void guardar(Administrador administrador) {
            datos.put(administrador.getId(), administrador);
        }

        @Override
        public void borrar(String id) {
            datos.remove(id);
        }
    }
}
