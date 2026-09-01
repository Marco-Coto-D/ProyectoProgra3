package modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Administrador extends Usuario {

    public Administrador() {

    }
    public Administrador(String id, String clave){
        super(id, clave, Rol.ADMINISTRADOR);
    }
}
