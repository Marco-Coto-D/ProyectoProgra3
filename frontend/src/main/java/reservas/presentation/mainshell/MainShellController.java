package reservas.presentation.mainshell;

import javafx.scene.Parent;

import reservas.logic.Rol;
import reservas.presentation.Sesion;
import reservas.presentation.actividad.ActividadController;
import reservas.presentation.actividad.ActividadModel;
import reservas.presentation.actividad.ActividadView;
import reservas.presentation.alertas.AlertasController;
import reservas.presentation.alertas.AlertasModel;
import reservas.presentation.alertas.AlertasView;
import reservas.presentation.calendarizacion.CalendarizacionController;
import reservas.presentation.calendarizacion.CalendarizacionModel;
import reservas.presentation.calendarizacion.CalendarizacionView;
import reservas.presentation.categoria.CategoriaController;
import reservas.presentation.categoria.CategoriaModel;
import reservas.presentation.categoria.CategoriaView;
import reservas.presentation.clave.CambiarClaveController;
import reservas.presentation.clave.CambiarClaveModel;
import reservas.presentation.clave.CambiarClaveView;
import reservas.presentation.estadistica.EstadisticaController;
import reservas.presentation.estadistica.EstadisticaModel;
import reservas.presentation.estadistica.EstadisticaView;
import reservas.presentation.funcionario.FuncionarioController;
import reservas.presentation.funcionario.FuncionarioModel;
import reservas.presentation.funcionario.FuncionarioView;
import reservas.presentation.recurso.RecursoController;
import reservas.presentation.recurso.RecursoModel;
import reservas.presentation.recurso.RecursoView;
import reservas.presentation.reserva.ReservaController;
import reservas.presentation.reserva.ReservaModel;
import reservas.presentation.reserva.ReservaView;
import reservas.protocolo.Notificacion;
import reservas.proxy.BackendProxy;

public class MainShellController {
    private final MainShellView view;
    private final MainShellModel model;
    private final BackendProxy proxy;

    private AlertasView alertasView;
    private AlertasController alertasController;

    public MainShellController(MainShellView view, MainShellModel model, BackendProxy proxy) {
        this.view = view;
        this.model = model;
        this.proxy = proxy;
        view.setModel(model);
    }

    public void iniciar() {
        if (!Sesion.isLoggedIn()) {
            return;
        }

        model.setUsuario(Sesion.getUsuario());

        alertasView = new AlertasView();
        alertasController = new AlertasController(alertasView, new AlertasModel());

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
        view.mostrarAlertas(alertasView.getRoot());

        view.mostrar();
    }

    private ReservaController reservaController;

    private Parent construirReservas() {
        ReservaView reservaView = new ReservaView();
        ReservaModel reservaModel = new ReservaModel();
        reservaController = new ReservaController(reservaView, reservaModel, proxy);
        return reservaView.getRoot();
    }

    private void refrescarReservas() {
        if (reservaController != null) {
            reservaController.refrescar();
        }
    }

    private RecursoController recursoController;

    private Parent construirRecursos() {
        RecursoView recursoView = new RecursoView();
        RecursoModel recursoModel = new RecursoModel();
        recursoController = new RecursoController(recursoView, recursoModel, proxy);
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
        new CategoriaController(categoriaView, categoriaModel, proxy);
        return categoriaView.getRoot();
    }

    private FuncionarioController funcionarioController;

    private Parent construirFuncionarios() {
        FuncionarioView funcionarioView = new FuncionarioView();
        FuncionarioModel funcionarioModel = new FuncionarioModel();
        funcionarioController = new FuncionarioController(funcionarioView, funcionarioModel, proxy);
        return funcionarioView.getRoot();
    }

    private void refrescarFuncionarios() {
        if (funcionarioController != null) {
            funcionarioController.refrescar();
        }
    }

    private CalendarizacionController calendarizacionController;

    private Parent construirCalendarizacion() {
        CalendarizacionView calendarizacionView = new CalendarizacionView();
        CalendarizacionModel calendarizacionModel = new CalendarizacionModel();
        calendarizacionController = new CalendarizacionController(calendarizacionView, calendarizacionModel, proxy);
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
        new ActividadController(actividadView, actividadModel, proxy);
        return actividadView.getRoot();
    }

    private Parent construirEstadistica() {
        EstadisticaView estadisticaView = new EstadisticaView();
        EstadisticaModel estadisticaModel = new EstadisticaModel();
        new EstadisticaController(estadisticaView, estadisticaModel, proxy);
        return estadisticaView.getRoot();
    }

    private Parent construirCambiarClave() {
        CambiarClaveView claveView = new CambiarClaveView();
        CambiarClaveModel claveModel = new CambiarClaveModel();
        new CambiarClaveController(claveView, claveModel, proxy);
        return claveView.getRoot();
    }

    public void recibirNotificacion(Notificacion notificacion) {
        alertasController.recibir(notificacion);
    }
}
