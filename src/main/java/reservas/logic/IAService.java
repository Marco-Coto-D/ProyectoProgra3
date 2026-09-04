package reservas.logic;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IAService {

    public DatosReservaIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) throws Exception {
        String listaCategorias = categoriasDisponibles.stream()
                .map(CategoriaRecurso::getDescripcion)
                .collect(Collectors.joining(", "));

        ChatLanguageModel modelo = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        ReservaExtractorService aiService = AiServices.create(ReservaExtractorService.class, modelo);

        ReservaExtraccion resultado = aiService.extraer(frase, listaCategorias, LocalDate.now().toString());

        String actividad     = resultado.getActividad();
        LocalDate fecha      = LocalDate.parse(resultado.getFecha());
        LocalTime horaInicio = LocalTime.parse(resultado.getHoraInicio());
        LocalTime horaFin    = LocalTime.parse(resultado.getHoraFin());

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
}
