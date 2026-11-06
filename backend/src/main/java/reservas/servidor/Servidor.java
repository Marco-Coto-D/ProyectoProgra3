package reservas.servidor;

import reservas.logic.Service;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Servidor {

    // los puertos se leen de servidor.properties (mismas claves que el del Frontend);
    // si falta el archivo o una clave, se usan estos
    public static final String PROPERTIES_FILE_NAME = "/servidor.properties";
    public static final int PUERTO_POR_DEFECTO = 5555;
    public static final int PUERTO_NOTIFICACIONES_POR_DEFECTO = 5556;

    private static int puerto;
    private static int puertoNotificaciones;

    public static void main(String[] args) throws Exception {
        Properties prop = leerPropiedades();
        puerto = leerPuerto(prop, "servidor_puerto", PUERTO_POR_DEFECTO);
        puertoNotificaciones = leerPuerto(prop, "servidor_puerto_notificaciones", PUERTO_NOTIFICACIONES_POR_DEFECTO);

        // los dos puertos se abren antes de atender a nadie: si alguno está ocupado el Backend
        // no arranca, en vez de quedar andando sin alertas
        ServerSocket socketPeticiones = abrir(puerto, "servidor_puerto");
        ServerSocket socketNotificaciones = abrir(puertoNotificaciones, "servidor_puerto_notificaciones");

        Thread hiloNotificaciones = new Thread(() -> aceptarNotificaciones(socketNotificaciones));
        hiloNotificaciones.setDaemon(true);
        hiloNotificaciones.start();

        try (ServerSocket serverSocket = socketPeticiones) {
            System.out.println("Backend escuchando en el puerto " + puerto);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                new Thread(new ManejadorCliente(socket)).start();
            }
        }
    }

    private static ServerSocket abrir(int puerto, String clave) {
        try {
            return new ServerSocket(puerto);
        } catch (Exception e) {
            System.out.println("No se pudo abrir el puerto " + puerto + " (" + e.getMessage() + "). "
                    + "¿Hay otro Backend abierto? Cerralo, o cambiá " + clave + " en servidor.properties "
                    + "(en el Backend y en el Frontend).");
            System.exit(1);
            return null;
        }
    }

    private static Properties leerPropiedades() {
        Properties prop = new Properties();
        try (InputStream archivo = Servidor.class.getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (archivo != null) {
                prop.load(archivo);
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer servidor.properties, se usan los puertos por defecto");
        }
        return prop;
    }

    static int leerPuerto(Properties prop, String clave, int porDefecto) {
        String valor = prop.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            int puerto = Integer.parseInt(valor.trim());
            if (puerto >= 1 && puerto <= 65535) {
                return puerto;
            }
        } catch (NumberFormatException ignored) { }
        System.out.println(clave + "=" + valor + " no es un puerto válido, se usa " + porDefecto);
        return porDefecto;
    }

    private static void aceptarNotificaciones(ServerSocket socketNotificaciones) {
        try (ServerSocket serverSocket = socketNotificaciones) {
            System.out.println("Notificaciones escuchando en el puerto " + puertoNotificaciones);
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
