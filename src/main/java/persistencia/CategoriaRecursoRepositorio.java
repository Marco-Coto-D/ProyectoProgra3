package persistencia;


import modelo.CategoriaRecurso;
import java.util.List;
import java.util.Optional;

public interface CategoriaRecursoRepositorio {
    List<CategoriaRecurso> listarTodos();
    Optional<CategoriaRecurso> buscarPorId(String id);
    List<CategoriaRecurso> buscarPorDescripcion (String descripcion);
    void guardar(CategoriaRecurso categoria);
    void borrar (String id);

}
