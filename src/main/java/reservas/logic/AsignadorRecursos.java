package reservas.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsignadorRecursos {

    public ResultadoAsignacion asignar(List<CategoriaRecurso> categoriasSolicitadas, List<Recurso> recursosDelSistema, Set<String> idsOcupados) {
        List<Recurso> asignados = new ArrayList<>();
        List<String> categoriasSinDisponibilidad = new ArrayList<>();
        Set<String> ocupados = new HashSet<>(idsOcupados);

        for (CategoriaRecurso categoria : categoriasSolicitadas) {
            Recurso disponible = recursosDelSistema.stream()
                    .filter(r -> r.getCategoria().getId().equals(categoria.getId()))
                    .filter(r -> !ocupados.contains(r.getId()))
                    .findFirst()
                    .orElse(null);

            if (disponible == null) {
                categoriasSinDisponibilidad.add(categoria.getDescripcion());
            } else {
                asignados.add(disponible);
                ocupados.add(disponible.getId());
            }
        }

        return new ResultadoAsignacion(asignados, categoriasSinDisponibilidad);
    }
}
