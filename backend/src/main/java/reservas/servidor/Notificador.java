package reservas.servidor;

import reservas.protocolo.Notificacion;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Notificador {
    private static Notificador theInstance;

    public static synchronized Notificador instance() {
        if (theInstance == null) theInstance = new Notificador();
        return theInstance;
    }

    // todo synchronized pq varios clientes pueden notificar al mismo tiempo
    private final Map<String, ObjectOutputStream> suscriptores = new HashMap<>();

    private Notificador() {
    }

    public synchronized void agregar(String sesionId, ObjectOutputStream salida) {
        suscriptores.put(sesionId, salida);
    }

    public synchronized void quitar(String sesionId) {
        ObjectOutputStream salida = suscriptores.remove(sesionId);
        cerrar(salida);
    }

    public synchronized void notificar(Notificacion notificacion, String excepto) {
        Iterator<Map.Entry<String, ObjectOutputStream>> it = suscriptores.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ObjectOutputStream> suscriptor = it.next();
            if (suscriptor.getKey().equals(excepto)) {
                continue;
            }
            try {
                suscriptor.getValue().writeObject(notificacion);
                suscriptor.getValue().flush();
                System.out.println("Notificación enviada a " + suscriptor.getKey() + ": " + notificacion);
            } catch (IOException e) {
                System.out.println("Suscriptor caído, se quita: " + suscriptor.getKey());
                it.remove();
                cerrar(suscriptor.getValue());
            }
        }
    }

    private void cerrar(ObjectOutputStream salida) {
        if (salida == null) {
            return;
        }
        try {
            salida.close();
        } catch (IOException ignored) { }
    }
}
