package reservas.protocolo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PeticionTest {

    @Test
    void autenticarNoMuestraLaClave() {
        String texto = new Peticion(Operacion.AUTENTICAR, "101", "secreta123").toString();

        assertEquals("Peticion{AUTENTICAR, argumentos=[101, ***]}", texto);
        assertFalse(texto.contains("secreta123"));
    }

    @Test
    void cambiarClaveNoMuestraNingunaDeLasDosClaves() {
        String texto = new Peticion(Operacion.CAMBIAR_CLAVE, "101", "vieja123", "nueva456").toString();

        assertEquals("Peticion{CAMBIAR_CLAVE, argumentos=[101, ***, ***]}", texto);
        assertFalse(texto.contains("vieja123"));
        assertFalse(texto.contains("nueva456"));
    }

    @Test
    void ocultarLaClaveNoCambiaLoQueViajaAlBackend() {
        Peticion peticion = new Peticion(Operacion.AUTENTICAR, "101", "secreta123");
        peticion.toString();

        assertEquals("secreta123", peticion.getArgumento(1));
    }

    @Test
    void lasDemasOperacionesSeMuestranCompletas() {
        String texto = new Peticion(Operacion.BUSCAR_CATEGORIAS, "proyector").toString();

        assertEquals("Peticion{BUSCAR_CATEGORIAS, argumentos=[proyector]}", texto);
    }

    @Test
    void sinArgumentosNoFalla() {
        assertEquals("Peticion{LISTAR_CATEGORIAS, argumentos=[]}", new Peticion(Operacion.LISTAR_CATEGORIAS).toString());
    }
}
