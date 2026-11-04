package reservas.presentation.actividad;

import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class ActividadModel extends AbstractModel {

    public static final String ERROR = "error";
    public static final String SEMANA = "semana";

    private String error = "";

    // la semana cargada: lunes null = no hay nada que mostrar
    private LocalDate lunes;
    private List<Reserva> reservas = List.of();

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        firePropertyChange(ERROR);
    }

    public LocalDate getLunes() {
        return lunes;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setSemana(LocalDate lunes, List<Reserva> reservas) {
        this.lunes = lunes;
        this.reservas = reservas;
        firePropertyChange(SEMANA);
    }

    public void limpiarSemana() {
        setSemana(null, List.of());
    }

    /** las reservas que están en curso ese día a esa hora */
    public List<Reserva> reservasEn(LocalDate fecha, LocalTime hora) {
        return reservas.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .filter(r -> !hora.isBefore(r.getHoraInicio()) && hora.isBefore(r.getHoraFin()))
                .collect(Collectors.toList());
    }
}
