package reservas.presentation.estadistica;

import reservas.logic.EstadisticaUtil;
import reservas.logic.EstadoReserva;
import reservas.proxy.BackendProxy;
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

    private Map<String, Long> ultimoConteoRecursos;
    private Map<String, Long> ultimoConteoActividades;

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

        ultimoConteoRecursos = conteo;
        model.setErrorRecursos("");
        view.mostrarRecursos(conteo);
    }

    public void printRecursos() {
        if (ultimoConteoRecursos == null || ultimoConteoRecursos.isEmpty()) {
            model.setErrorRecursos("Cargá los datos antes de generar el PDF");
            return;
        }
        List<String> encabezados = List.of("Categoría", "Cantidad");
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Long> e : ultimoConteoRecursos.entrySet()) {
            filas.add(List.of(e.getKey(), String.valueOf(e.getValue())));
        }
        try {
            PdfUtil.generar("Estadística - Recursos por categoría", encabezados, filas);
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

        ultimoConteoActividades = conteo;
        model.setErrorActividades("");
        view.mostrarActividades(conteo);
    }

    public void printActividades() {
        if (ultimoConteoActividades == null || ultimoConteoActividades.isEmpty()) {
            model.setErrorActividades("Cargá los datos antes de generar el PDF");
            return;
        }
        List<String> encabezados = List.of("Semana (lunes)", "Cantidad");
        List<List<String>> filas = new ArrayList<>();
        for (Map.Entry<String, Long> e : ultimoConteoActividades.entrySet()) {
            filas.add(List.of(e.getKey(), String.valueOf(e.getValue())));
        }
        try {
            PdfUtil.generar("Estadística - Actividades por semana", encabezados, filas);
            model.setErrorActividades("");
        } catch (Exception e) {
            model.setErrorActividades("Error al generar PDF: " + e.getMessage());
        }
    }
}