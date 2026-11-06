package reservas.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorRolTest {

    private static final String ERROR = "Solo el administrador puede hacer esta operación";

    @Test
    void administradorNoDaError() {
        assertTrue(ValidadorRol.validarAdministrador(new Administrador("999", "999")).isEmpty());
    }

    @Test
    void funcionarioDaError() {
        Optional<String> error = ValidadorRol.validarAdministrador(new Funcionario("101", "101", "Ana", "8888-0000"));
        assertEquals(Optional.of(ERROR), error);
    }

    @Test
    void sinUsuarioDaError() {
        assertEquals(Optional.of(ERROR), ValidadorRol.validarAdministrador(null));
    }

    @Test
    void funcionarioConRolCambiadoSigueDandoError() {
        // lo que manda es el tipo que armó el Backend al autenticar, no un campo que se pueda tocar
        Funcionario funcionario = new Funcionario("101", "101", "Ana", "8888-0000");
        funcionario.setRol(Rol.ADMINISTRADOR);
        assertEquals(Optional.of(ERROR), ValidadorRol.validarAdministrador(funcionario));
    }
}
