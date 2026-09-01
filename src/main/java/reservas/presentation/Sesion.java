package reservas.presentation;

import reservas.logic.Usuario;

public class Sesion {

    private static Usuario usuario;

    private Sesion() {
    }

    public static Usuario getUsuario() { return usuario; }
    public static void setUsuario(Usuario u) { usuario = u; }
    public static boolean isLoggedIn() { return usuario != null; }
    public static void logout() { usuario = null; }
}