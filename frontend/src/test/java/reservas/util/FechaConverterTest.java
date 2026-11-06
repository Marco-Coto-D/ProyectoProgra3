package reservas.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FechaConverterTest {

    private final FechaConverter converter = new FechaConverter();

    // ---- fecha válida ----

    @Test
    void fechaValidaSeConvierte() {
        assertEquals(LocalDate.of(2026, 11, 16), converter.fromString("16/11/2026"));
    }

    @Test
    void fechaValidaConCerosOSinCerosSeConvierte() {
        assertEquals(LocalDate.of(2027, 1, 3), converter.fromString("03/01/2027"));
        assertEquals(LocalDate.of(2027, 1, 3), converter.fromString("3/1/2027"));
    }

    @Test
    void fechaValidaConEspaciosAlrededorSeConvierte() {
        assertEquals(LocalDate.of(2026, 11, 16), converter.fromString("  16/11/2026 "));
    }

    @Test
    void veintinueveDeFebreroEnAnioBisiestoSeConvierte() {
        assertEquals(LocalDate.of(2028, 2, 29), converter.fromString("29/02/2028"));
    }

    // ---- formato inválido ----

    @Test
    void textoQueNoEsFechaDevuelveNull() {
        assertNull(converter.fromString("abc"));
    }

    @Test
    void otrosFormatosDeFechaDevuelvenNull() {
        assertNull(converter.fromString("2026-11-16"));
        assertNull(converter.fromString("16-11-2026"));
        assertNull(converter.fromString("16/11"));
    }

    @Test
    void anioDeDosDigitosDevuelveNull() {
        assertNull(converter.fromString("16/11/26"));
    }

    // ---- fecha que no existe en el calendario ----

    @Test
    void treintaYUnoDeFebreroDevuelveNullYNoSeCorrige() {
        assertNull(converter.fromString("31/02/2027"));
    }

    @Test
    void veintinueveDeFebreroEnAnioNoBisiestoDevuelveNull() {
        assertNull(converter.fromString("29/02/2027"));
    }

    @Test
    void treintaYUnoEnMesDeTreintaDiasDevuelveNull() {
        assertNull(converter.fromString("31/04/2027"));
    }

    @Test
    void mesOdiaFueraDeRangoDevuelveNull() {
        assertNull(converter.fromString("15/13/2027"));
        assertNull(converter.fromString("0/01/2027"));
    }

    // ---- texto vacío ----

    @Test
    void textoVacioONullDevuelveNull() {
        assertNull(converter.fromString(""));
        assertNull(converter.fromString("   "));
        assertNull(converter.fromString(null));
    }

    // ---- mostrar ----

    @Test
    void fechaSeMuestraComoDiaMesAnio() {
        assertEquals("3/11/2026", converter.toString(LocalDate.of(2026, 11, 3)));
    }

    @Test
    void fechaNullSeMuestraVacia() {
        assertEquals("", converter.toString(null));
    }

    @Test
    void loQueSeMuestraSeVuelveALeerIgual() {
        LocalDate fecha = LocalDate.of(2026, 12, 31);
        assertEquals(fecha, converter.fromString(converter.toString(fecha)));
    }

    // ---- marca de fecha inválida (para el mensaje específico) ----

    @Test
    void alEmpezarNoEstaInvalida() {
        assertFalse(converter.isInvalida());
    }

    @Test
    void textoQueNoEsFechaLaMarcaInvalida() {
        converter.fromString("abc");
        assertTrue(converter.isInvalida());
    }

    @Test
    void fechaQueNoExisteLaMarcaInvalida() {
        converter.fromString("31/02/2027");
        assertTrue(converter.isInvalida());
    }

    @Test
    void campoVacioNoEsInvalidoSinoQueFalta() {
        converter.fromString("abc");
        converter.fromString("");
        assertFalse(converter.isInvalida());
    }

    @Test
    void fechaValidaDespuesDeUnaInvalidaLimpiaLaMarca() {
        converter.fromString("abc");
        converter.fromString("16/11/2026");
        assertFalse(converter.isInvalida());
    }

    @Test
    void elegirFechaEnElCalendarioLimpiaLaMarca() {
        converter.fromString("abc");
        converter.toString(LocalDate.of(2026, 11, 16));
        assertFalse(converter.isInvalida());
    }

    @Test
    void mostrarElCampoVacioNoLimpiaLaMarca() {
        // es lo que hace el DatePicker justo después de rechazar el texto
        converter.fromString("abc");
        converter.toString(null);
        assertTrue(converter.isInvalida());
    }

    @Test
    void reiniciarLimpiaLaMarca() {
        converter.fromString("abc");
        converter.reiniciar();
        assertFalse(converter.isInvalida());
    }
}
