package reservas.presentation.estadistica;

import reservas.presentation.AbstractModel;

public class EstadisticaModel extends AbstractModel {

    public static final String ERROR_RECURSOS = "errorRecursos";
    public static final String ERROR_ACTIVIDADES = "errorActividades";

    private String errorRecursos = "";
    private String errorActividades = "";

    public String getErrorRecursos() {
        return errorRecursos;
    }

    public void setErrorRecursos(String error) {
        this.errorRecursos = error;
        firePropertyChange(ERROR_RECURSOS);
    }

    public String getErrorActividades() {
        return errorActividades;
    }

    public void setErrorActividades(String error) {
        this.errorActividades = error;
        firePropertyChange(ERROR_ACTIVIDADES);
    }
}