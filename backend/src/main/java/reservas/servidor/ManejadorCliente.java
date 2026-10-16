package reservas.servidor;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.logic.ResultadoAutenticacion;
import reservas.logic.Service;
import reservas.protocolo.Notificacion;
import reservas.protocolo.Operacion;
import reservas.protocolo.Peticion;
import reservas.protocolo.Respuesta;
import reservas.protocolo.TipoNotificacion;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ManejadorCliente implements Runnable {
    private final Socket socket;
    private String sesionId;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {
            salida.flush();
            try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
                while (true) {
                    Peticion peticion = (Peticion) entrada.readObject();
                    System.out.println("Peticion recibida: " + peticion);
                    Respuesta respuesta = atender(peticion);
                    System.out.println("Respuesta enviada: " + respuesta);
                    salida.writeObject(respuesta);
                    salida.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("Cliente desconectado (" + socket.getInetAddress() + "): " + e.getMessage());
        } finally {
            if (sesionId != null) {
                Service.instance().cerrarSesion(sesionId);
            }
            try {
                socket.close();
            } catch (Exception ignored) { }
        }
    }

    @SuppressWarnings("unchecked")
    private Respuesta atender(Peticion peticion) {
        try {
            if (sesionId == null
                    && peticion.getOperacion() != Operacion.AUTENTICAR
                    && peticion.getOperacion() != Operacion.CAMBIAR_CLAVE) {
                return Respuesta.error("Tenés que iniciar sesión");
            }
            switch (peticion.getOperacion()) {
                case AUTENTICAR:
                    ResultadoAutenticacion resultado = Service.instance().autenticar(
                            (String) peticion.getArgumento(0),
                            (String) peticion.getArgumento(1));
                    if (sesionId != null) {
                        Service.instance().cerrarSesion(sesionId);
                    }
                    sesionId = resultado.sesionId();
                    return Respuesta.ok(resultado);
                case CAMBIAR_CLAVE:
                    Service.instance().cambiarClave(
                            (String) peticion.getArgumento(0),
                            (String) peticion.getArgumento(1),
                            (String) peticion.getArgumento(2));
                    return Respuesta.ok();
                case LISTAR_CATEGORIAS:
                    return Respuesta.ok(Service.instance().listarCategorias());
                case BUSCAR_CATEGORIAS:
                    return Respuesta.ok(Service.instance().buscarCategorias(
                            (String) peticion.getArgumento(0)));
                case GUARDAR_CATEGORIA:
                    Service.instance().guardarCategoria(
                            (CategoriaRecurso) peticion.getArgumento(0));
                    return Respuesta.ok();
                case BORRAR_CATEGORIA:
                    Service.instance().borrarCategoria(
                            (String) peticion.getArgumento(0));
                    return Respuesta.ok();
                case LISTAR_RECURSOS:
                    return Respuesta.ok(Service.instance().listarRecursos());
                case BUSCAR_RECURSOS:
                    return Respuesta.ok(Service.instance().buscarRecursos(
                            (String) peticion.getArgumento(0)));
                case BUSCAR_RECURSOS_POR_CATEGORIA:
                    return Respuesta.ok(Service.instance().buscarRecursosPorCategoria(
                            (String) peticion.getArgumento(0)));
                case GUARDAR_RECURSO:
                    Service.instance().guardarRecurso(
                            (Recurso) peticion.getArgumento(0),
                            (Boolean) peticion.getArgumento(1));
                    return Respuesta.ok();
                case LISTAR_FUNCIONARIOS:
                    return Respuesta.ok(Service.instance().listarFuncionarios());
                case BUSCAR_FUNCIONARIOS:
                    return Respuesta.ok(Service.instance().buscarFuncionarios(
                            (String) peticion.getArgumento(0)));
                case GUARDAR_FUNCIONARIO:
                    Service.instance().guardarFuncionario(
                            (Funcionario) peticion.getArgumento(0),
                            (Boolean) peticion.getArgumento(1));
                    return Respuesta.ok();
                case LISTAR_MIS_RESERVAS:
                    return Respuesta.ok(Service.instance().listarMisReservas(sesionId));
                case LISTAR_RESERVAS_ENTRE:
                    return Respuesta.ok(Service.instance().listarReservasEntre(
                            (LocalDate) peticion.getArgumento(0),
                            (LocalDate) peticion.getArgumento(1)));
                case CREAR_RESERVA:
                    Reserva reserva = Service.instance().crearReserva(
                            (String) peticion.getArgumento(0),
                            (LocalDate) peticion.getArgumento(1),
                            (LocalTime) peticion.getArgumento(2),
                            (LocalTime) peticion.getArgumento(3),
                            sesionId,
                            (List<CategoriaRecurso>) peticion.getArgumento(4));
                    Notificador.instance().notificar(
                            new Notificacion(TipoNotificacion.NUEVA_RESERVA, reserva), sesionId);
                    return Respuesta.ok(reserva);
                case CANCELAR_RESERVA:
                    Service.instance().cancelarReserva(
                            (String) peticion.getArgumento(0), sesionId);
                    return Respuesta.ok();
                case BORRAR_RECURSO:
                    Service.instance().borrarRecurso(
                            (String) peticion.getArgumento(0));
                    return Respuesta.ok();
                case BORRAR_FUNCIONARIO:
                    Service.instance().borrarFuncionario(
                            (String) peticion.getArgumento(0));
                    return Respuesta.ok();
                case EXTRAER_IA:
                    return Respuesta.ok(Service.instance().extraerIA(
                            (String) peticion.getArgumento(0)));
                default:
                    return Respuesta.error("Operación no soportada aún");
            }
        } catch (Exception e) {
            return Respuesta.error(e.getMessage());
        }
    }
}
