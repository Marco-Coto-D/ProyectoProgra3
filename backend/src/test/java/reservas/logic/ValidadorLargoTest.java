package reservas.logic;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorLargoTest {

    private static String texto(int largo) {
        return "x".repeat(largo);
    }

    private static final CategoriaRecurso CATEGORIA = new CategoriaRecurso("CAT-001", "Proyector");

    // ---- categoría ----

    @Test
    void categoriaConIdMuyLargoDaError() {
        Optional<String> resultado = ValidadorLargo.validarCategoria(new CategoriaRecurso(texto(51), "Proyector"));

        assertTrue(resultado.isPresent());
        assertEquals("El id no puede tener más de 50 caracteres", resultado.get());
    }

    @Test
    void categoriaConDescripcionMuyLargaDaError() {
        Optional<String> resultado = ValidadorLargo.validarCategoria(new CategoriaRecurso("CAT-001", texto(151)));

        assertTrue(resultado.isPresent());
        assertEquals("La descripción no puede tener más de 150 caracteres", resultado.get());
    }

    @Test
    void categoriaEnElLimiteNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarCategoria(new CategoriaRecurso(texto(50), texto(150)));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void categoriaNuevaSinIdNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarCategoria(new CategoriaRecurso(null, "Proyector"));

        assertTrue(resultado.isEmpty());
    }

    // ---- recurso ----

    @Test
    void recursoConIdMuyLargoDaError() {
        Optional<String> resultado = ValidadorLargo.validarRecurso(new Recurso(texto(51), "Epson", CATEGORIA));

        assertTrue(resultado.isPresent());
        assertEquals("El id no puede tener más de 50 caracteres", resultado.get());
    }

    @Test
    void recursoConDescripcionMuyLargaDaError() {
        Optional<String> resultado = ValidadorLargo.validarRecurso(new Recurso("PROY-01", texto(151), CATEGORIA));

        assertTrue(resultado.isPresent());
        assertEquals("La descripción no puede tener más de 150 caracteres", resultado.get());
    }

    @Test
    void recursoEnElLimiteNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarRecurso(new Recurso(texto(50), texto(150), CATEGORIA));

        assertTrue(resultado.isEmpty());
    }

    // ---- funcionario ----

    @Test
    void funcionarioConIdMuyLargoDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario(texto(51), null, "Ana", "8888-0000"));

        assertTrue(resultado.isPresent());
        assertEquals("El id no puede tener más de 50 caracteres", resultado.get());
    }

    @Test
    void funcionarioConNombreMuyLargoDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario("F001", null, texto(151), "8888-0000"));

        assertTrue(resultado.isPresent());
        assertEquals("El nombre no puede tener más de 150 caracteres", resultado.get());
    }

    @Test
    void funcionarioConTelefonoMuyLargoDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario("F001", null, "Ana", texto(31)));

        assertTrue(resultado.isPresent());
        assertEquals("El teléfono no puede tener más de 30 caracteres", resultado.get());
    }

    @Test
    void funcionarioConClaveMuyLargaDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario("F001", texto(101), "Ana", "8888-0000"));

        assertTrue(resultado.isPresent());
        assertEquals("La clave no puede tener más de 100 caracteres", resultado.get());
    }

    @Test
    void funcionarioEnElLimiteNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario(texto(50), texto(100), texto(150), texto(30)));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void funcionarioNuevoSinClaveNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarFuncionario(new Funcionario("F001", null, "Ana", "8888-0000"));

        assertTrue(resultado.isEmpty());
    }

    // ---- reserva ----

    @Test
    void actividadMuyLargaDaError() {
        Optional<String> resultado = ValidadorLargo.validarActividad(texto(201));

        assertTrue(resultado.isPresent());
        assertEquals("La actividad no puede tener más de 200 caracteres", resultado.get());
    }

    @Test
    void actividadEnElLimiteNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarActividad(texto(200));

        assertTrue(resultado.isEmpty());
    }

    // ---- cambio de clave ----

    @Test
    void claveNuevaMuyLargaDaError() {
        Optional<String> resultado = ValidadorLargo.validarClave(texto(101));

        assertTrue(resultado.isPresent());
        assertEquals("La clave no puede tener más de 100 caracteres", resultado.get());
    }

    @Test
    void claveNuevaEnElLimiteNoDaError() {
        Optional<String> resultado = ValidadorLargo.validarClave(texto(100));

        assertTrue(resultado.isEmpty());
    }
}
