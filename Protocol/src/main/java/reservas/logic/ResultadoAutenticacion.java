package reservas.logic;

import java.io.Serializable;

public record ResultadoAutenticacion(Usuario usuario, String sesionId) implements Serializable {}
