package reservas.protocolo;

import java.io.Serializable;

/**
 * Mensaje que el Backend devuelve como respuesta a una Peticion.
 * Si ok es false, error trae el mensaje para mostrarle al usuario
 * (el mismo texto que antes iba directo a model.setError(...)).
 */
public class Respuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean ok;
    private final Object resultado;
    private final String error;

    private Respuesta(boolean ok, Object resultado, String error) {
        this.ok = ok;
        this.resultado = resultado;
        this.error = error;
    }

    public static Respuesta ok(Object resultado) {
        return new Respuesta(true, resultado, null);
    }

    public static Respuesta ok() {
        return new Respuesta(true, null, null);
    }

    public static Respuesta error(String mensaje) {
        return new Respuesta(false, null, mensaje);
    }

    public boolean isOk() {
        return ok;
    }

    public Object getResultado() {
        return resultado;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return ok ? "Respuesta{ok, resultado=" + resultado + "}" : "Respuesta{error=" + error + "}";
    }
}
