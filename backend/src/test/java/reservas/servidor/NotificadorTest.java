package reservas.servidor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reservas.logic.Funcionario;
import reservas.logic.Reserva;
import reservas.protocolo.Notificacion;
import reservas.protocolo.TipoNotificacion;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificadorTest {
    private final Notificador notificador = Notificador.instance();
    private final List<String> sesiones = new ArrayList<>();

    // Notificador es singleton, cada test saca sus sesiones al final
    @AfterEach
    void quitarSesiones() {
        sesiones.forEach(notificador::quitar);
    }

    private static class Suscriptor {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final ObjectOutputStream salida;

        Suscriptor() throws IOException {
            salida = new ObjectOutputStream(bytes);
            salida.flush();
        }

        List<Notificacion> recibidas() throws Exception {
            List<Notificacion> lista = new ArrayList<>();
            try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                while (true) {
                    lista.add((Notificacion) entrada.readObject());
                }
            } catch (EOFException fin) {
                return lista;
            }
        }
    }

    // acepta el header y despues tira error, como un socket caido
    private static class StreamCaido extends OutputStream {
        boolean caido = false;
        int bytesRecibidos = 0;

        @Override
        public void write(int b) throws IOException {
            if (caido) {
                throw new IOException("conexión cerrada");
            }
            bytesRecibidos++;
        }
    }

    private Suscriptor suscribir(String sesionId) throws IOException {
        Suscriptor suscriptor = new Suscriptor();
        notificador.agregar(sesionId, suscriptor.salida);
        sesiones.add(sesionId);
        return suscriptor;
    }

    private static Notificacion nuevaReserva(String actividad) {
        Reserva reserva = new Reserva("RES-" + actividad, actividad, LocalDate.of(2030, 6, 3),
                LocalTime.of(9, 0), LocalTime.of(10, 0), new Funcionario("F-1", "x", "Ana", "1"));
        return new Notificacion(TipoNotificacion.NUEVA_RESERVA, reserva);
    }

    @Test
    void notificaATodosMenosAlQueOriginoElCambio() throws Exception {
        Suscriptor a = suscribir("test-A");
        Suscriptor b = suscribir("test-B");
        Suscriptor c = suscribir("test-C");

        notificador.notificar(nuevaReserva("Charla"), "test-A");

        assertTrue(a.recibidas().isEmpty());
        assertEquals("Charla", b.recibidas().get(0).getReserva().getActividad());
        assertEquals("Charla", c.recibidas().get(0).getReserva().getActividad());
    }

    @Test
    void suscriptorCaidoSeQuitaYLosDemasSiguenRecibiendo() throws Exception {
        StreamCaido stream = new StreamCaido();
        notificador.agregar("test-caido", new ObjectOutputStream(stream));
        sesiones.add("test-caido");
        Suscriptor sano = suscribir("test-sano");

        stream.caido = true;
        notificador.notificar(nuevaReserva("Primera"), null);

        stream.caido = false;
        // si no lo hubiera sacado, esta le escribiria
        stream.bytesRecibidos = 0;
        notificador.notificar(nuevaReserva("Segunda"), null);

        assertEquals(0, stream.bytesRecibidos);
        assertEquals(List.of("Primera", "Segunda"),
                sano.recibidas().stream().map(n -> n.getReserva().getActividad()).toList());
    }

    @Test
    void sesionQuitadaNoRecibeMas() throws Exception {
        Suscriptor a = suscribir("test-A");
        Suscriptor b = suscribir("test-B");

        notificador.quitar("test-B");
        notificador.notificar(nuevaReserva("Charla"), null);

        assertEquals(1, a.recibidas().size());
        assertTrue(b.recibidas().isEmpty());
    }
}
