package persistencia;

import modelo.Recurso;
import java.util.List;
import java.util.Optional;

public interface RecursoRepositorio {
    List<Recurso> listarTodos();
    List<Recurso> buscarPorCategoria(String categoriaID);
    Optional<Recurso> buscarPorId(String id);
    void guardar( Recurso recurso);
    void borrar(String id);
}
