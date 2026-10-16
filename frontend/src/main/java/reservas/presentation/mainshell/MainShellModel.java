package reservas.presentation.mainshell;

import reservas.logic.Usuario;
import reservas.presentation.AbstractModel;

public class MainShellModel extends AbstractModel {

    public static final String USUARIO = "usuario";

    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        firePropertyChange(USUARIO);
    }
}