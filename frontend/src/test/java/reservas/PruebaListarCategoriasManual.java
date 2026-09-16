package reservas;

import reservas.logic.CategoriaRecurso;
import reservas.proxy.BackendProxy;
import reservas.servidor.Servidor;

import java.util.List;

/**
 * Prueba manual de punta a punta del Paso 2: arranca el Servidor del
 * Backend en un hilo dentro de este mismo proceso, conecta un
 * BackendProxy, pide LISTAR_CATEGORIAS y compara el resultado contra la
 * fila de prueba insertada por schema.sql (id = "CAT-PRUEBA").
 *
 * No es un test JUnit (por eso el nombre no matchea los patrones de
 * Surefire ni tiene anotaciones @Test): es un main() para correr a mano
 * y ver la consola, tal como se pidió para este paso. Requiere una
 * instancia de MySQL corriendo en localhost con la base "reservas" ya
 * creada a partir de backend/src/main/resources/schema.sql.
 *
 * Se ejecuta con:
 *   mvn -pl frontend test-compile
 *   mvn -pl frontend dependency:build-classpath -Dmdep.outputFile=cp.txt
 *   java -cp frontend/target/test-classes:frontend/target/classes:$(cat frontend/cp.txt) reservas.PruebaListarCategoriasManual
 */
public class PruebaListarCategoriasManual {

    public static void main(String[] args) throws Exception {
        Thread hiloServidor = new Thread(() -> {
            try {
                Servidor.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        hiloServidor.setDaemon(true);
        hiloServidor.start();

        Thread.sleep(1000);

        BackendProxy proxy = new BackendProxy();
        proxy.conectar("localhost", Servidor.PUERTO);

        List<CategoriaRecurso> categorias = proxy.listarCategorias();

        System.out.println("Categorías recibidas del Backend (" + categorias.size() + "):");
        for (CategoriaRecurso c : categorias) {
            System.out.println(" - " + c.getId() + ": " + c.getDescripcion());
        }

        boolean encontroPrueba = categorias.stream()
                .anyMatch(c -> "CAT-PRUEBA".equals(c.getId()));

        System.out.println(encontroPrueba
                ? "OK: se encontró la categoría de prueba insertada por schema.sql."
                : "FALLO: no se encontró CAT-PRUEBA. Revisá que schema.sql se haya corrido contra la base 'reservas'.");

        proxy.desconectar();
        System.exit(encontroPrueba ? 0 : 1);
    }
}
