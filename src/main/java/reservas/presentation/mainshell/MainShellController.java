package reservas.presentation.mainshell;

import javafx.scene.Parent;

import reservas.logic.Rol;
import reservas.presentation.Sesion;
import reservas.data.xml.ReservaRepositorioXml;
import reservas.data.xml.RecursoRepositorioXml;
import reservas.data.xml.CategoriaRecursoRepositorioXml;
import reservas.data.xml.FuncionarioRepositorioXml;
import reservas.data.xml.AdministradorRepositorioXml;
import reservas.logic.AutenticacionService;
import reservas.presentation.reserva.ReservaController;
import reservas.presentation.reserva.ReservaModel;
import reservas.presentation.reserva.ReservaView;
import reservas.presentation.recurso.RecursoController;
import reservas.presentation.recurso.RecursoModel;
import reservas.presentation.recurso.RecursoView;
import reservas.presentation.categoria.CategoriaController;
import reservas.presentation.categoria.CategoriaModel;
import reservas.presentation.categoria.CategoriaView;
import reservas.presentation.funcionario.FuncionarioController;
import reservas.presentation.funcionario.FuncionarioModel;
import reservas.presentation.funcionario.FuncionarioView;
import reservas.presentation.calendarizacion.CalendarizacionController;
import reservas.presentation.calendarizacion.CalendarizacionModel;
import reservas.presentation.calendarizacion.CalendarizacionView;
import reservas.presentation.actividad.ActividadController;
import reservas.presentation.actividad.ActividadModel;
import reservas.presentation.actividad.ActividadView;
import reservas.presentation.estadistica.EstadisticaController;
import reservas.presentation.estadistica.EstadisticaModel;
import reservas.presentation.estadistica.EstadisticaView;
import reservas.presentation.clave.CambiarClaveController;
import reservas.presentation.clave.CambiarClaveModel;
import reservas.presentation.clave.CambiarClaveView;

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

        if (Sesion.getUsuario().getRol() == Rol.FUNCIONARIO) {
            view.agregarPestana("Reservas", this::construirReservas);
        }

        if (Sesion.getUsuario().getRol() == Rol.ADMINISTRADOR) {
            view.agregarPestana("Recursos", this::construirRecursos);
            view.agregarPestana("Funcionarios", this::construirFuncionarios);
            view.agregarPestana("Categorías", this::construirCategorias);
        }

        view.agregarPestana("Calendarización", this::construirCalendarizacion);
        view.agregarPestana("Programación", this::construirActividad);
        view.agregarPestana("Estadísticas", this::construirEstadistica);
        view.agregarPestana("Cambiar clave", this::construirCambiarClave);

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

    private Parent construirRecursos() {
        RecursoView recursoView = new RecursoView();
        RecursoModel recursoModel = new RecursoModel();

        new RecursoController(
                recursoView,
                recursoModel,
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
        );

        return recursoView.getRoot();
    }

    private Parent construirCategorias() {
        CategoriaView categoriaView = new CategoriaView();
        CategoriaModel categoriaModel = new CategoriaModel();

        new CategoriaController(
                categoriaView,
                categoriaModel,
                new CategoriaRecursoRepositorioXml()
        );

        return categoriaView.getRoot();
    }

    private Parent construirFuncionarios() {
        FuncionarioView funcionarioView = new FuncionarioView();
        FuncionarioModel funcionarioModel = new FuncionarioModel();

        new FuncionarioController(
                funcionarioView,
                funcionarioModel,
                new FuncionarioRepositorioXml()
        );

        return funcionarioView.getRoot();
    }

    private Parent construirCalendarizacion() {
        CalendarizacionView calendarizacionView = new CalendarizacionView();
        CalendarizacionModel calendarizacionModel = new CalendarizacionModel();

        new CalendarizacionController(
                calendarizacionView,
                calendarizacionModel,
                new ReservaRepositorioXml(),
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
        );

        return calendarizacionView.getRoot();
    }

    private Parent construirActividad() {
        ActividadView actividadView = new ActividadView();
        ActividadModel actividadModel = new ActividadModel();

        new ActividadController(
                actividadView,
                actividadModel,
                new ReservaRepositorioXml()
        );

        return actividadView.getRoot();
    }

    private Parent construirEstadistica() {
        EstadisticaView estadisticaView = new EstadisticaView();
        EstadisticaModel estadisticaModel = new EstadisticaModel();

        new EstadisticaController(
                estadisticaView,
                estadisticaModel,
                new ReservaRepositorioXml()
        );

        return estadisticaView.getRoot();
    }

    private Parent construirCambiarClave() {
        CambiarClaveView claveView = new CambiarClaveView();
        CambiarClaveModel claveModel = new CambiarClaveModel();

        AutenticacionService autenticacion = new AutenticacionService(
                new FuncionarioRepositorioXml(),
                new AdministradorRepositorioXml()
        );

        new CambiarClaveController(claveView, claveModel, autenticacion);
        return claveView.getRoot();
    }
}