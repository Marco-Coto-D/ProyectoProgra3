package reservas.proxy;

import reservas.logic.CategoriaRecurso;
import reservas.logic.DatosReservaIA;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.logic.ResultadoAutenticacion;
import reservas.protocolo.Operacion;
import reservas.protocolo.Peticion;
import reservas.protocolo.Respuesta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa al Backend remoto desde el punto de vista del Frontend.
 * Los Controllers le hablan a esta clase exactamente igual que antes le
 * hablaban a un Repositorio; por dentro, cada método arma una Peticion,
 * la manda por el socket y traduce la Respuesta. El socket es invisible
 * para quien la usa.
 */
public class BackendProxy {

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public void conectar(String host, int puerto) throws IOException {
        socket = new Socket(host, puerto);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    public void desconectar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    /** AUTENTICAR(id, clave). Si falla, lanza Exception("Usuario o clave incorrectos"). */
    public ResultadoAutenticacion autenticar(String id, String clave) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.AUTENTICAR, id, clave));
        return (ResultadoAutenticacion) respuesta.getResultado();
    }

    /** CAMBIAR_CLAVE(id, claveActual, claveNueva). Si la clave actual no coincide, lanza Exception("Clave actual incorrecta"). */
    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        enviar(new Peticion(Operacion.CAMBIAR_CLAVE, id, claveActual, claveNueva));
    }

    @SuppressWarnings("unchecked")
    public List<CategoriaRecurso> listarCategorias() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_CATEGORIAS));
        return (List<CategoriaRecurso>) respuesta.getResultado();
    }

    /** BUSCAR_CATEGORIAS(texto). Vacío: todas; si coincide con un id, esa sola; si no, por descripción. */
    @SuppressWarnings("unchecked")
    public List<CategoriaRecurso> buscarCategorias(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_CATEGORIAS, texto));
        return (List<CategoriaRecurso>) respuesta.getResultado();
    }

    /** GUARDAR_CATEGORIA(categoria). Con id null se crea (el Backend genera CAT-###); con id se actualiza. */
    public void guardarCategoria(CategoriaRecurso categoria) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_CATEGORIA, categoria));
    }

    /** BORRAR_CATEGORIA(categoriaId). Si tiene recursos, lanza Exception("No se puede borrar: hay recursos asociados a esta categoría"). */
    public void borrarCategoria(String categoriaId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_CATEGORIA, categoriaId));
    }

    /** LISTAR_RECURSOS(). Cada Recurso viene con su CategoriaRecurso completa. */
    @SuppressWarnings("unchecked")
    public List<Recurso> listarRecursos() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_RECURSOS));
        return (List<Recurso>) respuesta.getResultado();
    }

    /** BUSCAR_RECURSOS(texto). Vacío: todos; si coincide con un id, ese solo; si no, por descripción del recurso o de su categoría. */
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursos(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_RECURSOS, texto));
        return (List<Recurso>) respuesta.getResultado();
    }

    /** BUSCAR_RECURSOS_POR_CATEGORIA(categoriaId). Con categoriaId null devuelve todos. */
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursosPorCategoria(String categoriaId) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_RECURSOS_POR_CATEGORIA, categoriaId));
        return (List<Recurso>) respuesta.getResultado();
    }

    /**
     * GUARDAR_RECURSO(recurso, esNuevo). esNuevo = true crea, false actualiza.
     * Si es nuevo y el id ya existe, lanza Exception("Ya existe un registro con el id X").
     */
    public void guardarRecurso(Recurso recurso, boolean esNuevo) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_RECURSO, recurso, esNuevo));
    }

    /** LISTAR_FUNCIONARIOS(). Los Funcionario vienen completos (con clave), igual que en la app vieja. */
    @SuppressWarnings("unchecked")
    public List<Funcionario> listarFuncionarios() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_FUNCIONARIOS));
        return (List<Funcionario>) respuesta.getResultado();
    }

    /** BUSCAR_FUNCIONARIOS(texto). Vacío: todos; si coincide con un id, ese solo; si no, por nombre. */
    @SuppressWarnings("unchecked")
    public List<Funcionario> buscarFuncionarios(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_FUNCIONARIOS, texto));
        return (List<Funcionario>) respuesta.getResultado();
    }

    /**
     * GUARDAR_FUNCIONARIO(funcionario, esNuevo). esNuevo = true crea (el Backend
     * le pone el id como clave inicial); false actualiza con la clave que trae.
     * Si es nuevo y el id ya existe (funcionario o administrador), lanza
     * Exception("Ya existe un registro con el id X").
     */
    public void guardarFuncionario(Funcionario funcionario, boolean esNuevo) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_FUNCIONARIO, funcionario, esNuevo));
    }

    /** LISTAR_MIS_RESERVAS(funcionarioId). Todas las del funcionario, también las canceladas. */
    @SuppressWarnings("unchecked")
    public List<Reserva> listarMisReservas(String funcionarioId) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_MIS_RESERVAS, funcionarioId));
        return (List<Reserva>) respuesta.getResultado();
    }

    /**
     * LISTAR_RESERVAS_ENTRE(desde, hasta), ambos inclusive. Devuelve todas las
     * reservas del rango sin filtrar por estado: cada pantalla filtra ACTIVADA.
     */
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservasEntre(LocalDate desde, LocalDate hasta) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_RESERVAS_ENTRE, desde, hasta));
        return (List<Reserva>) respuesta.getResultado();
    }

    /**
     * CREAR_RESERVA(actividad, fecha, horaInicio, horaFin, funcionarioId, categorias).
     * Devuelve la Reserva creada con los recursos que asignó el Backend. Si no
     * hay disponibilidad lanza Exception("Sin disponibilidad para: ..."); las
     * validaciones de fecha/hora también vuelven como Exception con su mensaje.
     * Las categorías se copian a un ArrayList porque la lista que viene de
     * JavaFX (ObservableList) no es Serializable.
     */
    public Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String funcionarioId, List<CategoriaRecurso> categorias) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.CREAR_RESERVA,
                actividad, fecha, horaInicio, horaFin, funcionarioId, new ArrayList<>(categorias)));
        return (Reserva) respuesta.getResultado();
    }

    /**
     * CANCELAR_RESERVA(reservaId). Si no se puede cancelar lanza Exception con
     * el motivo de ReglasReserva ("Esta reserva ya está cancelada", "No se puede
     * cancelar una reserva pasada", "Esta reserva ya inició, no se puede cancelar")
     * o "La reserva no existe".
     */
    public void cancelarReserva(String reservaId) throws Exception {
        enviar(new Peticion(Operacion.CANCELAR_RESERVA, reservaId));
    }

    /** BORRAR_RECURSO(recursoId). Si tiene reservas (aunque estén canceladas), lanza Exception("No se puede borrar: hay reservas asociadas a este recurso"). */
    public void borrarRecurso(String recursoId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_RECURSO, recursoId));
    }

    /** BORRAR_FUNCIONARIO(funcionarioId). Si tiene reservas (aunque estén canceladas), lanza Exception("No se puede borrar: el funcionario tiene reservas asociadas"). */
    public void borrarFuncionario(String funcionarioId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_FUNCIONARIO, funcionarioId));
    }

    /**
     * EXTRAER_IA(frase). El Backend usa las categorías de la base. Tarda varios
     * segundos (llama al modelo); los campos que no pudo inferir vienen en null.
     */
    public DatosReservaIA extraerIA(String frase) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.EXTRAER_IA, frase));
        return (DatosReservaIA) respuesta.getResultado();
    }

    // synchronized: hay un solo socket. Si dos hilos (por ejemplo el de la IA
    // y el de JavaFX) llaman a la vez, sin esto las respuestas se pueden cruzar.
    private synchronized Respuesta enviar(Peticion peticion) throws Exception {
        salida.writeObject(peticion);
        salida.flush();
        Respuesta respuesta = (Respuesta) entrada.readObject();
        if (!respuesta.isOk()) {
            throw new Exception(respuesta.getError());
        }
        return respuesta;
    }
}
