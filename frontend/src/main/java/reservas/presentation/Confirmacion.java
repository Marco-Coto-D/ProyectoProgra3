package reservas.presentation;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

// mismo diálogo que usa AlertasView para descartar, para borrar o cancelar en las demás pantallas
public class Confirmacion {

    private Confirmacion() {
    }

    public static boolean confirmar(Node nodo, String titulo, String mensaje) {
        return confirmar(nodo, titulo, mensaje, "Aceptar", "Cancelar");
    }

    /** con botones propios, para cuando "Aceptar"/"Cancelar" se presta a confusión.
     *  Texto fijo: ButtonType.OK/CANCEL salen en el idioma del sistema */
    public static boolean confirmar(Node nodo, String titulo, String mensaje, String textoSi, String textoNo) {
        return confirmar(nodo, titulo, mensaje,
                new ButtonType(textoSi, ButtonBar.ButtonData.OK_DONE),
                new ButtonType(textoNo, ButtonBar.ButtonData.CANCEL_CLOSE));
    }

    private static boolean confirmar(Node nodo, String titulo, String mensaje, ButtonType si, ButtonType no) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, si, no);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        if (nodo.getScene() != null && nodo.getScene().getWindow() != null) {
            alerta.initOwner(nodo.getScene().getWindow());
        }
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == si;
    }
}
