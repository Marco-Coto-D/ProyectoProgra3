package modelo;


import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import persistencia.xml.LocalDateAdapter;
import persistencia.xml.LocalTimeAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Reserva {

    @XmlID
    @XmlAttribute
    private String id;

    private String actividad;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fecha;

    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaInicio;

    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaFin;

    @XmlIDREF
    private Funcionario funcionario;

    @XmlIDREF
    private List<Recurso> recursos;

    private EstadoReserva estado;

    public Reserva() {

    }
    public Reserva (String id, String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Funcionario funcionario){
        this.id = id;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.funcionario = funcionario;
        this.recursos = new ArrayList<>();
        this.estado = EstadoReserva.ACTIVADA;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
}
