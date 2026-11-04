package reservas.protocolo;

import java.io.Serializable;
import java.util.Arrays;

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

    // el Backend imprime cada petición; las claves no tienen que quedar en el log
    @Override
    public String toString() {
        Object[] visibles = argumentos == null ? null : argumentos.clone();
        if (visibles != null && (operacion == Operacion.AUTENTICAR || operacion == Operacion.CAMBIAR_CLAVE)) {
            // AUTENTICAR(id, clave) y CAMBIAR_CLAVE(id, claveActual, claveNueva): todo menos el id es clave
            for (int i = 1; i < visibles.length; i++) {
                visibles[i] = "***";
            }
        }
        return "Peticion{" + operacion + ", argumentos=" + Arrays.toString(visibles) + "}";
    }
}
