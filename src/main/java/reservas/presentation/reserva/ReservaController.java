package reservas.presentation.reserva;

import javafx.application.Platform;
import reservas.data.interfaces.CategoriaRecursoRepositorio;
import reservas.data.interfaces.RecursoRepositorio;
import reservas.data.interfaces.ReservaRepositorio;
import reservas.logic.*;
import reservas.presentation.Sesion;
import reservas.util.PdfUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservaController {

    private final ReservaView view;
    private final ReservaModel model;
    private final ReservaRepositorio reservaRepositorio;
    private final RecursoRepositorio recursoRepositorio;
    private final CategoriaRecursoRepositorio categoriaRepositorio;
    private final AsignadorRecursos asignadorRecursos = new AsignadorRecursos();

    public ReservaController(ReservaView view, ReservaModel model, ReservaRepositorio reservaRepositorio, RecursoRepositorio recursoRepositorio, CategoriaRecursoRepositorio categoriaRepositorio) {
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
        model.setMisReservas(reservaRepositorio.listarPorFuncionario(funcionarioActual().getId()));
    }

    private Funcionario funcionarioActual() {
        return (Funcionario) Sesion.getUsuario();
    }

    public void crear(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                      List<CategoriaRecurso> categoriasSeleccionadas) {

        if (actividad == null || actividad.isBlank() || fecha == null
                || horaInicio == null || horaFin == null
                || categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            model.setError("Completá actividad, fecha, horario y al menos una categoría");
            return;
        }
        if (!horaFin.isAfter(horaInicio)) {
            model.setError("La hora de fin debe ser posterior a la hora de inicio");
            return;
        }
        if (fecha.isBefore(LocalDate.now())) {
            model.setError("La fecha no puede ser en el pasado");
            return;
        }
        if (fecha.equals(LocalDate.now()) && horaInicio.isBefore(LocalTime.now())) {
            model.setError("No podés reservar un horario de hoy que ya pasó");
            return;
        }


        Set<String> idsOcupados = reservaRepositorio.listarTodas().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                .filter(r -> r.getFecha().equals(fecha))
                .filter(r -> horaInicio.isBefore(r.getHoraFin()) && horaFin.isAfter(r.getHoraInicio()))
                .flatMap(r -> r.getRecursos().stream())
                .map(Recurso::getId)
                .collect(Collectors.toSet());

        ResultadoAsignacion resultado = asignadorRecursos.asignar(
                categoriasSeleccionadas, recursoRepositorio.listarTodos(), idsOcupados);

        if (!resultado.categoriasSinDisponibilidad().isEmpty()) {
            model.setError("Sin disponibilidad para: " + String.join(", ", resultado.categoriasSinDisponibilidad()));
            return;
        }

        Reserva reserva = new Reserva(UUID.randomUUID().toString(), actividad, fecha, horaInicio, horaFin, funcionarioActual());
        reserva.setRecursos(resultado.asignados());
        reservaRepositorio.guardar(reserva);

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

        List<CategoriaRecurso> snapshot = new ArrayList<>(model.getCategorias());
        IAService ia = new IAService();

        new Thread(() -> {
            try {
                DatosReservaIA datos = ia.extraer(frase, snapshot);
                Platform.runLater(() -> {
                    view.llenarDesdeIA(datos.actividad(), datos.fecha(),
                            datos.horaInicio(), datos.horaFin(), datos.categorias());
                    model.setError("");
                    view.setBtnExtraerDeshabilitado(false);
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
        if (seleccionada.getEstado() == EstadoReserva.CANCELADA) {
            model.setError("Esta reserva ya está cancelada");
            return;
        }
        if (seleccionada.getFecha().isBefore(LocalDate.now())) {
            model.setError("No se puede cancelar una reserva pasada");
            return;
        }
        if (seleccionada.getFecha().equals(LocalDate.now()) && !LocalTime.now().isBefore(seleccionada.getHoraFin())) {
            model.setError("Esta reserva ya finalizó, no se puede cancelar");
            return;
        }
        reservaRepositorio.actualizarEstado(seleccionada.getId(), EstadoReserva.CANCELADA);
        model.setError("");
        cargarDatos();
    }
}