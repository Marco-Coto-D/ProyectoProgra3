package reservas.servidor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.data.mysql.Database;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.protocolo.Notificacion;
import reservas.protocolo.Operacion;
import reservas.protocolo.Peticion;
import reservas.protocolo.Respuesta;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// CREAR_RESERVA de punta a punta: socket -> ManejadorCliente -> Service -> ReservaDao -> Notificador
class ManejadorClienteIT {
    private ServerSocket servidor;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private final ByteArrayOutputStream bytesObservador = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws Exception {
        borrarDatosDePrueba();
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F001", "clave123", "Ana López", "8888-0000");
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT1", "TEST-Proyector");
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT2", "TEST-Laboratorio");
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R1", "TEST-Epson", "TEST-CAT1");
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R2", "TEST-Lab", "TEST-CAT2");

        servidor = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try {
                new ManejadorCliente(servidor.accept()).run();
            } catch (IOException ignored) { }
        });
        hilo.setDaemon(true);
        hilo.start();
        socket = new Socket("localhost", servidor.getLocalPort());
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());

        // otra ventana abierta que recibe las alertas
        ObjectOutputStream observador = new ObjectOutputStream(bytesObservador);
        observador.flush();
        Notificador.instance().agregar("test-observador", observador);
    }

    @AfterEach
    void tearDown() throws Exception {
        ejecutar("drop trigger if exists test_falla_reserva_recurso");
        Notificador.instance().quitar("test-observador");
        socket.close();
        servidor.close();
        borrarDatosDePrueba();
    }

    @Test
    void reservaCuyoInsertDeRecursosFallaNoQuedaEnLaBaseNiSeNotifica() throws Exception {
        assertTrue(enviar(new Peticion(Operacion.AUTENTICAR, "TEST-F001", "clave123")).isOk());

        // control: una reserva normal sí le llega al observador
        Respuesta buena = crear("TEST-Buena", LocalDate.of(2030, 3, 5), "TEST-CAT1");
        assertTrue(buena.isOk(), buena.getError());
        assertEquals(List.of("TEST-Buena"), alertasRecibidas());

        // la base rechaza la fila de TEST-R2; la de TEST-R1 ya se habrá guardado
        ejecutar("create trigger test_falla_reserva_recurso before insert on reserva_recurso for each row "
                + "begin if new.recurso = 'TEST-R2' then signal sqlstate '45000' set message_text = 'falla de prueba'; end if; end");

        Respuesta mala = crear("TEST-Falla", LocalDate.of(2030, 3, 4), "TEST-CAT1", "TEST-CAT2");

        assertFalse(mala.isOk());
        assertEquals("No se pudo guardar la reserva", mala.getError());
        assertEquals(0, contar("select count(*) from reserva where actividad = 'TEST-Falla'"));
        assertEquals(0, contar("select count(*) from reserva_recurso rr inner join reserva r on rr.reserva = r.id where r.actividad = 'TEST-Falla'"));
        assertEquals(0, contar("select count(*) from reserva_recurso where reserva not in (select id from reserva)"));
        assertEquals(List.of("TEST-Buena"), alertasRecibidas());
    }

    private Respuesta crear(String actividad, LocalDate fecha, String... categorias) throws Exception {
        ArrayList<CategoriaRecurso> lista = new ArrayList<>();
        for (String c : categorias) {
            lista.add(new CategoriaRecurso(c, ""));
        }
        return enviar(new Peticion(Operacion.CREAR_RESERVA, actividad, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0), lista));
    }

    private Respuesta enviar(Peticion peticion) throws Exception {
        salida.writeObject(peticion);
        salida.flush();
        return (Respuesta) entrada.readObject();
    }

    private List<String> alertasRecibidas() throws Exception {
        List<String> actividades = new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytesObservador.toByteArray()))) {
            while (true) {
                Reserva r = ((Notificacion) in.readObject()).getReserva();
                actividades.add(r.getActividad());
            }
        } catch (EOFException fin) {
            return actividades;
        }
    }

    private long contar(String sql) throws Exception {
        ResultSet rs = Database.instance().prepareStatement(sql).executeQuery();
        rs.next();
        return rs.getLong(1);
    }

    private void borrarDatosDePrueba() throws Exception {
        ejecutar("delete from reserva_recurso where reserva in (select id from reserva where funcionario like 'TEST-%') or recurso like 'TEST-%'");
        ejecutar("delete from reserva where funcionario like 'TEST-%'");
        ejecutar("delete from recurso where id like 'TEST-%'");
        ejecutar("delete from funcionario where id like 'TEST-%'");
        ejecutar("delete from categoria_recurso where id like 'TEST-%'");
    }

    private void ejecutar(String sql, String... parametros) throws Exception {
        PreparedStatement stm = Database.instance().prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            stm.setString(i + 1, parametros[i]);
        }
        stm.executeUpdate();
    }
}
