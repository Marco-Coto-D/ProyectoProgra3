package reservas.presentation.actividad;

import reservas.logic.EstadoReserva;
import reservas.logic.Reserva;
import reservas.proxy.BackendProxy;
import reservas.util.FechaConverter;
import reservas.util.PdfUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ActividadController {
    private final ActividadView view;
    private final ActividadModel model;
    private final BackendProxy proxy;

    public ActividadController(ActividadView view, ActividadModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;

        view.setController(this);
        view.setModel(model);
    }

    public void cargarSemana(LocalDate fecha) {
        if (fecha == null) {
            model.setError("");
            model.limpiarSemana();
            return;
        }

        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);

        List<Reserva> reservasSemana;
        try {
            reservasSemana = proxy.listarReservasEntre(lunes, lunes.plusDays(6)).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.ACTIVADA)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            model.setError(e.getMessage());
            return;
        }

        model.setError("");
        model.setSemana(lunes, reservasSemana);
    }

    public void fechaInvalida() {
        model.limpiarSemana();
        model.setError(FechaConverter.MENSAJE_INVALIDA);
    }

    public void print() {
        LocalDate lunes = model.getLunes();
        if (lunes == null) {
            model.setError("Seleccioná una fecha antes de generar el PDF");
            return;
        }
        String[] diasNombre = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        List<String> encabezados = new ArrayList<>();
        encabezados.add("Hora");
        for (int d = 0; d < 7; d++) {
            encabezados.add(diasNombre[d] + " " + lunes.plusDays(d));
        }
        List<List<String>> filas = new ArrayList<>();
        for (int h = 6; h <= 22; h++) {
            LocalTime hora = LocalTime.of(h, 0);
            List<String> fila = new ArrayList<>();
            fila.add(hora.toString());
            for (int d = 0; d < 7; d++) {
                List<Reserva> enCelda = model.reservasEn(lunes.plusDays(d), hora);
                fila.add(enCelda.stream()
                        .map(r -> r.getActividad() + " (" + r.getFuncionario().getNombre() + ")")
                        .collect(Collectors.joining(" | ")));
            }
            filas.add(fila);
        }
        try {
            PdfUtil.generar("Programación semana " + lunes, encabezados, filas);
            model.setError("");
        } catch (Exception e) {
            model.setError("Error al generar PDF: " + e.getMessage());
        }
    }

}