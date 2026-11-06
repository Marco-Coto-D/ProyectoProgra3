package reservas.presentation.estadistica;

import reservas.presentation.AbstractModel;

import java.util.Map;

public class EstadisticaModel extends AbstractModel {

    public static final String ERROR_RECURSOS = "errorRecursos";
    public static final String ERROR_ACTIVIDADES = "errorActividades";
    public static final String RECURSOS = "recursos";
    public static final String ACTIVIDADES = "actividades";

    private String errorRecursos = "";
    private String errorActividades = "";

    // lo último que se cargó en cada sección: null = todavía no se cargó
    private Map<String, Long> conteoRecursos;
    private Map<String, Long> conteoActividades;

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

    public Map<String, Long> getConteoRecursos() {
        return conteoRecursos;
    }

    public void setConteoRecursos(Map<String, Long> conteo) {
        this.conteoRecursos = conteo;
        firePropertyChange(RECURSOS);
    }

    public Map<String, Long> getConteoActividades() {
        return conteoActividades;
    }

    public void setConteoActividades(Map<String, Long> conteo) {
        this.conteoActividades = conteo;
        firePropertyChange(ACTIVIDADES);
    }

    /** la carga falló: se borra lo anterior para que la tabla, el gráfico y el PDF no muestren datos viejos */
    public void errorAlCargarRecursos(String error) {
        setConteoRecursos(null);
        setErrorRecursos(error);
    }

    public void errorAlCargarActividades(String error) {
        setConteoActividades(null);
        setErrorActividades(error);
    }
}
