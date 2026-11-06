package reservas.util;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

// el converter por defecto del DatePicker corrige solo las fechas que no existen (31/02 -> 28/02)
// y tira una excepcion a la consola si el texto no es fecha; este las rechaza y devuelve null
public class FechaConverter extends StringConverter<LocalDate> {
    public static final String MENSAJE_INVALIDA = "Fecha inválida, usá el formato día/mes/año";

    // STRICT para que no corrija dias que no existen; uuuu porque con STRICT yyyy pide la era
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    // true si el campo quedo vacio porque el usuario escribio algo que no es fecha,
    // para distinguirlo de cuando simplemente no la puso
    private final SimpleBooleanProperty invalida = new SimpleBooleanProperty(false);

    public static void aplicar(DatePicker... fechas) {
        for (DatePicker fecha : fechas) {
            fecha.setConverter(new FechaConverter());
        }
    }

    public static boolean esInvalida(DatePicker fecha) {
        // si tiene valor es una fecha válida, aunque la marca todavía no se haya actualizado
        return fecha.getValue() == null && fecha.getConverter() instanceof FechaConverter c && c.isInvalida();
    }

    public static ReadOnlyBooleanProperty invalidaProperty(DatePicker fecha) {
        return ((FechaConverter) fecha.getConverter()).invalidaProperty();
    }

    /** para cuando la pantalla vacía el campo a propósito (botón Limpiar, etc.) */
    public static void reiniciar(DatePicker fecha) {
        if (fecha.getConverter() instanceof FechaConverter c) {
            c.reiniciar();
        }
    }

    public boolean isInvalida() {
        return invalida.get();
    }

    public ReadOnlyBooleanProperty invalidaProperty() {
        return invalida;
    }

    public void reiniciar() {
        invalida.set(false);
    }

    @Override
    public String toString(LocalDate fecha) {
        // una fecha que se muestra (elegida en el calendario o cargada) es válida;
        // con null no se toca, porque es lo que pasa justo después de rechazar un texto
        if (fecha != null) {
            invalida.set(false);
        }
        return fecha == null ? "" : FORMATO.format(fecha);
    }

    @Override
    public LocalDate fromString(String texto) {
        if (texto == null || texto.isBlank()) {
            invalida.set(false);
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto.trim(), FORMATO);
            invalida.set(false);
            return fecha;
        } catch (DateTimeParseException e) {
            invalida.set(true);
            return null;
        }
    }
}
