package reservas.logic;

import java.util.Optional;

// el mantenimiento de funcionarios, categorías y recursos es solo del administrador;
// la interfaz ya esconde esas pestañas, pero el Backend tiene que bloquearlo igual
public class ValidadorRol {

    public static final String SOLO_ADMINISTRADOR = "Solo el administrador puede hacer esta operación";

    public static Optional<String> validarAdministrador(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return Optional.empty();
        }
        return Optional.of(SOLO_ADMINISTRADOR);
    }
}
