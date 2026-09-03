package reservas.data.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;
import reservas.logic.EstadoReserva;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReservaRepositorioXmlIT extends XmlITBase {

    private final ReservaRepositorioXml repo = new ReservaRepositorioXml();
    private final FuncionarioRepositorioXml repoFunc = new FuncionarioRepositorioXml();
    private final CategoriaRecursoRepositorioXml repoCat = new CategoriaRecursoRepositorioXml();
    private final RecursoRepositorioXml repoRecurso = new RecursoRepositorioXml();

    private Funcionario funcionario;
    private Recurso recurso;

    @BeforeEach
    void prepararDependencias() {
        // Reserva.funcionario usa @XmlIDREF → el funcionario debe existir en el XML
        funcionario = new Funcionario("f_test", "clave", "Test User", "0000-0000");
        repoFunc.guardar(funcionario);

        CategoriaRecurso cat = new CategoriaRecurso("cat_test", "Salas Test");
        repoCat.guardar(cat);

        recurso = new Recurso("rec_test", "Sala Test", cat);
        repoRecurso.guardar(recurso);
    }

    private Reserva nuevaReserva(String id) {
        return new Reserva(
                id, "Reunión de prueba",
                LocalDate.of(2025, 6, 15),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                funcionario
        );
    }

    @Test
    void guardarYListar() {
        repo.guardar(nuevaReserva("res001"));

        List<Reserva> lista = repo.listarTodas();
        assertEquals(1, lista.size());
        assertEquals("res001", lista.get(0).getId());
        assertEquals("Reunión de prueba", lista.get(0).getActividad());
    }

    @Test
    void buscarPorId() {
        repo.guardar(nuevaReserva("res002"));

        Optional<Reserva> resultado = repo.buscarPorId("res002");
        assertTrue(resultado.isPresent());
        assertEquals(LocalDate.of(2025, 6, 15), resultado.get().getFecha());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(repo.buscarPorId("noexiste").isEmpty());
    }

    @Test
    void guardarActualizaExistente() {
        repo.guardar(nuevaReserva("res003"));

        Reserva actualizada = nuevaReserva("res003");
        actualizada.setActividad("Reunión actualizada");
        repo.guardar(actualizada);

        List<Reserva> lista = repo.listarTodas();
        assertEquals(1, lista.size());
        assertEquals("Reunión actualizada", lista.get(0).getActividad());
    }

    @Test
    void estadoInicialActivada() {
        repo.guardar(nuevaReserva("res004"));

        Reserva cargada = repo.buscarPorId("res004").get();
        assertEquals(EstadoReserva.ACTIVADA, cargada.getEstado());
    }

    @Test
    void actualizarEstado() {
        repo.guardar(nuevaReserva("res005"));
        repo.actualizarEstado("res005", EstadoReserva.CANCELADA);

        Reserva cargada = repo.buscarPorId("res005").get();
        assertEquals(EstadoReserva.CANCELADA, cargada.getEstado());
    }

    @Test
    void listarPorFuncionario() {
        repo.guardar(nuevaReserva("res006"));
        repo.guardar(nuevaReserva("res007"));

        List<Reserva> porFunc = repo.listarPorFuncionario("f_test");
        assertEquals(2, porFunc.size());
        assertTrue(porFunc.stream().allMatch(r -> r.getFuncionario().getId().equals("f_test")));
    }

    @Test
    void funcionarioResueltaAlCargar() {
        repo.guardar(nuevaReserva("res008"));

        Reserva cargada = repo.buscarPorId("res008").get();
        assertNotNull(cargada.getFuncionario());
        assertEquals("f_test", cargada.getFuncionario().getId());
        assertEquals("Test User", cargada.getFuncionario().getNombre());
    }
}
