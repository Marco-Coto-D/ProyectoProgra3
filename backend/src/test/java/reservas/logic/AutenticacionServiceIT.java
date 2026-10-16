package reservas.logic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.data.mysql.AdministradorDao;
import reservas.data.mysql.Database;
import reservas.data.mysql.FuncionarioDao;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionServiceIT {
    private AutenticacionService servicio;

    @BeforeEach
    void setUp() throws Exception {
        borrarDatosDePrueba();
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)",
                "TEST-F001", "clave123", "Ana López", "8888-0000");
        ejecutar("insert into administrador (id, clave) values (?, ?)",
                "TEST-ADMIN", "admin123");
        servicio = new AutenticacionService();
    }

    @AfterEach
    void tearDown() throws Exception {
        borrarDatosDePrueba();
    }

    @Test
    void autenticarFuncionarioCorrecto() {
        Usuario resultado = servicio.autenticar("TEST-F001", "clave123");
        assertNotNull(resultado);
        assertEquals("TEST-F001", resultado.getId());
        assertInstanceOf(Funcionario.class, resultado);
        assertEquals("Ana López", ((Funcionario) resultado).getNombre());
    }

    @Test
    void autenticarClaveIncorrecta() {
        assertNull(servicio.autenticar("TEST-F001", "claveMAL"));
    }

    @Test
    void autenticarIdInexistente() {
        assertNull(servicio.autenticar("TEST-NOEXISTE", "clave123"));
    }

    @Test
    void autenticarAdministrador() {
        Usuario resultado = servicio.autenticar("TEST-ADMIN", "admin123");
        assertNotNull(resultado);
        assertEquals("TEST-ADMIN", resultado.getId());
        assertInstanceOf(Administrador.class, resultado);
    }

    @Test
    void cambiarClaveActualizaYPersiste() {
        Funcionario f = (Funcionario) servicio.autenticar("TEST-F001", "clave123");
        servicio.cambiarClave(f, "nuevaClave");

        assertEquals("nuevaClave", new FuncionarioDao().read("TEST-F001").getClave());
        assertNull(servicio.autenticar("TEST-F001", "clave123"));
        assertNotNull(servicio.autenticar("TEST-F001", "nuevaClave"));
    }

    @Test
    void cambiarClaveAdministrador() {
        Administrador a = (Administrador) servicio.autenticar("TEST-ADMIN", "admin123");
        servicio.cambiarClave(a, "nuevoAdmin");

        assertEquals("nuevoAdmin", new AdministradorDao().read("TEST-ADMIN").getClave());
    }

    private void borrarDatosDePrueba() throws Exception {
        ejecutar("delete from funcionario where id like 'TEST-%'");
        ejecutar("delete from administrador where id like 'TEST-%'");
    }

    private void ejecutar(String sql, String... parametros) throws Exception {
        PreparedStatement stm = Database.instance().prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            stm.setString(i + 1, parametros[i]);
        }
        stm.executeUpdate();
    }
}
