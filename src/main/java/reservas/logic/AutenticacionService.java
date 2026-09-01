package reservas.logic;

import reservas.data.interfaces.AdministradorRepositorio;
import reservas.data.interfaces.FuncionarioRepositorio;

import java.util.Optional;

public class AutenticacionService {

    private final FuncionarioRepositorio funcionarioRepositorio;
    private final AdministradorRepositorio administradorRepositorio;

    public AutenticacionService(FuncionarioRepositorio funcionarioRepositorio,
                                AdministradorRepositorio administradorRepositorio) {
        this.funcionarioRepositorio = funcionarioRepositorio;
        this.administradorRepositorio = administradorRepositorio;
    }

    public Usuario autenticar(String id, String clave) {
        Optional<Funcionario> funcionario = funcionarioRepositorio.buscarPorId(id);
        if (funcionario.isPresent() && funcionario.get().getClave().equals(clave)) {
            return funcionario.get();
        }

        Optional<Administrador> administrador = administradorRepositorio.buscarPorId(id);
        if (administrador.isPresent() && administrador.get().getClave().equals(clave)) {
            return administrador.get();
        }

        return null;
    }

    public void cambiarClave(Usuario usuario, String claveNueva) {
        usuario.setClave(claveNueva);
        if (usuario instanceof Funcionario f) {
            funcionarioRepositorio.guardar(f);
        } else if (usuario instanceof Administrador a) {
            administradorRepositorio.guardar(a);
        }
    }
}