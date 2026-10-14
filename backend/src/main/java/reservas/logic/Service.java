package reservas.logic;

import reservas.data.mysql.AdministradorDao;
import reservas.data.mysql.CategoriaRecursoDao;
import reservas.data.mysql.Database;
import reservas.data.mysql.FuncionarioDao;
import reservas.data.mysql.RecursoDao;
import reservas.data.mysql.ReservaDao;
import reservas.servidor.Notificador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mismo patrón que el Service.java de referencia: singleton, fachada única
 * del Backend que delega a los Dao. A diferencia del ejemplo del profesor,
 * acá no lo llama directo la Presentation: lo llama el ManejadorCliente de
 * cada conexión por socket.
 */
public class Service {
    private static Service theInstance;

    public static Service instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    private CategoriaRecursoDao categoriaRecursoDao;
    private RecursoDao recursoDao;
    private FuncionarioDao funcionarioDao;
    private AdministradorDao administradorDao;
    private ReservaDao reservaDao;
    private AutenticacionService autenticacionService;
    // Sesiones abiertas (sesionId -> Usuario). Concurrent porque cada cliente
    // tiene su propio hilo.
    private final Map<String, Usuario> sesiones = new ConcurrentHashMap<>();

    private Service(){
        try{
            categoriaRecursoDao = new CategoriaRecursoDao();
            recursoDao = new RecursoDao();
            funcionarioDao = new FuncionarioDao();
            administradorDao = new AdministradorDao();
            reservaDao = new ReservaDao();
            autenticacionService = new AutenticacionService();
        }
        catch(Exception e){
            System.exit(-1);
        }
    }

    public void stop(){
        try {
            Database.instance().close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public List<CategoriaRecurso> listarCategorias() {
        return categoriaRecursoDao.findAll();
    }

    public List<CategoriaRecurso> buscarCategorias(String texto) {
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            return categoriaRecursoDao.findAll();
        }
        CategoriaRecurso porId = categoriaRecursoDao.read(textoLimpio);
        if (porId != null) {
            return List.of(porId);
        }
        return categoriaRecursoDao.findByDescripcion(textoLimpio);
    }

    /** Si la categoría viene sin id es nueva: se le genera CAT-### y se crea; si no, se actualiza. */
    public void guardarCategoria(CategoriaRecurso categoria) {
        if (categoria.getId() == null) {
            categoria.setId(generarIdCategoria());
            categoriaRecursoDao.create(categoria);
        } else {
            categoriaRecursoDao.update(categoria);
        }
    }

    public void borrarCategoria(String categoriaId) throws Exception {
        if (!recursoDao.findByCategoria(categoriaId).isEmpty()) {
            throw new Exception("No se puede borrar: hay recursos asociados a esta categoría");
        }
        categoriaRecursoDao.delete(categoriaId);
    }

    // Misma lógica que tenía CategoriaController.generarId() en el proyecto viejo.
    private String generarIdCategoria() {
        Pattern patron = Pattern.compile("CAT-(\\d+)");
        int maximo = categoriaRecursoDao.findAll().stream()
                .mapToInt(c -> {
                    Matcher m = patron.matcher(c.getId());
                    return m.matches() ? Integer.parseInt(m.group(1)) : 0;
                })
                .max()
                .orElse(0);
        return String.format("CAT-%03d", maximo + 1);
    }

    public List<Recurso> listarRecursos() {
        return recursoDao.findAll();
    }

    public List<Recurso> buscarRecursos(String texto) {
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            return recursoDao.findAll();
        }
        Recurso porId = recursoDao.read(textoLimpio);
        if (porId != null) {
            return List.of(porId);
        }
        return recursoDao.findByTexto(textoLimpio);
    }

    /** Cuenta también las reservas canceladas, igual que el RecursoController viejo. */
    public void borrarRecurso(String recursoId) throws Exception {
        if (DependenciaUtil.recursoTieneReservas(reservaDao.findAll(), recursoId)) {
            throw new Exception("No se puede borrar: hay reservas asociadas a este recurso");
        }
        recursoDao.delete(recursoId);
    }

