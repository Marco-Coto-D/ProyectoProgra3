package reservas.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// la interfaz ya no deja mandar campos vacíos, pero por el protocolo sí se puede
public class ValidadorCampos {

    public static final String RESERVA_INCOMPLETA = "Completá actividad, fecha, horario y al menos una categoría";
    public static final String CLAVE_VACIA = "La clave nueva no puede estar vacía";

    public static Optional<String> validarReserva(String actividad, LocalDate fecha, LocalTime horaInicio,
                                                  LocalTime horaFin, List<CategoriaRecurso> categorias) {
        if (actividad == null || actividad.isBlank() || fecha == null || horaInicio == null || horaFin == null
                || categorias == null || categorias.isEmpty()) {
            return Optional.of(RESERVA_INCOMPLETA);
        }
        return Optional.empty();
    }

    public static Optional<String> validarClaveNueva(String clave) {
        if (clave == null || clave.isBlank()) {
            return Optional.of(CLAVE_VACIA);
        }
        return Optional.empty();
    }
}
