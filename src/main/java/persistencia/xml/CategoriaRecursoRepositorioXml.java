package persistencia.xml;

import modelo.CategoriaRecurso;
import persistencia.CategoriaRecursoRepositorio;
import java.util.List;
import java.util.Optional;

public class CategoriaRecursoRepositorioXml implements CategoriaRecursoRepositorio {

    @Override
    public List<CategoriaRecurso> listarTodos() {
        SistemaXml sistema = XmlUtil.cargar();
        return sistema.getCategorias();
    }

    @Override
    public Optional<CategoriaRecurso> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        String filtro = descripcion == null ? "" : descripcion.toLowerCase();
        return listarTodos().stream()
                .filter(c -> c.getDescripcion().toLowerCase().contains(filtro))
                .toList();
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getCategorias().removeIf(c -> c.getId().equals(categoria.getId()));
        sistema.getCategorias().add(categoria);
        XmlUtil.guardar(sistema);
    }

    @Override
    public void borrar(String id) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getCategorias().removeIf(c -> c.getId().equals(id));
        XmlUtil.guardar(sistema);
    }
}