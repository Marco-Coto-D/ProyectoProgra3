package reservas.proxy;

import org.junit.jupiter.api.Test;
import reservas.logic.CategoriaRecurso;
import reservas.protocolo.Respuesta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackendProxyTest {

    /** servidor falso de una sola conexión: atiende como diga la prueba y cierra */
    private interface Atencion {
        void atender(ObjectInputStream entrada, ObjectOutputStream salida) throws Exception;
    }

    private int servidorFalso(Atencion atencion) throws IOException {
        ServerSocket server = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try (server; Socket socket = server.accept()) {
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                salida.flush();
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                atencion.atender(entrada, salida);
            } catch (Exception ignored) {
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        return server.getLocalPort();
    }

    @Test
    void conectarSinBackendDaMensajeEntendible() throws Exception {
        int puertoLibre;
        try (ServerSocket s = new ServerSocket(0)) {
            puertoLibre = s.getLocalPort();
        }
        BackendProxy proxy = new BackendProxy();

        IOException e = assertThrows(IOException.class, () -> proxy.conectar("localhost", puertoLibre));
        assertEquals(BackendProxy.SIN_CONEXION, e.getMessage());
    }

    @Test
    void backendQueSeCaeDuranteLaPeticionDaMensajeEntendible() throws Exception {
        // acepta la conexión y la cierra sin contestar, como un Backend que se cayó
        int puerto = servidorFalso((entrada, salida) -> { });
        BackendProxy proxy = new BackendProxy();
        proxy.conectar("localhost", puerto);

        Exception e = assertThrows(Exception.class, proxy::listarCategorias);
        assertEquals(BackendProxy.SIN_CONEXION, e.getMessage());
    }

    @Test
    void despuesDeCaerseSigueDandoElMismoMensaje() throws Exception {
        int puerto = servidorFalso((entrada, salida) -> { });
        BackendProxy proxy = new BackendProxy();
        proxy.conectar("localhost", puerto);
        assertThrows(Exception.class, proxy::listarCategorias);

        Exception e = assertThrows(Exception.class, () -> proxy.guardarCategoria(new CategoriaRecurso(null, "Proyector")));
        assertEquals(BackendProxy.SIN_CONEXION, e.getMessage());
    }

    @Test
    void errorDelBackendSeMuestraTalCual() throws Exception {
        int puerto = servidorFalso((entrada, salida) -> {
            entrada.readObject();
            salida.writeObject(Respuesta.error("Ya existe un registro con el id PROY-01"));
            salida.flush();
        });
        BackendProxy proxy = new BackendProxy();
        proxy.conectar("localhost", puerto);

        Exception e = assertThrows(Exception.class, proxy::listarCategorias);
        assertEquals("Ya existe un registro con el id PROY-01", e.getMessage());
    }

    @Test
    void respuestaCorrectaDevuelveElResultado() throws Exception {
        int puerto = servidorFalso((entrada, salida) -> {
            entrada.readObject();
            salida.writeObject(Respuesta.ok(List.of(new CategoriaRecurso("CAT-001", "Proyector"))));
            salida.flush();
        });
        BackendProxy proxy = new BackendProxy();
        proxy.conectar("localhost", puerto);

        List<CategoriaRecurso> categorias = proxy.listarCategorias();
        assertEquals("CAT-001", categorias.get(0).getId());
    }
}
