package reservas.presentation.estadistica;

import reservas.data.interfaces.ReservaRepositorio;
import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.util.PdfUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class EstadisticaController {

    private final EstadisticaView view;
    private final EstadisticaModel model;
    private final ReservaRepositorio reservaRepositorio;

    private Map<String, Long> ultimoConteoRecursos;
    private Map<String, Long> ultimoConteoActividades;

    public EstadisticaController(EstadisticaView view, EstadisticaModel model,
                                 ReservaRepositorio reservaRepositorio) {
        this.view = view;
        this.model = model;
        this.reservaRepositorio = reservaRepositorio;

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

        Map<String, Long> conteo = reservaRepositorio.listarTodas().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .flatMap(r -> r.getRecursos().stream())
                .collect(Collectors.groupingBy(
                        rec -> rec.getCategoria().getDescripcion(),
                        TreeMap::new,
                        Collectors.counting()
                ));

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

        Map<String, Long> conteo = reservaRepositorio.listarTodas().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .collect(Collectors.groupingBy(
                        r -> r.getFecha().with(DayOfWeek.MONDAY).toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));

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