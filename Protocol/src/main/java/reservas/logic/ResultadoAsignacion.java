package reservas.logic;

import java.io.Serializable;
import java.util.List;

public record ResultadoAsignacion(List<Recurso> asignados, List<String> categoriasSinDisponibilidad)
        implements Serializable {
}
