package reservas.presentation.calendarizacion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CalendarizacionModel extends AbstractModel {

    public static final String ERROR = "error";
    public static final String CALENDARIO = "calendario";

    private final ObservableList<CategoriaRecurso> categorias = FXCollections.observableArrayList();
    private String error = "";

    // el calendario cargado: fecha null = no hay nada que mostrar
    private LocalDate fecha;
    private List<Recurso> recursos = List.of();
    private List<Reserva> reservas = List.of();

    public ObservableList<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> lista) {
        categorias.setAll(lista);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        firePropertyChange(ERROR);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setCalendario(LocalDate fecha, List<Recurso> recursos, List<Reserva> reservas) {
        this.fecha = fecha;
        this.recursos = recursos;
        this.reservas = reservas;
        firePropertyChange(CALENDARIO);
    }

    public void limpiarCalendario() {
        setCalendario(null, List.of(), List.of());
    }

    /** la reserva que ocupa ese recurso a esa hora, o null si está libre */
    public Reserva reservaEn(Recurso recurso, LocalTime hora) {
        for (Reserva r : reservas) {
            if (hora.isBefore(r.getHoraInicio()) || !hora.isBefore(r.getHoraFin())) continue;
            for (Recurso rec : r.getRecursos()) {
                if (rec.getId().equals(recurso.getId())) return r;
            }
        }
        return null;
    }
}
