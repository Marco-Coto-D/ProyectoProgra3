package reservas.data.xml;

import reservas.logic.Funcionario;
import reservas.data.interfaces.FuncionarioRepositorio;

import java.util.List;
import java.util.Optional;

public class FuncionarioRepositorioXml implements FuncionarioRepositorio {

    @Override
    public List<Funcionario> listarTodos() {
        SistemaXml sistema = XmlUtil.cargar();
        return sistema.getFuncionarios();
    }

    @Override
    public Optional<Funcionario> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Funcionario> buscarPorNombre(String nombre) {
        String filtro = nombre == null ? "" : nombre.toLowerCase();
        return listarTodos().stream()
                .filter(f -> f.getNombre().toLowerCase().contains(filtro))
                .toList();
    }

    @Override
    public void guardar(Funcionario funcionario) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getFuncionarios().removeIf(f -> f.getId().equals(funcionario.getId()));
        sistema.getFuncionarios().add(funcionario);
        XmlUtil.guardar(sistema);
    }

    @Override
    public void borrar(String id) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getFuncionarios().removeIf(f -> f.getId().equals(id));
        XmlUtil.guardar(sistema);
    }
}