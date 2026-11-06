package reservas.logic;

import java.util.Optional;
import java.util.regex.Pattern;

public class ValidadorTelefono {
    // numeros separados por un espacio o un guion, con + opcional al inicio: 88882222, 8888-2222, +506 8888-2222
    private static final Pattern FORMATO = Pattern.compile("\\+?\\d+([ -]\\d+)*");

    public static Optional<String> validar(String telefono) {
        // el telefono puede ir vacio en la base; que no falte lo revisa el Frontend
        if (telefono == null || telefono.isBlank()) {
            return Optional.empty();
        }
        if (!FORMATO.matcher(telefono.trim()).matches()) {
            return Optional.of("El teléfono solo puede tener números, espacios o guiones");
        }
        return Optional.empty();
    }
}
