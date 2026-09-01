package reservas.data.xml;

import reservas.logic.Administrador;
import reservas.data.interfaces.AdministradorRepositorio;

import java.util.List;
import java.util.Optional;

public class AdministradorRepositorioXml implements AdministradorRepositorio {

    @Override
    public List<Administrador> listarTodos() {
        SistemaXml sistema = XmlUtil.cargar();
        return sistema.getAdministradores();
    }

    @Override
    public Optional<Administrador> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    @Override
    public void guardar(Administrador administrador) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getAdministradores().removeIf(a -> a.getId().equals(administrador.getId()));
        sistema.getAdministradores().add(administrador);
        XmlUtil.guardar(sistema);
    }

    @Override
    public void borrar(String id) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getAdministradores().removeIf(a -> a.getId().equals(id));
        XmlUtil.guardar(sistema);
    }
}