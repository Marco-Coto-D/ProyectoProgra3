package reservas.logic;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReservaExtractorService {

    @SystemMessage("""
            Sos un asistente que extrae datos de reservas de recursos a partir de una frase \
            en lenguaje natural escrita por el usuario.
            Reglas:
            - La fecha debe ir en formato ISO yyyy-MM-dd. Si el usuario menciona una fecha \
            relativa (por ejemplo "mañana", "el viernes"), resolvela usando como referencia \
            la fecha de hoy que se te indica.
            - Las horas deben ir en formato HH:mm de 24 horas.
            - Las categorías que devolvés deben ser EXACTAMENTE alguna(s) de las que aparecen \
            en la lista de categorías disponibles que se te pasa. Nunca inventes categorías \
            nuevas ni uses una descripción que no esté en esa lista.
            - Si no podés inferir con certeza algún campo, devolvé null para ese campo.
            """)
    @UserMessage("""
            Hoy es {{hoy}}.
            Categorías de recursos disponibles: {{categorias}}.
            Frase del usuario: "{{frase}}"

            Extraé de la frase: actividad, fecha, horaInicio, horaFin y las categorías (de la \
            lista disponible) que correspondan.
            """)
    ReservaExtraccion extraer(@V("frase") String frase,
                               @V("categorias") String categoriasList,
                               @V("hoy") String fechaHoy);
}
