package reservas.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorIdTest {

    @Test
    void esNuevoConIdExistenteDaError() {
        Optional<String> resultado = ValidadorId.validarIdParaCrear(true, true, "F001");

        assertTrue(resultado.isPresent());
        assertEquals("Ya existe un registro con el id F001", resultado.get());
    }

    @Test
    void esNuevoConIdNoExistenteNoDaError() {
        Optional<String> resultado = ValidadorId.validarIdParaCrear(true, false, "F001");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void esEdicionConIdExistenteNoDaError() {
        Optional<String> resultado = ValidadorId.validarIdParaCrear(false, true, "F001");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void esEdicionConIdNoExistenteNoDaError() {
        Optional<String> resultado = ValidadorId.validarIdParaCrear(false, false, "F001");

        assertTrue(resultado.isEmpty());
    }
}
