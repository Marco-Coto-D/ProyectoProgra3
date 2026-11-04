package reservas.presentation.calendarizacion;

import reservas.logic.CategoriaRecurso;
import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.proxy.BackendProxy;
import reservas.util.FechaConverter;
import reservas.util.PdfUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarizacionController {
    private final CalendarizacionView view;
    private final CalendarizacionModel model;
    private final BackendProxy proxy;

    public CalendarizacionController(CalendarizacionView view, CalendarizacionModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            model.setCategorias(proxy.listarCategorias());
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void refrescar() {
        cargarDatos();
    }

    public void cargarCalendario(LocalDate fecha, CategoriaRecurso categoria) {
        if (fecha == null || categoria == null) {
            model.setError("");
            model.limpiarCalendario();
            return;
        }

        List<Recurso> recursos;
        List<Reserva> reservasDelDia;
        try {
            recursos = proxy.buscarRecursosPorCategoria(categoria.getId());
            reservasDelDia = proxy.listarReservasEntre(fecha, fecha).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        model.setCalendario(fecha, recursos, reservasDelDia);
    }

    public void fechaInvalida() {
        model.limpiarCalendario();
        model.setError(FechaConverter.MENSAJE_INVALIDA);
    }

    public void print() {
        if (model.getFecha() == null || model.getRecursos().isEmpty()) {
            model.setError("Seleccioná fecha y categoría antes de generar el PDF");
            return;
        }
        List<String> encabezados = new ArrayList<>();
        encabezados.add("Hora");
        for (Recurso r : model.getRecursos()) encabezados.add(r.getDescripcion());

        List<List<String>> filas = new ArrayList<>();
        for (int h = 6; h <= 22; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            List<String> fila = new ArrayList<>();
            fila.add(hora.toString());
            for (Recurso r : model.getRecursos()) {
                Reserva res = model.reservaEn(r, hora);
                fila.add(res != null ? res.getActividad() + " - " + res.getFuncionario().getNombre() : "");
            }
            filas.add(fila);
        }
        try {
            PdfUtil.generar("Calendarización " + model.getFecha(), encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

}