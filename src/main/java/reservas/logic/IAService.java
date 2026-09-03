package reservas.logic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IAService {

    private static final String URL_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    public DatosReservaIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new Exception("Variable de entorno GEMINI_API_KEY no configurada. " +
                    "Agregala en Run → Edit Configurations → Environment variables.");
        }

        String listaCategorias = categoriasDisponibles.stream()
                .map(CategoriaRecurso::getDescripcion)
                .collect(Collectors.joining(", "));

        String prompt = """
                Hoy es %s. Categorías de recursos disponibles: %s.
                El usuario quiere reservar con esta descripción: "%s"
                Devolvé SOLO un JSON válido (sin markdown, sin texto adicional) con estas claves exactas:
                {"actividad":"...","fecha":"yyyy-MM-dd","horaInicio":"HH:mm","horaFin":"HH:mm","categorias":["descripción exacta","..."]}
                Usá únicamente descripciones de categorías que aparezcan en la lista disponible.
                Si no podés inferir algún campo, usá valores razonables.
                """.formatted(LocalDate.now(), listaCategorias, frase);

        String cuerpo = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))))
                .toString();

        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new Exception("Error HTTP " + respuesta.statusCode() + " de Gemini: " + respuesta.body());
        }

        JSONObject respJson = new JSONObject(respuesta.body());
        String texto = respJson
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim();

        // Limpiar posibles bloques ```json ... ```
        texto = texto.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)```\\s*$", "").trim();

        JSONObject datos = new JSONObject(texto);
        String actividad     = datos.getString("actividad");
        LocalDate fecha      = LocalDate.parse(datos.getString("fecha"));
        LocalTime horaInicio = LocalTime.parse(datos.getString("horaInicio"));
        LocalTime horaFin    = LocalTime.parse(datos.getString("horaFin"));

        List<CategoriaRecurso> categoriasElegidas = new ArrayList<>();
        JSONArray catArray = datos.optJSONArray("categorias");
        if (catArray != null) {
            for (int i = 0; i < catArray.length(); i++) {
                String desc = catArray.getString(i);
                categoriasDisponibles.stream()
                        .filter(c -> c.getDescripcion().equals(desc))
                        .findFirst()
                        .ifPresent(categoriasElegidas::add);
            }
        }

        return new DatosReservaIA(actividad, fecha, horaInicio, horaFin, categoriasElegidas);
    }
}
