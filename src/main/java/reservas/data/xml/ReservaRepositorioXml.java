package reservas.data.xml;

import reservas.logic.EstadoReserva;
import reservas.logic.Reserva;
import reservas.data.interfaces.ReservaRepositorio;

import java.util.List;
import java.util.Optional;

public class ReservaRepositorioXml implements ReservaRepositorio {

    @Override
    public List<Reserva> listarTodas() {
        SistemaXml sistema = XmlUtil.cargar();
        return sistema.getReservas();
    }

    @Override
    public List<Reserva> listarPorFuncionario(String funcionarioId) {
        return listarTodas().stream()
                .filter(r -> r.getFuncionario().getId().equals(funcionarioId))
                .toList();
    }

    @Override
    public Optional<Reserva> buscarPorId(String id) {
        return listarTodas().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public void guardar(Reserva reserva) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getReservas().removeIf(r -> r.getId().equals(reserva.getId()));
        sistema.getReservas().add(reserva);
        XmlUtil.guardar(sistema);
    }

    @Override
    public void actualizarEstado(String id, EstadoReserva estado) {
        SistemaXml sistema = XmlUtil.cargar();
        sistema.getReservas().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .ifPresent(r -> r.setEstado(estado));
        XmlUtil.guardar(sistema);
    }
}