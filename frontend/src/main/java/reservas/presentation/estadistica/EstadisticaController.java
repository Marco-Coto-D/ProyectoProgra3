package reservas.presentation.estadistica;

import reservas.logic.EstadisticaUtil;
import reservas.logic.EstadoReserva;
import reservas.proxy.BackendProxy;
import reservas.util.FechaConverter;
import reservas.util.PdfUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstadisticaController {
    private final EstadisticaView view;
    private final EstadisticaModel model;
    private final BackendProxy proxy;

    public EstadisticaController(EstadisticaView view, EstadisticaModel model,
                                 BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);
    }

    public void cargarRecursos(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            model.setErrorRecursos("Seleccioná ambas fechas");
            return;
        }
        if (hasta.isBefore(desde)) {
            model.setErrorRecursos("'Hasta' debe ser posterior a 'Desde'");
            return;
        }

        Map<String, Long> conteo;
        try {
            Map<String, Long> conteoPorId = proxy.listarReservasEntre(desde, hasta).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                    .flatMap(r -> r.getRecursos().stream())
                    .collect(Collectors.groupingBy(
                            rec -> rec.getCategoria().getId(),
                            Collectors.counting()
                    ));
            conteo = EstadisticaUtil.resolverDescripciones(conteoPorId, proxy.listarCategorias());
        } catch (Exception e) {
            model.setErrorRecursos(e.getMessage());
            return;
        }

        model.setErrorRecursos("");
        model.setConteoRecursos(conteo);
    }

    public void fechaInvalidaRecursos() {
        model.setErrorRecursos(FechaConverter.MENSAJE_INVALIDA);
    }

    public void fechaInvalidaActividades() {
        model.setErrorActividades(FechaConverter.MENSAJE_INVALIDA);
    }

    public void printRecursos() {
        Map<String, Long> conteo = model.getConteoRecursos();
        if (conteo == null || conteo.isEmpty()) {
            model.setErrorRecursos("Cargá los datos antes de generar el PDF");
            return;
        }
        List<String> encabezados = List.of("Categoría", "Cantidad");
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            filas.add(List.of(e.getKey(), String.valueOf(e.getValue())));
        }
        try {
            PdfUtil.generar("Estadística - Recursos por categoría", encabezados, filas, view.graficoRecursos());
            model.setErrorRecursos("");
        } catch (Exception e) {
            model.setErrorRecursos("Error al generar PDF: " + e.getMessage());
        }
    }

    public void cargarActividades(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            model.setErrorActividades("Seleccioná ambas fechas");
            return;
        }
        if (hasta.isBefore(desde)) {
            model.setErrorActividades("'Hasta' debe ser posterior a 'Desde'");
            return;
        }

        Map<LocalDate, Long> conteoPorLunes;
        try {
            conteoPorLunes = proxy.listarReservasEntre(desde, hasta).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                    .collect(Collectors.groupingBy(
                            r -> r.getFecha().with(DayOfWeek.MONDAY),
                            Collectors.counting()
                    ));
        } catch (Exception e) {
            model.setErrorActividades(e.getMessage());
            return;
        }

        Map<String, Long> conteo = EstadisticaUtil.completarSemanas(conteoPorLunes, desde, hasta);

        model.setErrorActividades("");
        model.setConteoActividades(conteo);
    }

    public void printActividades() {
        Map<String, Long> conteo = model.getConteoActividades();
        if (conteo == null || conteo.isEmpty()) {
            model.setErrorActividades("Cargá los datos antes de generar el PDF");
            return;
        }
        List<String> encabezados = List.of("Semana (lunes)", "Cantidad");
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            filas.add(List.of(e.getKey(), String.valueOf(e.getValue())));
        }
        try {
            PdfUtil.generar("Estadística - Actividades por semana", encabezados, filas, view.graficoActividades());
            model.setErrorActividades("");
        } catch (Exception e) {
            model.setErrorActividades("Error al generar PDF: " + e.getMessage());
        }
    }
}