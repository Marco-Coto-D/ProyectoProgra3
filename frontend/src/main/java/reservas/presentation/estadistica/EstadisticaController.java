package reservas.presentation.estadistica;

import reservas.proxy.BackendProxy;
import reservas.util.FechaConverter;
import reservas.util.PdfUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EstadisticaController {
    static final String SIN_RECURSOS = "No hay recursos reservados en ese período";

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
            model.errorAlCargarRecursos("Seleccioná ambas fechas");
            return;
        }
        // el período al revés lo rechaza el Backend
        Map<String, Long> conteo;
        try {
            conteo = proxy.contarRecursosPorCategoria(desde, hasta);
        } catch (Exception e) {
            model.errorAlCargarRecursos(e.getMessage());
            return;
        }

        model.setErrorRecursos("");
        model.setConteoRecursos(conteo);
        if (conteo.isEmpty()) {
            model.setErrorRecursos(SIN_RECURSOS);
        }
    }

    public void fechaInvalidaRecursos() {
        model.errorAlCargarRecursos(FechaConverter.MENSAJE_INVALIDA);
    }

    public void fechaInvalidaActividades() {
        model.errorAlCargarActividades(FechaConverter.MENSAJE_INVALIDA);
    }

    public void printRecursos() {
        Map<String, Long> conteo = model.getConteoRecursos();
        if (conteo == null) {
            model.setErrorRecursos("Cargá los datos antes de generar el PDF");
            return;
        }
        if (conteo.isEmpty()) {
            model.setErrorRecursos(SIN_RECURSOS);
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
            model.errorAlCargarActividades("Seleccioná ambas fechas");
            return;
        }
        Map<String, Long> conteo;
        try {
            conteo = proxy.contarActividadesPorSemana(desde, hasta);
        } catch (Exception e) {
            model.errorAlCargarActividades(e.getMessage());
            return;
        }

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