    /** Con categoriaId null devuelve todos, igual que el RecursoController viejo. */
    public List<Recurso> buscarRecursosPorCategoria(String categoriaId) {
        if (categoriaId == null) {
            return recursoDao.findAll();
        }
        return recursoDao.findByCategoria(categoriaId);
    }

    /**
     * esNuevo lo decide el Frontend (antes era "no hay nada seleccionado en
     * la tabla"). Si es nuevo y el id ya existe, ValidadorId da el error.
     */
    public void guardarRecurso(Recurso recurso, boolean esNuevo) throws Exception {
        boolean idYaExiste = recursoDao.read(recurso.getId()) != null;
        Optional<String> errorId = ValidadorId.validarIdParaCrear(esNuevo, idYaExiste, recurso.getId());
        if (errorId.isPresent()) {
            throw new Exception(errorId.get());
        }
        if (esNuevo) {
            recursoDao.create(recurso);
        } else {
            recursoDao.update(recurso);
        }
    }

    public List<Funcionario> listarFuncionarios() {
        return funcionarioDao.findAll();
    }

    public List<Funcionario> buscarFuncionarios(String texto) {
        String textoLimpio = texto == null ? "" : texto.trim();
        if (textoLimpio.isBlank()) {
            return funcionarioDao.findAll();
        }
        Funcionario porId = funcionarioDao.read(textoLimpio);
        if (porId != null) {
            return List.of(porId);
        }
        return funcionarioDao.findByNombre(textoLimpio);
    }

    /**
     * Mismo criterio que guardarRecurso. El id no puede chocar ni con otro
     * funcionario ni con un administrador (comparten el login). Un
     * funcionario nuevo arranca con su id como clave; uno editado conserva
     * la clave que trae.
     */
    public void guardarFuncionario(Funcionario funcionario, boolean esNuevo) throws Exception {
        boolean idYaExiste = funcionarioDao.read(funcionario.getId()) != null
                || administradorDao.read(funcionario.getId()) != null;
        Optional<String> errorId = ValidadorId.validarIdParaCrear(esNuevo, idYaExiste, funcionario.getId());
        if (errorId.isPresent()) {
            throw new Exception(errorId.get());
        }
        if (esNuevo) {
            funcionario.setClave(funcionario.getId());
            funcionarioDao.create(funcionario);
        } else {
            funcionarioDao.update(funcionario);
        }
    }

    /** Todas las reservas del usuario de la sesión, también las canceladas. */
    public List<Reserva> listarMisReservas(String sesionId) throws Exception {
        Usuario usuario = usuarioDeSesion(sesionId);
        return reservaDao.findByFuncionario(usuario.getId());
    }

    /** Todas las reservas con fecha entre desde y hasta (inclusive), sin filtrar por estado. */
    public List<Reserva> listarReservasEntre(LocalDate desde, LocalDate hasta) {
        return reservaDao.findEntre(desde, hasta);
    }

