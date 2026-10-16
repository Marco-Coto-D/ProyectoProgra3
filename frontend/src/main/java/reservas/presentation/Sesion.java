package reservas.presentation;

import reservas.logic.Usuario;

public class Sesion {
    private static Usuario usuario;
    private static String sesionId;

    private Sesion() {
    }

    public static Usuario getUsuario() { return usuario; }
    public static void setUsuario(Usuario u) { usuario = u; }
    public static String getSesionId() { return sesionId; }
    public static void setSesionId(String id) { sesionId = id; }
    public static boolean isLoggedIn() { return usuario != null; }
    public static void logout() { usuario = null; sesionId = null; }
}
