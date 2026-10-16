package reservas.logic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservas.data.mysql.Database;
import reservas.data.mysql.FuncionarioDao;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class ServiceIT {

    private Service service;
    private String sesionF1;
    private String sesionF2;
    private String sesionAdmin;

    @BeforeEach
    void setUp() throws Exception {
        borrarDatosDePrueba();
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F001", "clave123", "Ana López", "8888-0000");
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F002", "clave456", "Bruno Pérez", "8888-1111");
        ejecutar("insert into administrador (id, clave) values (?, ?)", "TEST-ADMIN", "admin123");
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT1", "TEST-Proyector portátil");
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT2", "TEST-Laboratorio");
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R1", "TEST-Epson aula 12", "TEST-CAT1");
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R2", "TEST-Lab de química", "TEST-CAT2");
        // fechas en 2030 para que no se mezclen con reservas reales
        insertarReserva("TEST-RES1", "TEST-Clase de física", "2030-01-07", "08:00", "10:00", "TEST-F001", "ACTIVADA");
        ejecutar("insert into reserva_recurso (reserva, recurso) values (?, ?)", "TEST-RES1", "TEST-R1");
        ejecutar("insert into reserva_recurso (reserva, recurso) values (?, ?)", "TEST-RES1", "TEST-R2");
        insertarReserva("TEST-RES2", "TEST-Reunión", "2030-01-09", "14:00", "15:00", "TEST-F002", "CANCELADA");
        ejecutar("insert into reserva_recurso (reserva, recurso) values (?, ?)", "TEST-RES2", "TEST-R1");
        insertarReserva("TEST-RES3", "TEST-Taller", "2030-02-01", "09:00", "11:00", "TEST-F001", "ACTIVADA");
        service = Service.instance();
        sesionF1 = service.autenticar("TEST-F001", "clave123").sesionId();
        sesionF2 = service.autenticar("TEST-F002", "clave456").sesionId();
        sesionAdmin = service.autenticar("TEST-ADMIN", "admin123").sesionId();
    }

    @AfterEach
    void tearDown() throws Exception {
        service.cerrarSesion(sesionF1);
        service.cerrarSesion(sesionF2);
        service.cerrarSesion(sesionAdmin);
        borrarDatosDePrueba();
    }

    @Test
    void autenticarDevuelveUsuarioYSesionId() throws Exception {
        ResultadoAutenticacion resultado = service.autenticar("TEST-F001", "clave123");
        assertEquals("TEST-F001", resultado.usuario().getId());
        assertInstanceOf(Funcionario.class, resultado.usuario());
        assertNotNull(resultado.sesionId());
        assertFalse(resultado.sesionId().isBlank());
    }

    @Test
    void autenticarAdministrador() throws Exception {
        ResultadoAutenticacion resultado = service.autenticar("TEST-ADMIN", "admin123");
        assertInstanceOf(Administrador.class, resultado.usuario());
    }

    @Test
    void autenticarGeneraSesionIdDistintoCadaVez() throws Exception {
        String primero = service.autenticar("TEST-F001", "clave123").sesionId();
        String segundo = service.autenticar("TEST-F001", "clave123").sesionId();
        assertNotEquals(primero, segundo);
    }

    @Test
    void autenticarClaveIncorrectaLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.autenticar("TEST-F001", "claveMAL"));
        assertEquals("Usuario o clave incorrectos", e.getMessage());
    }

    @Test
    void cambiarClaveConClaveActualCorrecta() throws Exception {
        service.cambiarClave("TEST-F001", "clave123", "nuevaClave");
        assertEquals("nuevaClave", (new FuncionarioDao()).read("TEST-F001").getClave());
    }

    @Test
    void cambiarClaveConClaveActualIncorrectaNoCambiaNada() {
        Exception e = assertThrows(Exception.class, () -> service.cambiarClave("TEST-F001", "claveMAL", "nuevaClave"));
        assertEquals("Clave actual incorrecta", e.getMessage());
        assertEquals("clave123", (new FuncionarioDao()).read("TEST-F001").getClave());
    }

    @Test
    void buscarCategoriasVacioDevuelveTodas() {
        List<CategoriaRecurso> resultado = service.buscarCategorias("  ");
        assertTrue(resultado.stream().anyMatch(c -> c.getId().equals("TEST-CAT1")));
        assertEquals(service.listarCategorias().size(), resultado.size());
    }

    @Test
    void buscarCategoriasPorIdDevuelveSoloEsa() {
        List<CategoriaRecurso> resultado = service.buscarCategorias("TEST-CAT1");
        assertEquals(1, resultado.size());
        assertEquals("TEST-Proyector portátil", ((CategoriaRecurso)resultado.get(0)).getDescripcion());
    }

    @Test
    void buscarCategoriasPorDescripcionParcialSinMayusculas() {
        List<CategoriaRecurso> resultado = service.buscarCategorias("proyector PORT");
        assertTrue(resultado.stream().anyMatch(c -> c.getId().equals("TEST-CAT1")));
    }

    @Test
    void guardarCategoriaNuevaGeneraIdConsecutivo() {
        CategoriaRecurso primera = new CategoriaRecurso(null, "TEST-Nueva uno");
        CategoriaRecurso segunda = new CategoriaRecurso(null, "TEST-Nueva dos");
        service.guardarCategoria(primera);
        service.guardarCategoria(segunda);
        assertTrue(primera.getId().matches("CAT-\\d{3}"));
        int numeroPrimera = Integer.parseInt(primera.getId().substring(4));
        assertEquals(String.format("CAT-%03d", numeroPrimera + 1), segunda.getId());
        assertEquals("TEST-Nueva uno", (service.buscarCategorias(primera.getId()).get(0)).getDescripcion());
    }

    @Test
    void guardarCategoriaExistenteActualiza() {
        service.guardarCategoria(new CategoriaRecurso("TEST-CAT1", "TEST-Proyector de techo"));
        assertEquals("TEST-Proyector de techo", (service.buscarCategorias("TEST-CAT1").get(0)).getDescripcion());
    }

    @Test
    void borrarCategoriaConRecursosLanzaErrorYNoBorra() {
        Exception e = assertThrows(Exception.class, () -> service.borrarCategoria("TEST-CAT1"));
        assertEquals("No se puede borrar: hay recursos asociados a esta categoría", e.getMessage());
        assertEquals(1, service.buscarCategorias("TEST-CAT1").size());
    }

    @Test
    void borrarCategoriaSinRecursosLaBorra() throws Exception {
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT3", "TEST-Sin recursos");
        service.borrarCategoria("TEST-CAT3");
        assertTrue(service.listarCategorias().stream().noneMatch(c -> c.getId().equals("TEST-CAT3")));
    }

    @Test
    void listarRecursosTraeCategoriaCompleta() {
        Recurso r1 = service.listarRecursos().stream().filter(r -> r.getId().equals("TEST-R1")).findFirst().orElseThrow();
        assertEquals("TEST-Epson aula 12", r1.getDescripcion());
        assertEquals("TEST-CAT1", r1.getCategoria().getId());
        assertEquals("TEST-Proyector portátil", r1.getCategoria().getDescripcion());
    }

    @Test
    void buscarRecursosVacioDevuelveTodos() {
        assertEquals(service.listarRecursos().size(), service.buscarRecursos("").size());
    }

    @Test
    void buscarRecursosPorIdDevuelveSoloEse() {
        List<Recurso> resultado = service.buscarRecursos("TEST-R2");
        assertEquals(1, resultado.size());
        assertEquals("TEST-Lab de química", ((Recurso)resultado.get(0)).getDescripcion());
    }

    @Test
    void buscarRecursosPorDescripcionDelRecurso() {
        List<Recurso> resultado = service.buscarRecursos("epson");
        assertTrue(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R1")));
        assertFalse(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R2")));
    }

    @Test
    void buscarRecursosPorDescripcionDeLaCategoria() {
        List<Recurso> resultado = service.buscarRecursos("laboratorio");
        assertTrue(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R2")));
        assertFalse(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R1")));
    }

    @Test
    void buscarRecursosPorCategoriaFiltra() {
        List<Recurso> resultado = service.buscarRecursosPorCategoria("TEST-CAT1");
        assertEquals(1, resultado.size());
        assertEquals("TEST-R1", ((Recurso)resultado.get(0)).getId());
    }

    @Test
    void buscarRecursosPorCategoriaNullDevuelveTodos() {
        assertEquals(service.listarRecursos().size(), service.buscarRecursosPorCategoria(null).size());
    }

    @Test
    void guardarRecursoNuevoLoCrea() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2").get(0);
        service.guardarRecurso(new Recurso("TEST-R3", "TEST-Microscopio", cat2), true);
        List<Recurso> resultado = service.buscarRecursos("TEST-R3");
        assertEquals(1, resultado.size());
        assertEquals("TEST-Microscopio", ((Recurso)resultado.get(0)).getDescripcion());
        assertEquals("TEST-CAT2", ((Recurso)resultado.get(0)).getCategoria().getId());
    }

    @Test
    void guardarRecursoNuevoConIdExistenteLanzaErrorYNoPisa() {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2").get(0);
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-R1", "TEST-Otro", cat2), true));
        assertEquals("Ya existe un registro con el id TEST-R1", e.getMessage());
        Recurso r1 = service.buscarRecursos("TEST-R1").get(0);
        assertEquals("TEST-Epson aula 12", r1.getDescripcion());
        assertEquals("TEST-CAT1", r1.getCategoria().getId());
    }

    @Test
    void guardarRecursoExistenteActualizaDescripcionYCategoria() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2").get(0);
        service.guardarRecurso(new Recurso("TEST-R1", "TEST-Epson movido", cat2), false);
        Recurso r1 = service.buscarRecursos("TEST-R1").get(0);
        assertEquals("TEST-Epson movido", r1.getDescripcion());
        assertEquals("TEST-CAT2", r1.getCategoria().getId());
    }

    @Test
    void listarFuncionariosTraeDatosCompletos() {
        Funcionario f1 = service.listarFuncionarios().stream().filter(f -> f.getId().equals("TEST-F001")).findFirst().orElseThrow();
        assertEquals("Ana López", f1.getNombre());
        assertEquals("8888-0000", f1.getTelefono());
        assertEquals("clave123", f1.getClave());
        assertEquals(Rol.FUNCIONARIO, f1.getRol());
    }

    @Test
    void buscarFuncionariosVacioDevuelveTodos() {
        assertEquals(service.listarFuncionarios().size(), service.buscarFuncionarios(" ").size());
    }

    @Test
    void buscarFuncionariosPorIdDevuelveSoloEse() {
        List<Funcionario> resultado = service.buscarFuncionarios("TEST-F002");
        assertEquals(1, resultado.size());
        assertEquals("Bruno Pérez", ((Funcionario)resultado.get(0)).getNombre());
    }

    @Test
    void buscarFuncionariosPorNombreParcialSinMayusculas() {
        List<Funcionario> resultado = service.buscarFuncionarios("ana lóp");
        assertTrue(resultado.stream().anyMatch(f -> f.getId().equals("TEST-F001")));
        assertFalse(resultado.stream().anyMatch(f -> f.getId().equals("TEST-F002")));
    }

    @Test
    void guardarFuncionarioNuevoUsaIdComoClave() throws Exception {
        service.guardarFuncionario(new Funcionario("TEST-F003", "loQueMandeElCliente", "Carla Ruiz", "8888-2222"), true);
        Funcionario f3 = service.buscarFuncionarios("TEST-F003").get(0);
        assertEquals("Carla Ruiz", f3.getNombre());
        assertEquals("TEST-F003", f3.getClave());
        assertNotNull(service.autenticar("TEST-F003", "TEST-F003"));
    }

    @Test
    void guardarFuncionarioNuevoConIdDeOtroFuncionarioLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F001", "x", "Otro", "0000-0000"), true));
        assertEquals("Ya existe un registro con el id TEST-F001", e.getMessage());
        assertEquals("Ana López", (service.buscarFuncionarios("TEST-F001").get(0)).getNombre());
    }

    @Test
    void guardarFuncionarioNuevoConIdDeAdministradorLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-ADMIN", "x", "Otro", "0000-0000"), true));
        assertEquals("Ya existe un registro con el id TEST-ADMIN", e.getMessage());
        assertTrue(service.buscarFuncionarios("TEST-ADMIN").isEmpty());
    }

    @Test
    void guardarFuncionarioExistenteActualizaYConservaClave() throws Exception {
        Funcionario f1 = service.buscarFuncionarios("TEST-F001").get(0);
        f1.setNombre("Ana López Mora");
        f1.setTelefono("7777-0000");
        service.guardarFuncionario(f1, false);
        Funcionario actualizado = service.buscarFuncionarios("TEST-F001").get(0);
        assertEquals("Ana López Mora", actualizado.getNombre());
        assertEquals("7777-0000", actualizado.getTelefono());
        assertEquals("clave123", actualizado.getClave());
    }

    @Test
    void listarMisReservasTraeLasDelFuncionarioConTodo() throws Exception {
        List<Reserva> resultado = service.listarMisReservas(sesionF1);
        assertEquals(List.of("TEST-RES1", "TEST-RES3"), resultado.stream().map(Reserva::getId).toList());
        Reserva res1 = (Reserva)resultado.get(0);
        assertEquals("TEST-Clase de física", res1.getActividad());
        assertEquals(LocalDate.of(2030, 1, 7), res1.getFecha());
        assertEquals(LocalTime.of(8, 0), res1.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), res1.getHoraFin());
        assertEquals(EstadoReserva.ACTIVADA, res1.getEstado());
        assertEquals("Ana López", res1.getFuncionario().getNombre());
        assertEquals(List.of("TEST-R1", "TEST-R2"), res1.getRecursos().stream().map(Recurso::getId).sorted().toList());
        assertEquals("TEST-Proyector portátil", (res1.getRecursos().stream().filter(r -> r.getId().equals("TEST-R1")).findFirst().orElseThrow()).getCategoria().getDescripcion());
        assertTrue(((Reserva)resultado.get(1)).getRecursos().isEmpty());
    }

    @Test
    void listarMisReservasIncluyeCanceladas() throws Exception {
        List<Reserva> resultado = service.listarMisReservas(sesionF2);
        assertEquals(1, resultado.size());
        assertEquals(EstadoReserva.CANCELADA, ((Reserva)resultado.get(0)).getEstado());
    }

    @Test
    void listarMisReservasSinSesionLanzaError() {
        Exception sinSesion = assertThrows(Exception.class, () -> service.listarMisReservas(null));
        Exception inventada = assertThrows(Exception.class, () -> service.listarMisReservas("sesion-inventada"));
        assertEquals("Tenés que iniciar sesión", sinSesion.getMessage());
        assertEquals("Tenés que iniciar sesión", inventada.getMessage());
    }

    @Test
    void listarReservasEntreIncluyeExtremosYTodosLosEstados() {
        List<String> ids = service.listarReservasEntre(LocalDate.of(2030, 1, 7), LocalDate.of(2030, 1, 9)).stream().map(Reserva::getId).filter(id -> id.startsWith("TEST-")).toList();
        assertEquals(List.of("TEST-RES1", "TEST-RES2"), ids);
    }

    @Test
    void listarReservasEntreUnSoloDia() {
        List<Reserva> resultado = service.listarReservasEntre(LocalDate.of(2030, 2, 1), LocalDate.of(2030, 2, 1));
        assertTrue(resultado.stream().anyMatch(r -> r.getId().equals("TEST-RES3")));
        assertTrue(resultado.stream().noneMatch(r -> r.getId().equals("TEST-RES1")));
    }

    @Test
    void cancelarReservaFuturaLaCancela() throws Exception {
        service.cancelarReserva("TEST-RES1", sesionF1);
        Reserva res1 = service.listarMisReservas(sesionF1).stream().filter(r -> r.getId().equals("TEST-RES1")).findFirst().orElseThrow();
        assertEquals(EstadoReserva.CANCELADA, res1.getEstado());
    }

    @Test
    void cancelarReservaYaCanceladaLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES2", sesionF2));
        assertEquals("Esta reserva ya está cancelada", e.getMessage());
    }

    @Test
    void cancelarReservaPasadaLanzaErrorYNoCambiaEstado() throws Exception {
        insertarReserva("TEST-RES4", "TEST-Vieja", "2020-01-01", "08:00", "09:00", "TEST-F001", "ACTIVADA");
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES4", sesionF1));
        assertEquals("No se puede cancelar una reserva pasada", e.getMessage());
        Reserva res4 = service.listarMisReservas(sesionF1).stream().filter(r -> r.getId().equals("TEST-RES4")).findFirst().orElseThrow();
        assertEquals(EstadoReserva.ACTIVADA, res4.getEstado());
    }

    @Test
    void cancelarReservaQueYaInicioHoyLanzaError() throws Exception {
        // falla si se corre justo despues de medianoche
        LocalTime haceUnMinuto = LocalTime.now().minusMinutes(1L).withNano(0);
        insertarReserva("TEST-RES5", "TEST-En curso", LocalDate.now().toString(), haceUnMinuto.toString(), "23:59:59", "TEST-F001", "ACTIVADA");
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES5", sesionF1));
        assertEquals("Esta reserva ya inició, no se puede cancelar", e.getMessage());
    }

    @Test
    void cancelarReservaInexistenteLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-NOEXISTE", sesionF1));
        assertEquals("La reserva no existe", e.getMessage());
    }

    @Test
    void cancelarReservaDeOtroFuncionarioLanzaErrorYNoLaCancela() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES1", sesionF2));
        assertEquals("No podés cancelar una reserva de otro funcionario", e.getMessage());
        Reserva res1 = service.listarMisReservas(sesionF1).stream().filter(r -> r.getId().equals("TEST-RES1")).findFirst().orElseThrow();
        assertEquals(EstadoReserva.ACTIVADA, res1.getEstado());
    }

    @Test
    void cancelarReservaComoAdministradorTambienSeBloquea() {
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES1", sesionAdmin));
        assertEquals("No podés cancelar una reserva de otro funcionario", e.getMessage());
    }

    @Test
    void cancelarReservaSinSesionLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.cancelarReserva("TEST-RES1", "sesion-inventada"));
        assertEquals("Tenés que iniciar sesión", e.getMessage());
    }

    @Test
    void borrarRecursoConReservasLanzaErrorYNoBorra() {
        Exception e = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-R2"));
        assertEquals("No se puede borrar: hay reservas asociadas a este recurso", e.getMessage());
        assertEquals(1, service.buscarRecursos("TEST-R2").size());
    }

    @Test
    void borrarRecursoConSoloReservasCanceladasTambienLanzaError() throws Exception {
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R4", "TEST-Solo cancelada", "TEST-CAT1");
        ejecutar("insert into reserva_recurso (reserva, recurso) values (?, ?)", "TEST-RES2", "TEST-R4");
        Exception e = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-R4"));
        assertEquals("No se puede borrar: hay reservas asociadas a este recurso", e.getMessage());
    }

    @Test
    void borrarRecursoSinReservasLoBorra() throws Exception {
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R5", "TEST-Sin reservas", "TEST-CAT1");
        service.borrarRecurso("TEST-R5");
        assertTrue(service.listarRecursos().stream().noneMatch(r -> r.getId().equals("TEST-R5")));
    }

    @Test
    void borrarFuncionarioConReservasLanzaErrorYNoBorra() {
        Exception e = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-F001"));
        assertEquals("No se puede borrar: el funcionario tiene reservas asociadas", e.getMessage());
        assertEquals(1, service.buscarFuncionarios("TEST-F001").size());
    }

    @Test
    void borrarFuncionarioConSoloReservasCanceladasTambienLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-F002"));
        assertEquals("No se puede borrar: el funcionario tiene reservas asociadas", e.getMessage());
    }

    @Test
    void borrarFuncionarioSinReservasLoBorra() throws Exception {
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F004", "x", "Diego Mora", "8888-4444");
        service.borrarFuncionario("TEST-F004");
        assertTrue(service.buscarFuncionarios("TEST-F004").isEmpty());
    }

    private List<CategoriaRecurso> categoria(String id) {
        return List.of(service.buscarCategorias(id).get(0));
    }

    @Test
    void crearReservaAsignaRecursoLibreYLaGuarda() throws Exception {
        Reserva creada = service.crearReserva("TEST-Nueva", LocalDate.of(2030, 1, 7), LocalTime.of(10, 0), LocalTime.of(11, 0), sesionF1, categoria("TEST-CAT1"));
        assertNotNull(creada.getId());
        assertEquals(EstadoReserva.ACTIVADA, creada.getEstado());
        assertEquals(List.of("TEST-R1"), creada.getRecursos().stream().map(Recurso::getId).toList());
        Reserva guardada = service.listarMisReservas(sesionF1).stream().filter(r -> r.getId().equals(creada.getId())).findFirst().orElseThrow();
        assertEquals("TEST-Nueva", guardada.getActividad());
        assertEquals(List.of("TEST-R1"), guardada.getRecursos().stream().map(Recurso::getId).toList());
    }

    @Test
    void crearReservaSinDisponibilidadLanzaErrorYNoGuarda() throws Exception {
        int antes = service.listarMisReservas(sesionF2).size();
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-Choque", LocalDate.of(2030, 1, 7), LocalTime.of(9, 0), LocalTime.of(9, 30), sesionF2, categoria("TEST-CAT1")));
        assertEquals("Sin disponibilidad para: TEST-Proyector portátil", e.getMessage());
        assertEquals(antes, service.listarMisReservas(sesionF2).size());
    }

    @Test
    void crearReservaIgnoraReservasCanceladas() throws Exception {
        Reserva creada = service.crearReserva("TEST-Libre", LocalDate.of(2030, 1, 9), LocalTime.of(14, 0), LocalTime.of(15, 0), sesionF2, categoria("TEST-CAT1"));
        assertEquals(List.of("TEST-R1"), creada.getRecursos().stream().map(Recurso::getId).toList());
    }

    @Test
    void crearReservaConFinAntesDeInicioLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2030, 1, 7), LocalTime.of(11, 0), LocalTime.of(11, 0), sesionF1, categoria("TEST-CAT1")));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio", e.getMessage());
    }

    @Test
    void crearReservaEnFechaPasadaLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2020, 1, 1), LocalTime.of(8, 0), LocalTime.of(9, 0), sesionF1, categoria("TEST-CAT1")));
        assertEquals("La fecha no puede ser en el pasado", e.getMessage());
    }

    @Test
    void crearReservaHoyEnHorarioPasadoLanzaError() {
        // falla si se corre justo despues de medianoche
        LocalTime haceUnMinuto = LocalTime.now().minusMinutes(1L);
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.now(), haceUnMinuto, LocalTime.of(23, 59), sesionF1, categoria("TEST-CAT1")));
        assertEquals("No podés reservar un horario de hoy que ya pasó", e.getMessage());
    }

    @Test
    void crearReservaConFuncionarioDeLaSesionBorradoLanzaError() throws Exception {
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F009", "x", "Borrado", "0");
        String sesion = service.autenticar("TEST-F009", "x").sesionId();
        ejecutar("delete from funcionario where id = ?", "TEST-F009");

        try {
            Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2030, 1, 7), LocalTime.of(12, 0), LocalTime.of(13, 0), sesion, categoria("TEST-CAT1")));
            assertEquals("El funcionario no existe", e.getMessage());
        } finally {
            service.cerrarSesion(sesion);
        }

    }

    @Test
    void crearReservaQuedaANombreDelFuncionarioDeLaSesion() throws Exception {
        Reserva creada = service.crearReserva("TEST-De Bruno", LocalDate.of(2030, 1, 10), LocalTime.of(8, 0), LocalTime.of(9, 0), sesionF2, categoria("TEST-CAT1"));
        assertEquals("TEST-F002", creada.getFuncionario().getId());
        assertTrue(service.listarMisReservas(sesionF2).stream().anyMatch(r -> r.getId().equals(creada.getId())));
        assertTrue(service.listarMisReservas(sesionF1).stream().noneMatch(r -> r.getId().equals(creada.getId())));
    }

    @Test
    void crearReservaComoAdministradorLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2030, 1, 10), LocalTime.of(8, 0), LocalTime.of(9, 0), sesionAdmin, categoria("TEST-CAT1")));
        assertEquals("Solo los funcionarios pueden hacer reservas", e.getMessage());
    }

    @Test
    void crearReservaSinSesionLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2030, 1, 10), LocalTime.of(8, 0), LocalTime.of(9, 0), "sesion-inventada", categoria("TEST-CAT1")));
        assertEquals("Tenés que iniciar sesión", e.getMessage());
    }

    @Test
    void crearReservaDosClientesALaVezPorElUnicoRecursoSoloUnoLoConsigue() throws Exception {
        List<CategoriaRecurso> cat1 = categoria("TEST-CAT1");
        CountDownLatch largada = new CountDownLatch(1);
        List<String> resultados = Collections.synchronizedList(new ArrayList<>());
        Runnable pedido = () -> {
            try {
                largada.await();
                service.crearReserva("TEST-Carrera", LocalDate.of(2030, 1, 8), LocalTime.of(8, 0), LocalTime.of(9, 0), sesionF1, cat1);
                resultados.add("ok");
            } catch (Exception e) {
                resultados.add(e.getMessage());
            }
        };
        Thread a = new Thread(pedido);
        Thread b = new Thread(pedido);
        a.start();
        b.start();
        largada.countDown();
        a.join();
        b.join();
        assertEquals(1L, resultados.stream().filter("ok"::equals).count());
        assertTrue(resultados.contains("Sin disponibilidad para: TEST-Proyector portátil"));
        long guardadas = service.listarReservasEntre(LocalDate.of(2030, 1, 8), LocalDate.of(2030, 1, 8)).stream().filter(r -> r.getActividad().equals("TEST-Carrera")).count();
        assertEquals(1L, guardadas);
    }

    private void insertarReserva(String id, String actividad, String fecha, String horaInicio, String horaFin, String funcionario, String estado) throws Exception {
        ejecutar("insert into reserva (id, actividad, fecha, hora_inicio, hora_fin, funcionario, estado) values (?, ?, ?, ?, ?, ?, ?)", id, actividad, fecha, horaInicio, horaFin, funcionario, estado);
    }

    private void borrarDatosDePrueba() throws Exception {
        ejecutar("delete from reserva_recurso where reserva like 'TEST-%' or recurso like 'TEST-%'");
        ejecutar("delete from reserva where id like 'TEST-%' or funcionario like 'TEST-%'");
        ejecutar("delete from recurso where id like 'TEST-%'");
        ejecutar("delete from funcionario where id like 'TEST-%'");
        ejecutar("delete from administrador where id like 'TEST-%'");
        ejecutar("delete from categoria_recurso where id like 'TEST-%' or descripcion like 'TEST-%'");
    }

    private void ejecutar(String sql, String... parametros) throws Exception {
        PreparedStatement stm = Database.instance().prepareStatement(sql);

        for(int i = 0; i < parametros.length; ++i) {
            stm.setString(i + 1, parametros[i]);
        }

        stm.executeUpdate();
    }
}
