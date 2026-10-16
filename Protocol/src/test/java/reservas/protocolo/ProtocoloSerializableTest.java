package reservas.protocolo;

import org.junit.jupiter.api.Test;
import reservas.logic.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProtocoloSerializableTest {
    @SuppressWarnings("unchecked")
    private static <T> T idaYVuelta(T objeto) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(objeto);
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) entrada.readObject();
        }
    }

    private static Reserva reservaCompleta() {
        CategoriaRecurso categoria = new CategoriaRecurso("CAT-001", "Proyectores");
        Reserva reserva = new Reserva("RES-1", "Charla de bienvenida", LocalDate.of(2030, 6, 3),
                LocalTime.of(9, 0), LocalTime.of(10, 30), new Funcionario("F-1", "clave", "Laura Jiménez", "8888-0000"));
        reserva.setRecursos(new ArrayList<>(List.of(
                new Recurso("R1", "Epson aula 5", categoria),
                new Recurso("R2", "Epson aula 6", categoria))));
        return reserva;
    }

    @Test
    void peticionConArgumentosDeVariosTipos() throws Exception {
        Peticion original = new Peticion(Operacion.LISTAR_RESERVAS_ENTRE, LocalDate.of(2030, 1, 7), LocalDate.of(2030, 1, 13));

        Peticion copia = idaYVuelta(original);

        assertEquals(Operacion.LISTAR_RESERVAS_ENTRE, copia.getOperacion());
        assertEquals(LocalDate.of(2030, 1, 7), copia.getArgumento(0));
        assertEquals(LocalDate.of(2030, 1, 13), copia.getArgumento(1));
    }

    @Test
    void respuestaOkConListaDeEntidades() throws Exception {
        Respuesta original = Respuesta.ok(new ArrayList<>(List.of(new CategoriaRecurso("CAT-001", "Proyectores"))));

        Respuesta copia = idaYVuelta(original);

        assertTrue(copia.isOk());
        List<?> lista = (List<?>) copia.getResultado();
        assertEquals("Proyectores", ((CategoriaRecurso) lista.get(0)).getDescripcion());
    }

    @Test
    void respuestaDeErrorConservaElMensaje() throws Exception {
        Respuesta copia = idaYVuelta(Respuesta.error("Ya existe un registro con el id R1"));

        assertFalse(copia.isOk());
        assertEquals("Ya existe un registro con el id R1", copia.getError());
        assertNull(copia.getResultado());
    }

    @Test
    void notificacionConReservaCompleta() throws Exception {
        Notificacion copia = idaYVuelta(new Notificacion(TipoNotificacion.NUEVA_RESERVA, reservaCompleta()));

        Reserva reserva = copia.getReserva();
        assertEquals(TipoNotificacion.NUEVA_RESERVA, copia.getTipo());
        assertEquals("Charla de bienvenida", reserva.getActividad());
        assertEquals(LocalDate.of(2030, 6, 3), reserva.getFecha());
        assertEquals(LocalTime.of(10, 30), reserva.getHoraFin());
        assertEquals(EstadoReserva.ACTIVADA, reserva.getEstado());
        assertEquals("Laura Jiménez", reserva.getFuncionario().getNombre());
        assertEquals(List.of("R1", "R2"), reserva.getRecursos().stream().map(Recurso::getId).toList());
        assertEquals("Proyectores", reserva.getRecursos().get(0).getCategoria().getDescripcion());
    }

    @Test
    void resultadoAutenticacionConAdministrador() throws Exception {
        ResultadoAutenticacion copia = idaYVuelta(new ResultadoAutenticacion(new Administrador("999", "999"), "sesion-123"));

        assertInstanceOf(Administrador.class, copia.usuario());
        assertEquals(Rol.ADMINISTRADOR, copia.usuario().getRol());
        assertEquals("sesion-123", copia.sesionId());
    }

    @Test
    void datosReservaIAConCamposNulos() throws Exception {
        DatosReservaIA original = new DatosReservaIA("Charla", LocalDate.of(2030, 6, 3), null, null,
                new ArrayList<>(List.of(new CategoriaRecurso("CAT-001", "Proyectores"))));

        DatosReservaIA copia = idaYVuelta(original);

        assertEquals("Charla", copia.actividad());
        assertEquals(LocalDate.of(2030, 6, 3), copia.fecha());
        assertNull(copia.horaInicio());
        assertNull(copia.horaFin());
        assertEquals("CAT-001", copia.categorias().get(0).getId());
    }

    @Test
    void resultadoAsignacion() throws Exception {
        CategoriaRecurso categoria = new CategoriaRecurso("CAT-001", "Proyectores");
        ResultadoAsignacion original = new ResultadoAsignacion(
                new ArrayList<>(List.of(new Recurso("R1", "Epson aula 5", categoria))),
                new ArrayList<>(List.of("Laboratorios")));

        ResultadoAsignacion copia = idaYVuelta(original);

        assertEquals("R1", copia.asignados().get(0).getId());
        assertEquals(List.of("Laboratorios"), copia.categoriasSinDisponibilidad());
    }
}
