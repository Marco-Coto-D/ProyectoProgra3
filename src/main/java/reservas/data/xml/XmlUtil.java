package reservas.data.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class XmlUtil {

    private static final String RUTA = "data/sistema.xml";

    public static SistemaXml cargar() {
        try {
            File archivo = new File(RUTA);
            if (!archivo.exists()) {
                return new SistemaXml();
            }
            JAXBContext contexto = JAXBContext.newInstance(SistemaXml.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            return (SistemaXml) unmarshaller.unmarshal(archivo);
        } catch (JAXBException e) {
            throw new RuntimeException("Error al cargar " + RUTA, e);
        }
    }

    public static void guardar(SistemaXml sistema) {
        try {
            File archivo = new File(RUTA);
            archivo.getParentFile().mkdirs();
            JAXBContext contexto = JAXBContext.newInstance(SistemaXml.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(sistema, archivo);
        } catch (JAXBException e) {
            throw new RuntimeException("Error al guardar " + RUTA, e);
        }
    }
}