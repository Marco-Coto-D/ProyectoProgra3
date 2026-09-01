package persistencia;

import modelo.EstadoReserva;
import modelo.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaRepositorio {
    List<Reserva> listarTodas();
    List<Reserva> listarPorFuncionario(String funcionarioID);
    Optional<Reserva> buscarPorId(String id);
    void guardar(Reserva reserva);
    void actualizarEstado(String id, EstadoReserva estado);
}
