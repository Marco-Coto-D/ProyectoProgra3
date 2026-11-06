package reservas.logic;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class IAService {
    // la clave de OpenRouter esta en este archivo
    public static final String PROPERTIES_FILE_NAME = "/ia.properties";
    public static final String SIN_RESPUESTA = "no hubo respuesta en este momento (servicio ocupado o se llegó al límite diario de consultas). "
            + "Probá de nuevo en un rato o llená el formulario a mano.";

    public DatosReservaIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) throws Exception {
        String listaCategorias = categoriasDisponibles.stream()
                .map(CategoriaRecurso::getDescripcion)
                .collect(Collectors.joining(", "));

        ChatLanguageModel modelo = OpenAiChatModel.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .apiKey(leerApiKey())
                .modelName("nvidia/nemotron-3.5-super-vl:free")
                .build();

        ReservaExtractorService aiService = AiServices.create(ReservaExtractorService.class, modelo);

        ReservaExtraccion resultado;
        try {
            resultado = aiService.extraer(frase, listaCategorias, LocalDate.now().toString());
        } catch (Exception e) {
            // el detalle tecnico queda en la consola del Backend, al usuario le llega un mensaje claro
            System.out.println("Error de la IA: " + e.getMessage());
            throw new Exception(SIN_RESPUESTA);
        }

        String actividad     = resultado.getActividad();
        LocalDate fecha      = ParseoSeguro.parsearFecha(resultado.getFecha());
        LocalTime horaInicio = ParseoSeguro.parsearHora(resultado.getHoraInicio());
        LocalTime horaFin    = ParseoSeguro.parsearHora(resultado.getHoraFin());

        List<CategoriaRecurso> categoriasElegidas = new ArrayList<>();
        if (resultado.getCategoriasRecurso() != null) {
            for (String desc : resultado.getCategoriasRecurso()) {
                categoriasDisponibles.stream()
                        .filter(c -> c.getDescripcion().equals(desc))
                        .findFirst()
                        .ifPresent(categoriasElegidas::add);
            }
        }

        return new DatosReservaIA(actividad, fecha, horaInicio, horaFin, categoriasElegidas);
    }

    private String leerApiKey() throws Exception {
        InputStream entrada = getClass().getResourceAsStream(PROPERTIES_FILE_NAME);
        if (entrada == null) {
            throw new Exception("Falta el archivo ia.properties con la clave de la IA");
        }
        Properties prop = new Properties();
        prop.load(entrada);
        return prop.getProperty("ia_api_key");
    }
}
