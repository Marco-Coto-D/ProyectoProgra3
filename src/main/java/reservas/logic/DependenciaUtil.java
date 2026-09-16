package reservas.logic;

import java.util.List;

public class DependenciaUtil {

    public static boolean recursoTieneReservas(List<Reserva> reservas, String recursoId) {
        return reservas.stream()
                .anyMatch(r -> r.getRecursos().stream()
                        .anyMatch(rec -> rec.getId().equals(recursoId)));
    }
}
