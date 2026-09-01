package reservas.data.xml;

import reservas.logic.Recurso;
import reservas.data.interfaces.RecursoRepositorio;

import java.util.List;
import java.util.Optional;

public class RecursoRepositorioXml implements RecursoRepositorio {

    @Override
    public List<Recurso> listarTodos() {
        SistemaXml sistema = XmlUtil.cargar();
        return sistema.getRecursos();
    }

    @Override
    public List<Recurso> buscarPorCategoria(String categoriaId) {
        return listarTodos().stream()
                .filter(r -> r.getCategoria().getId().equals(categoriaId))
                .toList();
    }

    @Override
    public Optional<Recurso> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public void guardar(Recurso recurso) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getRecursos().removeIf(r -> r.getId().equals(recurso.getId()));
        sistema.getRecursos().add(recurso);
        XmlUtil.guardar(sistema);
    }

    @Override
    public void borrar(String id) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getRecursos().removeIf(r -> r.getId().equals(id));
        XmlUtil.guardar(sistema);
    }
}