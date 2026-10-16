package reservas.presentation.alertas;

import org.junit.jupiter.api.Test;
import reservas.logic.Funcionario;
import reservas.logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertasModelTest {

    private static Reserva reserva(String id, String actividad) {
        return new Reserva(id, actividad, LocalDate.of(2030, 6, 3), LocalTime.of(9, 0), LocalTime.of(10, 0),
                new Funcionario("F-1", "x", "Ana", "1"));
    }

    private static List<String> actividades(AlertasModel model) {
        return model.getAlertas().stream().map(Reserva::getActividad).toList();
    }

    @Test
    void laAlertaMasNuevaQuedaPrimero() {
        AlertasModel model = new AlertasModel();

        model.agregar(reserva("A", "Charla"));
        model.agregar(reserva("B", "Taller"));
        model.agregar(reserva("C", "Reunión"));

        assertEquals(List.of("Reunión", "Taller", "Charla"), actividades(model));
    }

    @Test
    void quitarSacaSoloEsaAlerta() {
        AlertasModel model = new AlertasModel();
        Reserva charla = reserva("A", "Charla");
        Reserva taller = reserva("B", "Taller");
        model.agregar(charla);
        model.agregar(taller);

        model.quitar(charla);

        assertEquals(List.of("Taller"), actividades(model));
    }
}
