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

    /** AUTENTICAR(id, clave) */
    public ResultadoAutenticacion autenticar(String id, String clave) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.AUTENTICAR, id, clave));
        return (ResultadoAutenticacion) respuesta.getResultado();
    }

    /** CAMBIAR_CLAVE(id, claveActual, claveNueva) */
    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        enviar(new Peticion(Operacion.CAMBIAR_CLAVE, id, claveActual, claveNueva));
    }

    /** LISTAR_CATEGORIAS() */
    @SuppressWarnings("unchecked")
    public List<CategoriaRecurso> listarCategorias() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_CATEGORIAS));
        return (List<CategoriaRecurso>) respuesta.getResultado();
    }

    /** BUSCAR_CATEGORIAS(texto) */
    @SuppressWarnings("unchecked")
    public List<CategoriaRecurso> buscarCategorias(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_CATEGORIAS, texto));
        return (List<CategoriaRecurso>) respuesta.getResultado();
    }

    /** GUARDAR_CATEGORIA(categoria) */
    public void guardarCategoria(CategoriaRecurso categoria) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_CATEGORIA, categoria));
    }

    /** BORRAR_CATEGORIA(categoriaId) */
    public void borrarCategoria(String categoriaId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_CATEGORIA, categoriaId));
    }

    /** LISTAR_RECURSOS() */
    @SuppressWarnings("unchecked")
    public List<Recurso> listarRecursos() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_RECURSOS));
        return (List<Recurso>) respuesta.getResultado();
    }

    /** BUSCAR_RECURSOS(texto) */
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursos(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_RECURSOS, texto));
        return (List<Recurso>) respuesta.getResultado();
    }

    /** BUSCAR_RECURSOS_POR_CATEGORIA(categoriaId) */
    @SuppressWarnings("unchecked")
    public List<Recurso> buscarRecursosPorCategoria(String categoriaId) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_RECURSOS_POR_CATEGORIA, categoriaId));
        return (List<Recurso>) respuesta.getResultado();
    }

    /** GUARDAR_RECURSO(recurso, esNuevo) */
    public void guardarRecurso(Recurso recurso, boolean esNuevo) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_RECURSO, recurso, esNuevo));
    }

    /** LISTAR_FUNCIONARIOS() */
    @SuppressWarnings("unchecked")
    public List<Funcionario> listarFuncionarios() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_FUNCIONARIOS));
        return (List<Funcionario>) respuesta.getResultado();
    }

    /** BUSCAR_FUNCIONARIOS(texto) */
    @SuppressWarnings("unchecked")
    public List<Funcionario> buscarFuncionarios(String texto) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.BUSCAR_FUNCIONARIOS, texto));
        return (List<Funcionario>) respuesta.getResultado();
    }

    /** GUARDAR_FUNCIONARIO(funcionario, esNuevo) */
    public void guardarFuncionario(Funcionario funcionario, boolean esNuevo) throws Exception {
        enviar(new Peticion(Operacion.GUARDAR_FUNCIONARIO, funcionario, esNuevo));
    }

    /** LISTAR_MIS_RESERVAS() */
    @SuppressWarnings("unchecked")
    public List<Reserva> listarMisReservas() throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_MIS_RESERVAS));
        return (List<Reserva>) respuesta.getResultado();
    }

    /** LISTAR_RESERVAS_ENTRE(desde, hasta) */
    @SuppressWarnings("unchecked")
    public List<Reserva> listarReservasEntre(LocalDate desde, LocalDate hasta) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.LISTAR_RESERVAS_ENTRE, desde, hasta));
        return (List<Reserva>) respuesta.getResultado();
    }

    /** CREAR_RESERVA(actividad, fecha, horaInicio, horaFin, categorias) */
    public Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                List<CategoriaRecurso> categorias) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.CREAR_RESERVA,
                // la lista que viene de javafx (ObservableList) no es serializable, por eso la copia
                actividad, fecha, horaInicio, horaFin, new ArrayList<>(categorias)));
        return (Reserva) respuesta.getResultado();
    }

    /** CANCELAR_RESERVA(reservaId) */
    public void cancelarReserva(String reservaId) throws Exception {
        enviar(new Peticion(Operacion.CANCELAR_RESERVA, reservaId));
    }

    /** BORRAR_RECURSO(recursoId) */
    public void borrarRecurso(String recursoId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_RECURSO, recursoId));
    }

    /** BORRAR_FUNCIONARIO(funcionarioId) */
    public void borrarFuncionario(String funcionarioId) throws Exception {
        enviar(new Peticion(Operacion.BORRAR_FUNCIONARIO, funcionarioId));
    }

    /** EXTRAER_IA(frase) */
    public DatosReservaIA extraerIA(String frase) throws Exception {
        Respuesta respuesta = enviar(new Peticion(Operacion.EXTRAER_IA, frase));
        return (DatosReservaIA) respuesta.getResultado();
    }

    // synchronized pq es un solo socket y la IA lo usa desde otro hilo, si no se cruzan las respuestas
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
