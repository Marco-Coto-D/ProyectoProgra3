package reservas.presentation.alertas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

public class AlertasModel extends AbstractModel {
    private final ObservableList<Reserva> alertas = FXCollections.observableArrayList();

    public ObservableList<Reserva> getAlertas() {
        return alertas;
    }

    public void agregar(Reserva reserva) {
        alertas.add(0, reserva);
    }

    public void quitar(Reserva reserva) {
        alertas.remove(reserva);
    }
}
