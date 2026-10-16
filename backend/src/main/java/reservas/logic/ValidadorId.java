package reservas.logic;

import java.util.Optional;

public class ValidadorId {

    public static Optional<String> validarIdParaCrear(boolean esNuevo, boolean idYaExiste, String id) {
        if (esNuevo && idYaExiste) {
            return Optional.of("Ya existe un registro con el id " + id);
        }
        return Optional.empty();
    }
}
