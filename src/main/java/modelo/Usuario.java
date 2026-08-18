package modelo;

import java.util.Objects;

public class Usuario {
    protected String id;
    protected String clave;
    protected Rol rol;

    protected Usuario(String id, String clave, Rol rol){
        this.id = id;
        this.clave = id;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (!(o instanceof Usuario)){
            return false;
        }
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }
}
