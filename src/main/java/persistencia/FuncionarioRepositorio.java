package persistencia;

import modelo.Funcionario;
import java.util.List;
import java.util.Optional;

public interface FuncionarioRepositorio {
    List<Funcionario> listarTodos();
    Optional<Funcionario> buscarPorId(String id);
    List<Funcionario> buscarPorNombre(String nombre);
    void guardar(Funcionario funcionario);
    void borrar(String id);
}
