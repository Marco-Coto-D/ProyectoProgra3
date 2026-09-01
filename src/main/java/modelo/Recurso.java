package modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
public class Recurso {

    @XmlID
    @XmlAttribute
    private String id;

    private String descripcion;

    @XmlIDREF
    private CategoriaRecurso categoria;

    public Recurso() {

    }
    public Recurso(String id, String descripcion, CategoriaRecurso categoria){
        this.id = id;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public CategoriaRecurso getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaRecurso categoria) {
        this.categoria = categoria;
    }
}
