package reservas.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DatosReservaIA(
        String actividad,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        List<CategoriaRecurso> categorias
) {}
