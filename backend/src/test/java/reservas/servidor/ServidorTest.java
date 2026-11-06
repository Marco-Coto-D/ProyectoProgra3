package reservas.servidor;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServidorTest {

    private static Properties con(String clave, String valor) {
        Properties prop = new Properties();
        prop.setProperty(clave, valor);
        return prop;
    }

    @Test
    void usaElPuertoDelArchivo() {
        assertEquals(6000, Servidor.leerPuerto(con("servidor_puerto", "6000"), "servidor_puerto", 5555));
    }

    @Test
    void ignoraEspaciosAlrededor() {
        assertEquals(6000, Servidor.leerPuerto(con("servidor_puerto", " 6000 "), "servidor_puerto", 5555));
    }

    @Test
    void sinLaClaveUsaElPorDefecto() {
        assertEquals(5555, Servidor.leerPuerto(new Properties(), "servidor_puerto", 5555));
    }

    @Test
    void vacioUsaElPorDefecto() {
        assertEquals(5556, Servidor.leerPuerto(con("servidor_puerto_notificaciones", ""), "servidor_puerto_notificaciones", 5556));
    }

    @Test
    void conLetrasUsaElPorDefecto() {
        assertEquals(5555, Servidor.leerPuerto(con("servidor_puerto", "abc"), "servidor_puerto", 5555));
    }

    @Test
    void fueraDeRangoUsaElPorDefecto() {
        assertEquals(5555, Servidor.leerPuerto(con("servidor_puerto", "0"), "servidor_puerto", 5555));
        assertEquals(5555, Servidor.leerPuerto(con("servidor_puerto", "70000"), "servidor_puerto", 5555));
    }

    @Test
    void elArchivoDelProyectoTraeLosPuertosDeSiempre() throws Exception {
        Properties prop = new Properties();
        prop.load(Servidor.class.getResourceAsStream(Servidor.PROPERTIES_FILE_NAME));
        assertEquals(5555, Servidor.leerPuerto(prop, "servidor_puerto", 0));
        assertEquals(5556, Servidor.leerPuerto(prop, "servidor_puerto_notificaciones", 0));
    }
}
