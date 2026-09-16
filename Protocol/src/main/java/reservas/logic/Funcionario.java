package reservas.logic;

import java.io.Serializable;

public class Funcionario extends Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String telefono;

    public Funcionario() {

    }


    public Funcionario(String id, String clave, String nombre, String telefono){
        super(id, clave, Rol.FUNCIONARIO);
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
