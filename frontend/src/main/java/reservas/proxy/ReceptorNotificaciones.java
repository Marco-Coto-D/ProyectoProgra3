package reservas.proxy;

import reservas.protocolo.Notificacion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ReceptorNotificaciones {
    private Socket socket;

    public void conectar(String host, int puerto, String sesionId, Consumer<Notificacion> alRecibir) throws IOException {
        socket = new Socket(host, puerto);
        ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
        salida.writeObject(sesionId);
        salida.flush();
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

        Thread hilo = new Thread(() -> {
            try {
                while (true) {
                    // ojo, esto corre en este hilo y no en el de javafx
                    alRecibir.accept((Notificacion) entrada.readObject());
                }
            } catch (Exception e) {
                // se cerro el socket, termina el hilo
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    public void desconectar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
