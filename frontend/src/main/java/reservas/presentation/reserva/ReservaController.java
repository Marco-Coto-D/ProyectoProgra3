package reservas.presentation.reserva;

import javafx.application.Platform;
import reservas.logic.*;
import reservas.proxy.BackendProxy;
import reservas.util.PdfUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservaController {
    private final ReservaView view;
    private final ReservaModel model;
    private final BackendProxy proxy;

    public ReservaController(ReservaView view, ReservaModel model, BackendProxy proxy) {
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
            model.setMisReservas(proxy.listarMisReservas());
        } catch (Exception e) {
            model.setError(e.getMessage());
        }
    }

    public void refrescar() {
        cargarDatos();
    }

    public void crear(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                      List<CategoriaRecurso> categoriasSeleccionadas) {
        if (actividad == null || actividad.isBlank() || fecha == null
                || horaInicio == null || horaFin == null
                || categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            model.setError("Completá actividad, fecha, horario y al menos una categoría");
            return;
        }
        try {
            proxy.crearReserva(actividad, fecha, horaInicio, horaFin, categoriasSeleccionadas);
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        cargarDatos();
        view.limpiarFormulario();
    }

    public void print() {
        if (model.getMisReservas().isEmpty()) {
            model.setError("No hay reservas para exportar");
            return;
        }
        List<String> encabezados = List.of("Actividad", "Fecha", "Horario", "Recurso", "Estado");
        List<List<String>> filas = new ArrayList<>();
        for (Reserva r : model.getMisReservas()) {
            String recursos = r.getRecursos().stream()
                    .map(Recurso::getDescripcion)
                    .collect(Collectors.joining(", "));
            filas.add(List.of(
                    r.getActividad(),
                    r.getFecha().toString(),
                    r.getHoraInicio() + " - " + r.getHoraFin(),
                    recursos,
                    r.getEstado().toString()
            ));
        }
        try {
            PdfUtil.generar("Mis Reservas", encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

    public void extraerConIA(String frase) {
        if (frase == null || frase.isBlank()) {
            model.setError("Escribí una descripción antes de extraer");
            return;
        }
        view.setBtnExtraerDeshabilitado(true);
        model.setError("Consultando IA...");

        new Thread(() -> {
            try {
                DatosReservaIA datos = proxy.extraerIA(frase);
                Platform.runLater(() -> {
                    try {
                        view.llenarDesdeIA(datos.actividad(), datos.fecha(),
                                datos.horaInicio(), datos.horaFin(), datos.categorias());
                        boolean incompleto = datos.fecha() == null || datos.horaInicio() == null || datos.horaFin() == null;
                        model.setError(incompleto ? "Revisá y completá los campos que quedaron vacíos" : "");
                    } finally {
                        view.setBtnExtraerDeshabilitado(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    model.setError("Error IA: " + e.getMessage());
                    view.setBtnExtraerDeshabilitado(false);
                });
            }
        }).start();
    }

    public void cancelar(Reserva seleccionada) {
        if (seleccionada == null) {
            model.setError("Seleccioná una reserva de la tabla");
            return;
        }
        try {
            proxy.cancelarReserva(seleccionada.getId());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }
        model.setError("");
        cargarDatos();
    }
}