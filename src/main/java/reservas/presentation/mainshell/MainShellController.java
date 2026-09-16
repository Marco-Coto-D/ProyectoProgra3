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

    private ReservaController reservaController;
    private RecursoController recursoController;
    private FuncionarioController funcionarioController;
    private CalendarizacionController calendarizacionController;

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
            view.agregarPestana("Reservas", "reservas.png", this::construirReservas, this::refrescarReservas);
        }

        if (Sesion.getUsuario().getRol() == Rol.ADMINISTRADOR) {
            view.agregarPestana("Recursos", "recursos.png", this::construirRecursos, this::refrescarRecursos);
            view.agregarPestana("Funcionarios", "funcionarios.png", this::construirFuncionarios, this::refrescarFuncionarios);
            view.agregarPestana("Categorías", "categorias.png", this::construirCategorias);
        }

        view.agregarPestana("Calendarización", "calendarizacion.png", this::construirCalendarizacion, this::refrescarCalendarizacion);
        view.agregarPestana("Programación", "actividades.png", this::construirActividad);
        view.agregarPestana("Estadísticas", "statistics.png", this::construirEstadistica);
        view.agregarPestana("Cambiar clave", "clave.png", this::construirCambiarClave);

        view.mostrar();
    }

    private Parent construirReservas() {
        ReservaView reservaView = new ReservaView();
        ReservaModel reservaModel = new ReservaModel();

        reservaController = new ReservaController(
                reservaView,
                reservaModel,
                new ReservaRepositorioXml(),
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
        );

        return reservaView.getRoot();
    }

    private void refrescarReservas() {
        if (reservaController != null) {
            reservaController.refrescar();
        }
    }

    private Parent construirRecursos() {
        RecursoView recursoView = new RecursoView();
        RecursoModel recursoModel = new RecursoModel();

        recursoController = new RecursoController(
                recursoView,
                recursoModel,
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml(),
                new ReservaRepositorioXml()
        );

        return recursoView.getRoot();
    }

    private void refrescarRecursos() {
        if (recursoController != null) {
            recursoController.refrescar();
        }
    }

    private Parent construirCategorias() {
        CategoriaView categoriaView = new CategoriaView();
        CategoriaModel categoriaModel = new CategoriaModel();

        new CategoriaController(
                categoriaView,
                categoriaModel,
                new CategoriaRecursoRepositorioXml(),
                new RecursoRepositorioXml()
        );

        return categoriaView.getRoot();
    }

    private Parent construirFuncionarios() {
        FuncionarioView funcionarioView = new FuncionarioView();
        FuncionarioModel funcionarioModel = new FuncionarioModel();

        funcionarioController = new FuncionarioController(
                funcionarioView,
                funcionarioModel,
                new FuncionarioRepositorioXml(),
                new AdministradorRepositorioXml(),
                new ReservaRepositorioXml()
        );

        return funcionarioView.getRoot();
    }

    private void refrescarFuncionarios() {
        if (funcionarioController != null) {
            funcionarioController.refrescar();
        }
    }

    private Parent construirCalendarizacion() {
        CalendarizacionView calendarizacionView = new CalendarizacionView();
        CalendarizacionModel calendarizacionModel = new CalendarizacionModel();

        calendarizacionController = new CalendarizacionController(
                calendarizacionView,
                calendarizacionModel,
                new ReservaRepositorioXml(),
                new RecursoRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
        );

        return calendarizacionView.getRoot();
    }

    private void refrescarCalendarizacion() {
        if (calendarizacionController != null) {
            calendarizacionController.refrescar();
        }
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
                new ReservaRepositorioXml(),
                new CategoriaRecursoRepositorioXml()
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