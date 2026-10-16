package reservas.logic;

import reservas.data.mysql.AdministradorDao;
import reservas.data.mysql.FuncionarioDao;

public class AutenticacionService {

    private FuncionarioDao funcionarioDao;
    private AdministradorDao administradorDao;

    public AutenticacionService() {
        funcionarioDao = new FuncionarioDao();
        administradorDao = new AdministradorDao();
    }

    public Usuario autenticar(String id, String clave) {
        Funcionario funcionario = funcionarioDao.read(id);
        if (funcionario != null && funcionario.getClave().equals(clave)) {
            return funcionario;
        }

        Administrador administrador = administradorDao.read(id);
        if (administrador != null && administrador.getClave().equals(clave)) {
            return administrador;
        }

        return null;
    }

    public void cambiarClave(Usuario usuario, String claveNueva) {
        usuario.setClave(claveNueva);
        if (usuario instanceof Funcionario f) {
            funcionarioDao.update(f);
        } else if (usuario instanceof Administrador a) {
            administradorDao.update(a);
        }
    }
}
