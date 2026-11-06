package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorCamposTest {

    private static final LocalDate DIA = LocalDate.of(2030, 1, 7);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);
    private static final LocalTime DIEZ = LocalTime.of(10, 0);
    private static final List<CategoriaRecurso> UNA = List.of(new CategoriaRecurso("CAT-001", "Sala"));
    private static final Optional<String> INCOMPLETA = Optional.of(ValidadorCampos.RESERVA_INCOMPLETA);

    @Test
    void reservaCompletaNoDaError() {
        assertTrue(ValidadorCampos.validarReserva("Reunión", DIA, NUEVE, DIEZ, UNA).isEmpty());
    }

    @Test
    void actividadVaciaOSoloEspaciosDaError() {
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva(null, DIA, NUEVE, DIEZ, UNA));
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("", DIA, NUEVE, DIEZ, UNA));
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("   ", DIA, NUEVE, DIEZ, UNA));
    }

    @Test
    void sinFechaOSinHorasDaError() {
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("Reunión", null, NUEVE, DIEZ, UNA));
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("Reunión", DIA, null, DIEZ, UNA));
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("Reunión", DIA, NUEVE, null, UNA));
    }

    @Test
    void sinCategoriasDaError() {
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("Reunión", DIA, NUEVE, DIEZ, null));
        assertEquals(INCOMPLETA, ValidadorCampos.validarReserva("Reunión", DIA, NUEVE, DIEZ, List.of()));
    }

    @Test
    void claveNuevaNormalNoDaError() {
        assertTrue(ValidadorCampos.validarClaveNueva("nueva123").isEmpty());
    }

    @Test
    void claveNuevaVaciaOSoloEspaciosDaError() {
        Optional<String> vacia = Optional.of(ValidadorCampos.CLAVE_VACIA);
        assertEquals(vacia, ValidadorCampos.validarClaveNueva(null));
        assertEquals(vacia, ValidadorCampos.validarClaveNueva(""));
        assertEquals(vacia, ValidadorCampos.validarClaveNueva("   "));
    }
}
