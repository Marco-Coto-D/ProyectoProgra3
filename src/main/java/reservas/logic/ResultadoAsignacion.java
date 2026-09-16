package reservas.logic;

import java.util.List;

public record ResultadoAsignacion(List<Recurso> asignados, List<String> categoriasSinDisponibilidad) {
}
