package reservas.logic;

import java.util.Optional;

// los Dao se tragan la SQLException, entonces si un texto no cabe en la columna
// no se guarda y nadie se entera; por eso se revisa el largo antes de llegar a la base
public class ValidadorLargo {
    // mismos largos que las columnas de schema.sql
    public static final int MAX_ID = 50;
    public static final int MAX_DESCRIPCION = 150;
    public static final int MAX_NOMBRE = 150;
    public static final int MAX_TELEFONO = 30;
    public static final int MAX_CLAVE = 100;
    public static final int MAX_ACTIVIDAD = 200;

    public static Optional<String> validarCategoria(CategoriaRecurso categoria) {
        return primerError(
                validar("El id", categoria.getId(), MAX_ID),
                validar("La descripción", categoria.getDescripcion(), MAX_DESCRIPCION));
    }

    public static Optional<String> validarRecurso(Recurso recurso) {
        return primerError(
                validar("El id", recurso.getId(), MAX_ID),
                validar("La descripción", recurso.getDescripcion(), MAX_DESCRIPCION));
    }

    public static Optional<String> validarFuncionario(Funcionario funcionario) {
        return primerError(
                validar("El id", funcionario.getId(), MAX_ID),
                validar("El nombre", funcionario.getNombre(), MAX_NOMBRE),
                validar("El teléfono", funcionario.getTelefono(), MAX_TELEFONO),
                validar("La clave", funcionario.getClave(), MAX_CLAVE));
    }

    public static Optional<String> validarActividad(String actividad) {
        return validar("La actividad", actividad, MAX_ACTIVIDAD);
    }

    public static Optional<String> validarClave(String clave) {
        return validar("La clave", clave, MAX_CLAVE);
    }

    private static Optional<String> validar(String campo, String valor, int maximo) {
        if (valor != null && valor.length() > maximo) {
            return Optional.of(campo + " no puede tener más de " + maximo + " caracteres");
        }
        return Optional.empty();
    }

    @SafeVarargs
    private static Optional<String> primerError(Optional<String>... errores) {
        for (Optional<String> error : errores) {
            if (error.isPresent()) {
                return error;
            }
        }
        return Optional.empty();
    }
}
