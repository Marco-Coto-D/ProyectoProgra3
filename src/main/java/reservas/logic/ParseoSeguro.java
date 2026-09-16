package reservas.logic;

import java.time.LocalDate;
import java.time.LocalTime;

public class ParseoSeguro {

    public static LocalDate parsearFecha(String valor) {
        if (valor == null) return null;
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalTime parsearHora(String valor) {
        if (valor == null) return null;
        try {
            return LocalTime.parse(valor);
        } catch (Exception e) {
            return null;
        }
    }
}
