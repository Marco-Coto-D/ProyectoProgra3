package reservas.presentation.mainshell;

import javafx.scene.Parent;
import javafx.scene.control.Label;

import reservas.logic.Rol;
import reservas.presentation.Sesion;
import reservas.data.xml.ReservaRepositorioXml;
import reservas.data.xml.RecursoRepositorioXml;
import reservas.data.xml.CategoriaRecursoRepositorioXml;
import reservas.presentation.reserva.ReservaController;
import reservas.presentation.reserva.ReservaModel;
import reservas.presentation.reserva.ReservaView;

public class MainShellController {

    private final MainShellView view;
    private final MainShellModel model;

    public MainShellController(MainShellView view, MainShellModel model) {
        this.view = view;
        this.model = model;
        view.setModel(model);
    }

    public void iniciar() {
        if (!Sesion.isLoggedIn()) {
            return;
        }

        model.setUsuario(Sesion.getUsuario());

        view.agregarPestana("Reservas", this::construirReservas);

        if (Sesion.getUsuario().getRol() == Rol.ADMINISTRADOR) {
            view.agregarPestana("Recursos", () -> new Label("..."));
            view.agregarPestana("Funcionarios", () -> new Label("..."));
            view.agregarPestana("Categorías", () -> new Label("..."));
        }

        view.mostrar();
    }

    private Parent construirReservas() {
        ReservaView reservaView = new ReservaView();
        ReservaModel reservaModel = new ReservaModel();

        new ReservaController(
                reservaView,
                reservaModel,
                new ReservaRepositorioXml(),
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
        );

        return reservaView.getRoot();
    }
}