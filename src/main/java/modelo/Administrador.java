package modelo;

public class Administrador extends Usuario {

    public Administrador(String id, String clave){
        super(id, clave, Rol.ADMINISTRADOR);
    }
}
