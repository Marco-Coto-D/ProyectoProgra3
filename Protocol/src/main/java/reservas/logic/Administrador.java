package reservas.logic;

import java.io.Serializable;

public class Administrador extends Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    public Administrador() {

    }
    public Administrador(String id, String clave){
        super(id, clave, Rol.ADMINISTRADOR);
    }
}
