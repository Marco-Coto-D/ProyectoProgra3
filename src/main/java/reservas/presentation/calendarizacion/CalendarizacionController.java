package reservas.presentation.calendarizacion;

import reservas.data.interfaces.CategoriaRecursoRepositorio;
import reservas.data.interfaces.RecursoRepositorio;
import reservas.data.interfaces.ReservaRepositorio;
import reservas.logic.CategoriaRecurso;
import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.util.PdfUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarizacionController {

    private final CalendarizacionView view;
    private final CalendarizacionModel model;
    private final ReservaRepositorio reservaRepositorio;
    private final RecursoRepositorio recursoRepositorio;
    private final CategoriaRecursoRepositorio categoriaRepositorio;

    private LocalDate ultimaFecha;
    private List<Recurso> ultimosRecursos = new ArrayList<>();
    private List<Reserva> ultimasReservas = new ArrayList<>();

    public CalendarizacionController(CalendarizacionView view, CalendarizacionModel model, ReservaRepositorio reservaRepositorio, RecursoRepositorio recursoRepositorio, CategoriaRecursoRepositorio categoriaRepositorio) {
        this.view = view;
        this.model = model;
        this.reservaRepositorio = reservaRepositorio;
        this.recursoRepositorio = recursoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        model.setCategorias(categoriaRepositorio.listarTodos());
    }

    public void cargarCalendario(LocalDate fecha, CategoriaRecurso categoria) {
        if (fecha == null || categoria == null) {
            model.setError("");
            view.limpiarGrid();
            return;
        }

        List<Recurso> recursos = recursoRepositorio.buscarPorCategoria(categoria.getId());

        List<Reserva> reservasDelDia = reservaRepositorio.listarTodas().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                .filter(r -> r.getFecha().equals(fecha))
                .collect(Collectors.toList());

        ultimaFecha = fecha;
        ultimosRecursos = recursos;
        ultimasReservas = reservasDelDia;
        model.setError("");
        view.dibujarGrid(recursos, reservasDelDia);
    }

    public void print() {
        if (ultimaFecha == null || ultimosRecursos.isEmpty()) {
            model.setError("Seleccioná fecha y categoría antes de generar el PDF");
            return;
        }
        List<String> encabezados = new ArrayList<>();
        encabezados.add("Hora");
        for (Recurso r : ultimosRecursos) encabezados.add(r.getDescripcion());

        List<List<String>> filas = new ArrayList<>();
        for (int h = 6; h <= 22; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            List<String> fila = new ArrayList<>();
            fila.add(hora.toString());
            for (Recurso r : ultimosRecursos) {
                Reserva res = encontrarReserva(ultimasReservas, r, hora);
                fila.add(res != null ? res.getActividad() + " - " + res.getFuncionario().getNombre() : "");
            }
            filas.add(fila);
        }
        try {
            PdfUtil.generar("Calendarización " + ultimaFecha, encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

    private Reserva encontrarReserva(List<Reserva> reservas, Recurso recurso, LocalTime hora) {
        for (Reserva r : reservas) {
            if (hora.isBefore(r.getHoraInicio()) || !hora.isBefore(r.getHoraFin())) continue;
            for (Recurso rec : r.getRecursos()) {
                if (rec.getId().equals(recurso.getId())) return r;
            }
        }
        return null;
    }
}