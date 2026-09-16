package reservas.protocolo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Mensaje que el Frontend envía al Backend por el socket de petición/respuesta.
 * Los argumentos viajan en el orden que cada Operacion espera (documentado
 * en el BackendProxy, que es quien arma cada Peticion).
 */
public class Peticion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Operacion operacion;
    private final Object[] argumentos;

    public Peticion(Operacion operacion, Object... argumentos) {
        this.operacion = operacion;
        this.argumentos = argumentos;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public Object[] getArgumentos() {
        return argumentos;
    }

    public Object getArgumento(int indice) {
        return argumentos[indice];
    }

    @Override
    public String toString() {
        return "Peticion{" + operacion + ", argumentos=" + Arrays.toString(argumentos) + "}";
    }
}
