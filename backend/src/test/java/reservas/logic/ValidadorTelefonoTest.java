package reservas.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTelefonoTest {

    private static final String ERROR = "El teléfono solo puede tener números, espacios o guiones";

    @Test
    void soloDigitosNoDaError() {
        assertTrue(ValidadorTelefono.validar("88882222").isEmpty());
    }

    @Test
    void conGuionNoDaError() {
        assertTrue(ValidadorTelefono.validar("8888-2222").isEmpty());
    }

    @Test
    void conEspacioNoDaError() {
        assertTrue(ValidadorTelefono.validar("2222 3333").isEmpty());
    }

    @Test
    void conCodigoDePaisNoDaError() {
        assertTrue(ValidadorTelefono.validar("+506 8888-2222").isEmpty());
    }

    @Test
    void vacioNoDaError() {
        assertTrue(ValidadorTelefono.validar(null).isEmpty());
        assertTrue(ValidadorTelefono.validar("").isEmpty());
    }

    @Test
    void conLetrasDaError() {
        Optional<String> resultado = ValidadorTelefono.validar("abc");

        assertTrue(resultado.isPresent());
        assertEquals(ERROR, resultado.get());
    }

    @Test
    void digitosMezcladosConLetrasDaError() {
        Optional<String> resultado = ValidadorTelefono.validar("8888-22a2");

        assertTrue(resultado.isPresent());
        assertEquals(ERROR, resultado.get());
    }

    @Test
    void otrosSimbolosDaError() {
        Optional<String> resultado = ValidadorTelefono.validar("(506) 8888.2222");

        assertTrue(resultado.isPresent());
        assertEquals(ERROR, resultado.get());
    }

    @Test
    void soloSeparadoresDaError() {
        Optional<String> resultado = ValidadorTelefono.validar("- -");

        assertTrue(resultado.isPresent());
        assertEquals(ERROR, resultado.get());
    }

    @Test
    void separadorDobleOAlFinalDaError() {
        assertEquals(Optional.of(ERROR), ValidadorTelefono.validar("8888--2222"));
        assertEquals(Optional.of(ERROR), ValidadorTelefono.validar("8888-"));
    }
}
