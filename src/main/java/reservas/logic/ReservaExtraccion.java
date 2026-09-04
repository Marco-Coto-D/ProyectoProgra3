package reservas.logic;

import java.util.List;

public class ReservaExtraccion {

    private String actividad;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private List<String> categoriasRecurso;

    public ReservaExtraccion() {
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public List<String> getCategoriasRecurso() {
        return categoriasRecurso;
    }

    public void setCategoriasRecurso(List<String> categoriasRecurso) {
        this.categoriasRecurso = categoriasRecurso;
    }
}
