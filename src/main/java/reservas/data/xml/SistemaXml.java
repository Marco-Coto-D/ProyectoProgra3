package reservas.data.xml;

import jakarta.xml.bind.annotation.*;
import reservas.logic.Administrador;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "sistema")
@XmlAccessorType(XmlAccessType.FIELD)
public class SistemaXml {

    @XmlElementWrapper(name = "administradores")
    @XmlElement(name = "administrador")
    private List<Administrador> administradores = new ArrayList<>();

    @XmlElementWrapper(name = "funcionarios")
    @XmlElement(name = "funcionario")
    private List<Funcionario> funcionarios = new ArrayList<>();

    @XmlElementWrapper(name = "categorias")
    @XmlElement(name = "categoria")
    private List<CategoriaRecurso> categorias = new ArrayList<>();

    @XmlElementWrapper(name = "recursos")
    @XmlElement(name = "recurso")
    private List<Recurso> recursos = new ArrayList<>();

    @XmlElementWrapper(name = "reservas")
    @XmlElement(name = "reserva")
    private List<Reserva> reservas = new ArrayList<>();

    public List<Administrador> getAdministradores() { return administradores; }
    public void setAdministradores(List<Administrador> administradores) { this.administradores = administradores; }

    public List<Funcionario> getFuncionarios() { return funcionarios; }
    public void setFuncionarios(List<Funcionario> funcionarios) { this.funcionarios = funcionarios; }

    public List<CategoriaRecurso> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaRecurso> categorias) { this.categorias = categorias; }

    public List<Recurso> getRecursos() { return recursos; }
    public void setRecursos(List<Recurso> recursos) { this.recursos = recursos; }

    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
}