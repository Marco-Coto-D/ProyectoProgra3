package reservas.logic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParseoSeguroTest {

    @Test
    void parsearFechaConNullDaNull() {
        assertNull(ParseoSeguro.parsearFecha(null));
    }

    @Test
    void parsearFechaConFormatoInvalidoDaNull() {
        assertNull(ParseoSeguro.parsearFecha("no-es-una-fecha"));
    }

    @Test
    void parsearFechaConFormatoValidoParsea() {
        assertEquals(LocalDate.of(2026, 3, 5), ParseoSeguro.parsearFecha("2026-03-05"));
    }

    @Test
    void parsearHoraConNullDaNull() {
        assertNull(ParseoSeguro.parsearHora(null));
    }

    @Test
    void parsearHoraConFormatoInvalidoDaNull() {
        assertNull(ParseoSeguro.parsearHora("no-es-una-hora"));
    }

    @Test
    void parsearHoraConFormatoValidoParsea() {
        assertEquals(LocalTime.of(9, 30), ParseoSeguro.parsearHora("09:30"));
    }
}
