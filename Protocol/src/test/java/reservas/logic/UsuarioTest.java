package reservas.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void equalsConMismoId() {
        Funcionario a = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        Funcionario b = new Funcionario("u1", "otraClave", "Otro", "1111-1111");
        assertEquals(a, b);
    }

    @Test
    void equalsConDistintoId() {
        Funcionario a = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        Funcionario b = new Funcionario("u2", "clave", "Nombre", "0000-0000");
        assertNotEquals(a, b);
    }

    @Test
    void equalsConsigoMismo() {
        Funcionario f = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        assertEquals(f, f);
    }

    @Test
    void equalsConNull() {
        Funcionario f = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        assertNotEquals(null, f);
    }

    @Test
    void equalsEntreSubclasesConMismoId() {
        Funcionario f = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        Administrador a = new Administrador("u1", "clave");
        // equals() en Usuario usa instanceof Usuario → true para subclases con mismo id
        assertEquals(f, a);
    }

    @Test
    void rolFuncionarioAsignado() {
        Funcionario f = new Funcionario("u1", "clave", "Nombre", "0000-0000");
        assertEquals(Rol.FUNCIONARIO, f.getRol());
    }

    @Test
    void rolAdministradorAsignado() {
        Administrador a = new Administrador("a1", "clave");
        assertEquals(Rol.ADMINISTRADOR, a.getRol());
    }
}
