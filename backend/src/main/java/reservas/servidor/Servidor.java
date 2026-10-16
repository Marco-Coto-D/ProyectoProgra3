package reservas.servidor;

import reservas.logic.Service;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static final int PUERTO = 5555;
    public static final int PUERTO_NOTIFICACIONES = 5556;

    public static void main(String[] args) throws Exception {
        Thread hiloNotificaciones = new Thread(Servidor::aceptarNotificaciones);
        hiloNotificaciones.setDaemon(true);
        hiloNotificaciones.start();

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Backend escuchando en el puerto " + PUERTO);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                new Thread(new ManejadorCliente(socket)).start();
            }
        }
    }

    private static void aceptarNotificaciones() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_NOTIFICACIONES)) {
            System.out.println("Notificaciones escuchando en el puerto " + PUERTO_NOTIFICACIONES);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> registrarSuscriptor(socket)).start();
            }
        } catch (Exception e) {
            System.out.println("Error en el socket de notificaciones: " + e.getMessage());
        }
    }

    private static void registrarSuscriptor(Socket socket) {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            String sesionId = (String) entrada.readObject();
            if (Service.instance().sesionValida(sesionId)) {
                Notificador.instance().agregar(sesionId, salida);
                System.out.println("Suscriptor de notificaciones registrado: " + sesionId);
            } else {
                System.out.println("Suscriptor rechazado, sesionId inválido: " + sesionId);
                socket.close();
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception ignored) { }
        }
    }
}
