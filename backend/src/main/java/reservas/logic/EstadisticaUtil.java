package reservas.logic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class EstadisticaUtil {

    public static Map<String, Long> completarSemanas(Map<LocalDate, Long> conteoPorLunes, LocalDate desde, LocalDate hasta) {
        Map<String, Long> conteo = new TreeMap<>();
        LocalDate lunes = desde.with(DayOfWeek.MONDAY);
        LocalDate ultimoLunes = hasta.with(DayOfWeek.MONDAY);
        while (!lunes.isAfter(ultimoLunes)) {
            conteo.put(lunes.toString(), conteoPorLunes.getOrDefault(lunes, 0L));
            lunes = lunes.plusWeeks(1);
        }
        return conteo;
    }

    public static Map<String, Long> resolverDescripciones(Map<String, Long> conteoPorId, List<CategoriaRecurso> categoriasDisponibles) {
        Map<String, String> descripcionPorId = categoriasDisponibles.stream()
                .collect(Collectors.toMap(CategoriaRecurso::getId, CategoriaRecurso::getDescripcion, (a, b) -> a));

        Map<String, Long> resultado = new TreeMap<>();
        for (Map.Entry<String, Long> e : conteoPorId.entrySet()) {
            String descripcion = descripcionPorId.getOrDefault(e.getKey(), e.getKey());
            resultado.merge(descripcion, e.getValue(), Long::sum);
        }
        return resultado;
    }
}
