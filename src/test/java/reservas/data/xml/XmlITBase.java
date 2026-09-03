package reservas.data.xml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

abstract class XmlITBase {

    private static final File ARCHIVO = new File("data/sistema.xml");
    private byte[] backup;

    @BeforeEach
    void backupYLimpiar() throws IOException {
        ARCHIVO.getParentFile().mkdirs();
        if (ARCHIVO.exists()) {
            backup = Files.readAllBytes(ARCHIVO.toPath());
        } else {
            backup = null;
        }
        XmlUtil.guardar(new SistemaXml());
    }

    @AfterEach
    void restaurar() throws IOException {
        if (backup != null) {
            Files.write(ARCHIVO.toPath(), backup);
        } else {
            ARCHIVO.delete();
        }
    }
}
