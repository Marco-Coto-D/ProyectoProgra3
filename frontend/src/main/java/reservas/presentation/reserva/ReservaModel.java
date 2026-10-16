package reservas.presentation.reserva;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

import java.util.List;

public class ReservaModel extends AbstractModel {

    public static final String ERROR = "error";

    private final ObservableList<Reserva> misReservas = FXCollections.observableArrayList();
    private final ObservableList<CategoriaRecurso> categorias = FXCollections.observableArrayList();
    private String error = "";

    public ObservableList<Reserva> getMisReservas() {
        return misReservas;
    }

    public ObservableList<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setMisReservas(List<Reserva> lista) {
        misReservas.setAll(lista);
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
}