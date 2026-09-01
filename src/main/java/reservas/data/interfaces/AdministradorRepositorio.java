package reservas.data.interfaces;

import reservas.logic.Administrador;
import java.util.List;
import java.util.Optional;

public interface AdministradorRepositorio {
    List<Administrador> listarTodos();
    Optional<Administrador> buscarPorId(String id);
    void guardar(Administrador administrador);
    void borrar(String id);
}