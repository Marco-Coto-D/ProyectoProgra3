package reservas.presentation.reserva;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

import java.util.List;

public class ReservaModel extends AbstractModel {

    public static final String ERROR = "error";
    public static final String EXITO = "exito";

    private final ObservableList<Reserva> misReservas = FXCollections.observableArrayList();
    private final ObservableList<CategoriaRecurso> categorias = FXCollections.observableArrayList();
    private String error = "";
    private String exito = "";

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

    public String getExito() {
        return exito;
    }

    // va en el mismo lugar que los errores, pero en verde
    public void setExito(String exito) {
        this.exito = exito;
        firePropertyChange(EXITO);
    }
}