package reservas.presentation.recurso;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.presentation.AbstractModel;

import java.util.List;

public class RecursoModel extends AbstractModel {

    public static final String ERROR = "error";

    private final ObservableList<Recurso> recursos = FXCollections.observableArrayList();
    private final ObservableList<CategoriaRecurso> categorias = FXCollections.observableArrayList();
    private String error = "";

    public ObservableList<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> lista) {
        recursos.setAll(lista);
    }

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
}