    /**
     * Misma lógica que ReservaController.crear del proyecto viejo, con las
     * validaciones de tiempo hechas con la fecha y hora del servidor.
     * synchronized: con un hilo por cliente, dos pedidos simultáneos podrían
     * ver el mismo recurso libre y reservarlo dos veces; así se atienden de
     * a uno y el segundo ya ve ocupado lo que tomó el primero.
     * La reserva queda a nombre del funcionario de la sesión, no de un id
     * que mande el cliente.
     */
    public synchronized Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                             String sesionId, List<CategoriaRecurso> categorias) throws Exception {
        Usuario usuario = usuarioDeSesion(sesionId);
        if (!(usuario instanceof Funcionario)) {
            throw new Exception("Solo los funcionarios pueden hacer reservas");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new Exception("La hora de fin debe ser posterior a la hora de inicio");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new Exception("La fecha no puede ser en el pasado");
        }
        if (fecha.equals(LocalDate.now()) && horaInicio.isBefore(LocalTime.now())) {
            throw new Exception("No podés reservar un horario de hoy que ya pasó");
        }
        // Se relee de la base: el funcionario pudo cambiar o borrarse después del login.
        Funcionario funcionario = funcionarioDao.read(usuario.getId());
        if (funcionario == null) {
            throw new Exception("El funcionario no existe");
        }

        Set<String> idsOcupados = ReglasReserva.recursosOcupados(
                reservaDao.findEntre(fecha, fecha), fecha, horaInicio, horaFin);

        ResultadoAsignacion resultado = new AsignadorRecursos().asignar(categorias, recursoDao.findAll(), idsOcupados);
        if (!resultado.categoriasSinDisponibilidad().isEmpty()) {
            throw new Exception("Sin disponibilidad para: " + String.join(", ", resultado.categoriasSinDisponibilidad()));
        }

        Reserva reserva = new Reserva(UUID.randomUUID().toString(), actividad, fecha, horaInicio, horaFin, funcionario);
        reserva.setRecursos(resultado.asignados());
        reservaDao.create(reserva);
        return reserva;
    }

    /**
     * Lee la reserva de la base (no confía en la copia del cliente), comprueba
     * que sea del usuario de la sesión (nadie cancela reservas ajenas, tampoco
     * un administrador) y aplica ReglasReserva con la fecha y hora del servidor.
     */
    public void cancelarReserva(String reservaId, String sesionId) throws Exception {
        Usuario usuario = usuarioDeSesion(sesionId);
        Reserva reserva = reservaDao.read(reservaId);
        if (reserva == null) {
            throw new Exception("La reserva no existe");
        }
        if (!reserva.getFuncionario().getId().equals(usuario.getId())) {
            throw new Exception("No podés cancelar una reserva de otro funcionario");
        }
        Optional<String> error = ReglasReserva.validarCancelacion(reserva, LocalDate.now(), LocalTime.now());
        if (error.isPresent()) {
            throw new Exception(error.get());
        }
        reservaDao.updateEstado(reservaId, EstadoReserva.CANCELADA);
    }

    /** Cuenta también las reservas canceladas, igual que el FuncionarioController viejo. */
    public void borrarFuncionario(String funcionarioId) throws Exception {
        if (!reservaDao.findByFuncionario(funcionarioId).isEmpty()) {
            throw new Exception("No se puede borrar: el funcionario tiene reservas asociadas");
        }
        funcionarioDao.delete(funcionarioId);
    }

    /** Usa las categorías de la base como lista de opciones para el modelo. */
    public DatosReservaIA extraerIA(String frase) throws Exception {
        return new IAService().extraer(frase, categoriaRecursoDao.findAll());
    }

    public ResultadoAutenticacion autenticar(String id, String clave) throws Exception {
        Usuario usuario = autenticacionService.autenticar(id, clave);
        if (usuario == null) {
            throw new Exception("Usuario o clave incorrectos");
        }
        String sesionId = UUID.randomUUID().toString();
        sesiones.put(sesionId, usuario);
        return new ResultadoAutenticacion(usuario, sesionId);
    }

    // Usuario que inició la sesión; si la sesión no existe, no se puede seguir.
    private Usuario usuarioDeSesion(String sesionId) throws Exception {
        Usuario usuario = sesionId == null ? null : sesiones.get(sesionId);
        if (usuario == null) {
            throw new Exception("Tenés que iniciar sesión");
        }
        return usuario;
    }

    public boolean sesionValida(String sesionId) {
        return sesionId != null && sesiones.containsKey(sesionId);
    }

    /** Se llama cuando se cierra la conexión de peticiones: borra la sesión y corta sus notificaciones. */
    public void cerrarSesion(String sesionId) {
        sesiones.remove(sesionId);
        Notificador.instance().quitar(sesionId);
    }

    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        Usuario usuario = autenticacionService.autenticar(id, claveActual);
        if (usuario == null) {
            throw new Exception("Clave actual incorrecta");
        }
        autenticacionService.cambiarClave(usuario, claveNueva);
    }
}
