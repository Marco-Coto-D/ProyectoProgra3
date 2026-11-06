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
import java.util.Map;
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
    void cambiarClaveMuyLargaLanzaErrorYNoCambiaNada() {
        Exception e = assertThrows(Exception.class, () -> service.cambiarClave("TEST-F001", "clave123", "x".repeat(101)));
        assertEquals("La clave no puede tener más de 100 caracteres", e.getMessage());
        assertEquals("clave123", (new FuncionarioDao()).read("TEST-F001").getClave());
    }

    @Test
    void cambiarClaveAVaciaLanzaErrorYNoCambiaNada() {
        Exception e = assertThrows(Exception.class, () -> service.cambiarClave("TEST-F001", "clave123", "  "));
        assertEquals("La clave nueva no puede estar vacía", e.getMessage());
        assertEquals("clave123", (new FuncionarioDao()).read("TEST-F001").getClave());
    }

    @Test
    void administradorCambiaSuPropiaClave() throws Exception {
        service.cambiarClave("TEST-ADMIN", "admin123", "nueva#Admin");
        assertThrows(Exception.class, () -> service.autenticar("TEST-ADMIN", "admin123"));
        assertEquals(Rol.ADMINISTRADOR, service.autenticar("TEST-ADMIN", "nueva#Admin").usuario().getRol());
    }

    @Test
    void buscarCategoriasVacioDevuelveTodas() throws Exception {
        List<CategoriaRecurso> resultado = service.buscarCategorias("  ", sesionAdmin);
        assertTrue(resultado.stream().anyMatch(c -> c.getId().equals("TEST-CAT1")));
        assertEquals(service.listarCategorias().size(), resultado.size());
    }

    @Test
    void buscarCategoriasPorIdDevuelveSoloEsa() throws Exception {
        List<CategoriaRecurso> resultado = service.buscarCategorias("TEST-CAT1", sesionAdmin);
        assertEquals(1, resultado.size());
        assertEquals("TEST-Proyector portátil", ((CategoriaRecurso)resultado.get(0)).getDescripcion());
    }

    @Test
    void buscarCategoriasPorDescripcionParcialSinMayusculas() throws Exception {
        List<CategoriaRecurso> resultado = service.buscarCategorias("proyector PORT", sesionAdmin);
        assertTrue(resultado.stream().anyMatch(c -> c.getId().equals("TEST-CAT1")));
    }

    @Test
    void guardarCategoriaNuevaGeneraIdConsecutivo() throws Exception {
        CategoriaRecurso primera = new CategoriaRecurso(null, "TEST-Nueva uno");
        CategoriaRecurso segunda = new CategoriaRecurso(null, "TEST-Nueva dos");
        service.guardarCategoria(primera, sesionAdmin);
        service.guardarCategoria(segunda, sesionAdmin);
        assertTrue(primera.getId().matches("CAT-\\d{3}"));
        int numeroPrimera = Integer.parseInt(primera.getId().substring(4));
        assertEquals(String.format("CAT-%03d", numeroPrimera + 1), segunda.getId());
        assertEquals("TEST-Nueva uno", (service.buscarCategorias(primera.getId(), sesionAdmin).get(0)).getDescripcion());
    }

    @Test
    void guardarCategoriaExistenteActualiza() throws Exception {
        service.guardarCategoria(new CategoriaRecurso("TEST-CAT1", "TEST-Proyector de techo"), sesionAdmin);
        assertEquals("TEST-Proyector de techo", (service.buscarCategorias("TEST-CAT1", sesionAdmin).get(0)).getDescripcion());
    }

    @Test
    void guardarCategoriaNuevaConDescripcionMuyLargaLanzaErrorYNoGuarda() {
        int antes = service.listarCategorias().size();
        Exception e = assertThrows(Exception.class, () -> service.guardarCategoria(new CategoriaRecurso(null, "TEST-" + "x".repeat(146)), sesionAdmin));
        assertEquals("La descripción no puede tener más de 150 caracteres", e.getMessage());
        assertEquals(antes, service.listarCategorias().size());
    }

    @Test
    void guardarCategoriaExistenteConDescripcionMuyLargaLanzaErrorYNoCambia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarCategoria(new CategoriaRecurso("TEST-CAT1", "TEST-" + "x".repeat(146)), sesionAdmin));
        assertEquals("La descripción no puede tener más de 150 caracteres", e.getMessage());
        assertEquals("TEST-Proyector portátil", (service.buscarCategorias("TEST-CAT1", sesionAdmin).get(0)).getDescripcion());
    }

    @Test
    void borrarCategoriaConRecursosLanzaErrorYNoBorra() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.borrarCategoria("TEST-CAT1", sesionAdmin));
        assertEquals("No se puede borrar: hay recursos asociados a esta categoría", e.getMessage());
        assertEquals(1, service.buscarCategorias("TEST-CAT1", sesionAdmin).size());
    }

    @Test
    void borrarCategoriaSinRecursosLaBorra() throws Exception {
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT3", "TEST-Sin recursos");
        service.borrarCategoria("TEST-CAT3", sesionAdmin);
        assertTrue(service.listarCategorias().stream().noneMatch(c -> c.getId().equals("TEST-CAT3")));
    }

    @Test
    void listarRecursosTraeCategoriaCompleta() throws Exception {
        Recurso r1 = service.listarRecursos(sesionAdmin).stream().filter(r -> r.getId().equals("TEST-R1")).findFirst().orElseThrow();
        assertEquals("TEST-Epson aula 12", r1.getDescripcion());
        assertEquals("TEST-CAT1", r1.getCategoria().getId());
        assertEquals("TEST-Proyector portátil", r1.getCategoria().getDescripcion());
    }

    @Test
    void buscarRecursosVacioDevuelveTodos() throws Exception {
        assertEquals(service.listarRecursos(sesionAdmin).size(), service.buscarRecursos("", sesionAdmin).size());
    }

    @Test
    void buscarRecursosPorIdDevuelveSoloEse() throws Exception {
        List<Recurso> resultado = service.buscarRecursos("TEST-R2", sesionAdmin);
        assertEquals(1, resultado.size());
        assertEquals("TEST-Lab de química", ((Recurso)resultado.get(0)).getDescripcion());
    }

    @Test
    void buscarRecursosPorDescripcionDelRecurso() throws Exception {
        List<Recurso> resultado = service.buscarRecursos("epson", sesionAdmin);
        assertTrue(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R1")));
        assertFalse(resultado.stream().anyMatch(r -> r.getId().equals("TEST-R2")));
    }

    @Test
    void buscarRecursosPorDescripcionDeLaCategoria() throws Exception {
        List<Recurso> resultado = service.buscarRecursos("laboratorio", sesionAdmin);
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
    void buscarRecursosPorCategoriaNullDevuelveTodos() throws Exception {
        assertEquals(service.listarRecursos(sesionAdmin).size(), service.buscarRecursosPorCategoria(null).size());
    }

    @Test
    void guardarRecursoNuevoLoCrea() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2", sesionAdmin).get(0);
        service.guardarRecurso(new Recurso("TEST-R3", "TEST-Microscopio", cat2), true, sesionAdmin);
        List<Recurso> resultado = service.buscarRecursos("TEST-R3", sesionAdmin);
        assertEquals(1, resultado.size());
        assertEquals("TEST-Microscopio", ((Recurso)resultado.get(0)).getDescripcion());
        assertEquals("TEST-CAT2", ((Recurso)resultado.get(0)).getCategoria().getId());
    }

    @Test
    void guardarRecursoNuevoConIdExistenteLanzaErrorYNoPisa() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2", sesionAdmin).get(0);
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-R1", "TEST-Otro", cat2), true, sesionAdmin));
        assertEquals("Ya existe un registro con el id TEST-R1", e.getMessage());
        Recurso r1 = service.buscarRecursos("TEST-R1", sesionAdmin).get(0);
        assertEquals("TEST-Epson aula 12", r1.getDescripcion());
        assertEquals("TEST-CAT1", r1.getCategoria().getId());
    }

    @Test
    void guardarRecursoConIdMuyLargoLanzaErrorYNoGuarda() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2", sesionAdmin).get(0);
        int antes = service.listarRecursos(sesionAdmin).size();
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-" + "x".repeat(46), "TEST-Microscopio", cat2), true, sesionAdmin));
        assertEquals("El id no puede tener más de 50 caracteres", e.getMessage());
        assertEquals(antes, service.listarRecursos(sesionAdmin).size());
    }

    @Test
    void guardarRecursoExistenteConDescripcionMuyLargaLanzaErrorYNoCambia() throws Exception {
        CategoriaRecurso cat1 = service.buscarCategorias("TEST-CAT1", sesionAdmin).get(0);
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-R1", "TEST-" + "x".repeat(146), cat1), false, sesionAdmin));
        assertEquals("La descripción no puede tener más de 150 caracteres", e.getMessage());
        assertEquals("TEST-Epson aula 12", service.buscarRecursos("TEST-R1", sesionAdmin).get(0).getDescripcion());
    }

    @Test
    void guardarRecursoExistenteActualizaDescripcionYCategoria() throws Exception {
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2", sesionAdmin).get(0);
        service.guardarRecurso(new Recurso("TEST-R1", "TEST-Epson movido", cat2), false, sesionAdmin);
        Recurso r1 = service.buscarRecursos("TEST-R1", sesionAdmin).get(0);
        assertEquals("TEST-Epson movido", r1.getDescripcion());
        assertEquals("TEST-CAT2", r1.getCategoria().getId());
    }

    @Test
    void listarFuncionariosTraeDatosCompletos() throws Exception {
        Funcionario f1 = service.listarFuncionarios(sesionAdmin).stream().filter(f -> f.getId().equals("TEST-F001")).findFirst().orElseThrow();
        assertEquals("Ana López", f1.getNombre());
        assertEquals("8888-0000", f1.getTelefono());
        assertEquals("clave123", f1.getClave());
        assertEquals(Rol.FUNCIONARIO, f1.getRol());
    }

    @Test
    void buscarFuncionariosVacioDevuelveTodos() throws Exception {
        assertEquals(service.listarFuncionarios(sesionAdmin).size(), service.buscarFuncionarios(" ", sesionAdmin).size());
    }

    @Test
    void buscarFuncionariosPorIdDevuelveSoloEse() throws Exception {
        List<Funcionario> resultado = service.buscarFuncionarios("TEST-F002", sesionAdmin);
        assertEquals(1, resultado.size());
        assertEquals("Bruno Pérez", ((Funcionario)resultado.get(0)).getNombre());
    }

    @Test
    void buscarFuncionariosPorNombreParcialSinMayusculas() throws Exception {
        List<Funcionario> resultado = service.buscarFuncionarios("ana lóp", sesionAdmin);
        assertTrue(resultado.stream().anyMatch(f -> f.getId().equals("TEST-F001")));
        assertFalse(resultado.stream().anyMatch(f -> f.getId().equals("TEST-F002")));
    }

    @Test
    void guardarFuncionarioNuevoUsaIdComoClave() throws Exception {
        service.guardarFuncionario(new Funcionario("TEST-F003", "loQueMandeElCliente", "Carla Ruiz", "8888-2222"), true, sesionAdmin);
        Funcionario f3 = service.buscarFuncionarios("TEST-F003", sesionAdmin).get(0);
        assertEquals("Carla Ruiz", f3.getNombre());
        assertEquals("TEST-F003", f3.getClave());
        assertNotNull(service.autenticar("TEST-F003", "TEST-F003"));
    }

    @Test
    void guardarFuncionarioNuevoConIdDeOtroFuncionarioLanzaError() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F001", "x", "Otro", "0000-0000"), true, sesionAdmin));
        assertEquals("Ya existe un registro con el id TEST-F001", e.getMessage());
        assertEquals("Ana López", (service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0)).getNombre());
    }

    @Test
    void guardarFuncionarioNuevoConIdDeAdministradorLanzaError() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-ADMIN", "x", "Otro", "0000-0000"), true, sesionAdmin));
        assertEquals("Ya existe un registro con el id TEST-ADMIN", e.getMessage());
        assertTrue(service.buscarFuncionarios("TEST-ADMIN", sesionAdmin).isEmpty());
    }

    @Test
    void guardarFuncionarioNuevoConTelefonoMuyLargoLanzaErrorYNoGuarda() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F003", null, "Carla Mora", "1".repeat(31)), true, sesionAdmin));
        assertEquals("El teléfono no puede tener más de 30 caracteres", e.getMessage());
        assertTrue(service.buscarFuncionarios("TEST-F003", sesionAdmin).isEmpty());
    }

    @Test
    void guardarFuncionarioNuevoConLetrasEnElTelefonoLanzaErrorYNoGuarda() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F003", null, "Carla Mora", "abc"), true, sesionAdmin));
        assertEquals("El teléfono solo puede tener números, espacios o guiones", e.getMessage());
        assertTrue(service.buscarFuncionarios("TEST-F003", sesionAdmin).isEmpty());
    }

    @Test
    void guardarFuncionarioExistenteConLetrasEnElTelefonoLanzaErrorYNoCambia() throws Exception {
        Funcionario f1 = service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0);
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F001", f1.getClave(), f1.getNombre(), "8888-OOOO"), false, sesionAdmin));
        assertEquals("El teléfono solo puede tener números, espacios o guiones", e.getMessage());
        assertEquals("8888-0000", service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0).getTelefono());
    }

    @Test
    void guardarFuncionarioExistenteConNombreMuyLargoLanzaErrorYNoCambia() throws Exception {
        Funcionario f1 = service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0);
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F001", f1.getClave(), "x".repeat(151), f1.getTelefono()), false, sesionAdmin));
        assertEquals("El nombre no puede tener más de 150 caracteres", e.getMessage());
        assertEquals("Ana López", service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0).getNombre());
    }

    @Test
    void guardarFuncionarioExistenteActualizaYConservaClave() throws Exception {
        Funcionario f1 = service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0);
        f1.setNombre("Ana López Mora");
        f1.setTelefono("7777-0000");
        service.guardarFuncionario(f1, false, sesionAdmin);
        Funcionario actualizado = service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0);
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
    void borrarRecursoConReservasLanzaErrorYNoBorra() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-R2", sesionAdmin));
        assertEquals("No se puede borrar: hay reservas asociadas a este recurso", e.getMessage());
        assertEquals(1, service.buscarRecursos("TEST-R2", sesionAdmin).size());
    }

    @Test
    void borrarRecursoConSoloReservasCanceladasTambienLanzaError() throws Exception {
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R4", "TEST-Solo cancelada", "TEST-CAT1");
        ejecutar("insert into reserva_recurso (reserva, recurso) values (?, ?)", "TEST-RES2", "TEST-R4");
        Exception e = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-R4", sesionAdmin));
        assertEquals("No se puede borrar: hay reservas asociadas a este recurso", e.getMessage());
    }

    @Test
    void borrarRecursoSinReservasLoBorra() throws Exception {
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R5", "TEST-Sin reservas", "TEST-CAT1");
        service.borrarRecurso("TEST-R5", sesionAdmin);
        assertTrue(service.listarRecursos(sesionAdmin).stream().noneMatch(r -> r.getId().equals("TEST-R5")));
    }

    @Test
    void borrarFuncionarioConReservasLanzaErrorYNoBorra() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-F001", sesionAdmin));
        assertEquals("No se puede borrar: el funcionario tiene reservas asociadas", e.getMessage());
        assertEquals(1, service.buscarFuncionarios("TEST-F001", sesionAdmin).size());
    }

    @Test
    void borrarFuncionarioConSoloReservasCanceladasTambienLanzaError() {
        Exception e = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-F002", sesionAdmin));
        assertEquals("No se puede borrar: el funcionario tiene reservas asociadas", e.getMessage());
    }

    @Test
    void borrarFuncionarioSinReservasLoBorra() throws Exception {
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F004", "x", "Diego Mora", "8888-4444");
        service.borrarFuncionario("TEST-F004", sesionAdmin);
        assertTrue(service.buscarFuncionarios("TEST-F004", sesionAdmin).isEmpty());
    }

    // mantenimiento de funcionarios, categorías y recursos: solo el administrador, aunque se hable directo por el protocolo
    private static final String SOLO_ADMIN = "Solo el administrador puede hacer esta operación";

    @Test
    void funcionarioNoPuedeListarNiBuscarFuncionarios() {
        Exception listar = assertThrows(Exception.class, () -> service.listarFuncionarios(sesionF1));
        Exception buscar = assertThrows(Exception.class, () -> service.buscarFuncionarios("TEST-F002", sesionF1));
        assertEquals(SOLO_ADMIN, listar.getMessage());
        assertEquals(SOLO_ADMIN, buscar.getMessage());
    }

    @Test
    void funcionarioNoPuedeCrearFuncionarioYNoSeGuarda() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F003", null, "Carla Ruiz", "8888-2222"), true, sesionF1));
        assertEquals(SOLO_ADMIN, e.getMessage());
        assertTrue(service.buscarFuncionarios("TEST-F003", sesionAdmin).isEmpty());
    }

    @Test
    void funcionarioNoPuedeEditarOtroFuncionarioYNoCambia() throws Exception {
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(new Funcionario("TEST-F002", "robada", "Otro nombre", "0000-0000"), false, sesionF1));
        assertEquals(SOLO_ADMIN, e.getMessage());
        Funcionario f2 = service.buscarFuncionarios("TEST-F002", sesionAdmin).get(0);
        assertEquals("Bruno Pérez", f2.getNombre());
        assertEquals("clave456", f2.getClave());
    }

    @Test
    void funcionarioNoPuedeBorrarFuncionarioYNoSeBorra() throws Exception {
        ejecutar("insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)", "TEST-F004", "x", "Diego Mora", "8888-4444");
        Exception e = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-F004", sesionF1));
        assertEquals(SOLO_ADMIN, e.getMessage());
        assertEquals(1, service.buscarFuncionarios("TEST-F004", sesionAdmin).size());
    }

    @Test
    void funcionarioNoPuedeCrearEditarNiBorrarCategorias() throws Exception {
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT3", "TEST-Sin recursos");
        int antes = service.listarCategorias().size();
        Exception crear = assertThrows(Exception.class, () -> service.guardarCategoria(new CategoriaRecurso(null, "TEST-Nueva"), sesionF1));
        Exception editar = assertThrows(Exception.class, () -> service.guardarCategoria(new CategoriaRecurso("TEST-CAT1", "TEST-Cambiada"), sesionF1));
        Exception borrar = assertThrows(Exception.class, () -> service.borrarCategoria("TEST-CAT3", sesionF1));
        Exception buscar = assertThrows(Exception.class, () -> service.buscarCategorias("TEST", sesionF1));
        assertEquals(SOLO_ADMIN, crear.getMessage());
        assertEquals(SOLO_ADMIN, editar.getMessage());
        assertEquals(SOLO_ADMIN, borrar.getMessage());
        assertEquals(SOLO_ADMIN, buscar.getMessage());
        assertEquals(antes, service.listarCategorias().size());
        assertEquals("TEST-Proyector portátil", service.buscarCategorias("TEST-CAT1", sesionAdmin).get(0).getDescripcion());
    }

    @Test
    void funcionarioNoPuedeCrearEditarNiBorrarRecursos() throws Exception {
        ejecutar("insert into recurso (id, descripcion, categoria) values (?, ?, ?)", "TEST-R5", "TEST-Sin reservas", "TEST-CAT1");
        CategoriaRecurso cat2 = service.buscarCategorias("TEST-CAT2", sesionAdmin).get(0);
        Exception crear = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-R3", "TEST-Microscopio", cat2), true, sesionF1));
        Exception editar = assertThrows(Exception.class, () -> service.guardarRecurso(new Recurso("TEST-R1", "TEST-Cambiado", cat2), false, sesionF1));
        Exception borrar = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-R5", sesionF1));
        Exception listar = assertThrows(Exception.class, () -> service.listarRecursos(sesionF1));
        Exception buscar = assertThrows(Exception.class, () -> service.buscarRecursos("TEST", sesionF1));
        assertEquals(SOLO_ADMIN, crear.getMessage());
        assertEquals(SOLO_ADMIN, editar.getMessage());
        assertEquals(SOLO_ADMIN, borrar.getMessage());
        assertEquals(SOLO_ADMIN, listar.getMessage());
        assertEquals(SOLO_ADMIN, buscar.getMessage());
        assertTrue(service.buscarRecursos("TEST-R3", sesionAdmin).isEmpty());
        assertEquals("TEST-Epson aula 12", service.buscarRecursos("TEST-R1", sesionAdmin).get(0).getDescripcion());
        assertEquals(1, service.buscarRecursos("TEST-R5", sesionAdmin).size());
    }

    @Test
    void operacionDeAdministradorSinSesionPideIniciarSesion() {
        Exception e = assertThrows(Exception.class, () -> service.listarFuncionarios("sesion-inventada"));
        assertEquals("Tenés que iniciar sesión", e.getMessage());
    }

    @Test
    void funcionarioSigueTeniendoLasConsultasDeSusPantallas() throws Exception {
        // Reservas, Calendarización, Programación y Estadísticas
        assertTrue(service.listarCategorias().stream().anyMatch(c -> c.getId().equals("TEST-CAT1")));
        assertEquals(List.of("TEST-R1"), service.buscarRecursosPorCategoria("TEST-CAT1").stream().map(Recurso::getId).toList());
        assertTrue(service.listarReservasEntre(LocalDate.of(2030, 1, 7), LocalDate.of(2030, 1, 9)).size() >= 2);
        assertEquals(2, service.listarMisReservas(sesionF1).size());
    }

    private List<CategoriaRecurso> categoria(String id) throws Exception {
        return List.of(service.buscarCategorias(id, sesionAdmin).get(0));
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
    void crearReservaConActividadMuyLargaLanzaErrorYNoGuarda() throws Exception {
        int antes = service.listarMisReservas(sesionF2).size();
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-" + "x".repeat(196), LocalDate.of(2030, 3, 4), LocalTime.of(9, 0), LocalTime.of(10, 0), sesionF2, categoria("TEST-CAT2")));
        assertEquals("La actividad no puede tener más de 200 caracteres", e.getMessage());
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
    void crearReservaSinCategoriasLanzaErrorYNoGuarda() throws Exception {
        int antes = service.listarMisReservas(sesionF2).size();
        Exception e = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", LocalDate.of(2030, 3, 4), LocalTime.of(9, 0), LocalTime.of(10, 0), sesionF2, List.of()));
        assertEquals("Completá actividad, fecha, horario y al menos una categoría", e.getMessage());
        assertEquals(antes, service.listarMisReservas(sesionF2).size());
    }

    @Test
    void crearReservaConActividadEnBlancoOSinFechaLanzaError() {
        Exception blanco = assertThrows(Exception.class, () -> service.crearReserva("   ", LocalDate.of(2030, 3, 4), LocalTime.of(9, 0), LocalTime.of(10, 0), sesionF2, categoria("TEST-CAT2")));
        Exception sinFecha = assertThrows(Exception.class, () -> service.crearReserva("TEST-X", null, LocalTime.of(9, 0), LocalTime.of(10, 0), sesionF2, categoria("TEST-CAT2")));
        assertEquals("Completá actividad, fecha, horario y al menos una categoría", blanco.getMessage());
        assertEquals("Completá actividad, fecha, horario y al menos una categoría", sinFecha.getMessage());
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

    // ---------- errores de la base que antes se perdían en silencio ----------

    @Test
    void guardarRecursoConCategoriaInexistenteLanzaErrorYNoGuarda() throws Exception {
        Recurso nuevo = new Recurso("TEST-R9", "TEST-Sin categoría", new CategoriaRecurso("TEST-CAT-BORRADA", "Borrada"));
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(nuevo, true, sesionAdmin));
        assertEquals("La categoría seleccionada ya no existe", e.getMessage());
        assertTrue(service.buscarRecursos("TEST-R9", sesionAdmin).stream().noneMatch(r -> r.getId().equals("TEST-R9")));
    }

    @Test
    void editarRecursoAUnaCategoriaInexistenteLanzaErrorYNoCambia() throws Exception {
        Recurso editado = new Recurso("TEST-R1", "TEST-Otra descripción", new CategoriaRecurso("TEST-CAT-BORRADA", "Borrada"));
        Exception e = assertThrows(Exception.class, () -> service.guardarRecurso(editado, false, sesionAdmin));
        assertEquals("La categoría seleccionada ya no existe", e.getMessage());
        Recurso guardado = service.buscarRecursos("TEST-R1", sesionAdmin).get(0);
        assertEquals("TEST-Epson aula 12", guardado.getDescripcion());
        assertEquals("TEST-CAT1", guardado.getCategoria().getId());
    }

    @Test
    void recursoDaoConCategoriaInexistenteYaNoSeDaPorGuardado() {
        Recurso nuevo = new Recurso("TEST-R9", "TEST-Sin categoría", new CategoriaRecurso("TEST-CAT-BORRADA", "Borrada"));
        Exception e = assertThrows(Exception.class, () -> new reservas.data.mysql.RecursoDao().create(nuevo));
        assertEquals("No se pudo guardar el recurso", e.getMessage());
        assertNull(new reservas.data.mysql.RecursoDao().read("TEST-R9"));
    }

    @Test
    void editarSinCambiosNoDaError() throws Exception {
        Recurso recurso = service.buscarRecursos("TEST-R1", sesionAdmin).get(0);
        assertDoesNotThrow(() -> service.guardarRecurso(recurso, false, sesionAdmin));

        Funcionario funcionario = service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0);
        assertDoesNotThrow(() -> service.guardarFuncionario(funcionario, false, sesionAdmin));

        CategoriaRecurso categoria = service.buscarCategorias("TEST-CAT1", sesionAdmin).get(0);
        assertDoesNotThrow(() -> service.guardarCategoria(categoria, sesionAdmin));

        // misma clave que ya tenía
        assertDoesNotThrow(() -> service.cambiarClave("TEST-F001", "clave123", "clave123"));
        assertDoesNotThrow(() -> service.cambiarClave("TEST-ADMIN", "admin123", "admin123"));

        assertEquals("TEST-Epson aula 12", service.buscarRecursos("TEST-R1", sesionAdmin).get(0).getDescripcion());
        assertEquals("Ana López", service.buscarFuncionarios("TEST-F001", sesionAdmin).get(0).getNombre());
    }

    @Test
    void editarFuncionarioQueOtraVentanaBorroLanzaError() throws Exception {
        Funcionario funcionario = service.buscarFuncionarios("TEST-F002", sesionAdmin).get(0);
        ejecutar("delete from reserva_recurso where reserva = ?", "TEST-RES2");
        ejecutar("delete from reserva where id = ?", "TEST-RES2");
        ejecutar("delete from funcionario where id = ?", "TEST-F002");
        funcionario.setNombre("TEST-Otro nombre");
        Exception e = assertThrows(Exception.class, () -> service.guardarFuncionario(funcionario, false, sesionAdmin));
        assertEquals("El funcionario no existe", e.getMessage());
    }

    @Test
    void editarCategoriaQueOtraVentanaBorroLanzaError() throws Exception {
        ejecutar("insert into categoria_recurso (id, descripcion) values (?, ?)", "TEST-CAT9", "TEST-Temporal");
        CategoriaRecurso categoria = service.buscarCategorias("TEST-CAT9", sesionAdmin).get(0);
        ejecutar("delete from categoria_recurso where id = ?", "TEST-CAT9");
        categoria.setDescripcion("TEST-Renombrada");
        Exception e = assertThrows(Exception.class, () -> service.guardarCategoria(categoria, sesionAdmin));
        assertEquals("La categoría no existe", e.getMessage());
        assertTrue(service.buscarCategorias("TEST-Renombrada", sesionAdmin).isEmpty());
    }

    @Test
    void borrarLoQueYaNoExisteLanzaError() {
        Exception recurso = assertThrows(Exception.class, () -> service.borrarRecurso("TEST-NOEXISTE", sesionAdmin));
        Exception funcionario = assertThrows(Exception.class, () -> service.borrarFuncionario("TEST-NOEXISTE", sesionAdmin));
        Exception categoria = assertThrows(Exception.class, () -> service.borrarCategoria("TEST-NOEXISTE", sesionAdmin));
        assertEquals("No se pudo borrar el recurso", recurso.getMessage());
        assertEquals("No se pudo borrar el funcionario", funcionario.getMessage());
        assertEquals("No se pudo borrar la categoría", categoria.getMessage());
    }

    @Test
    void updateEstadoDeReservaInexistenteLanzaError() {
        Exception e = assertThrows(Exception.class,
                () -> new reservas.data.mysql.ReservaDao().updateEstado("TEST-NOEXISTE", EstadoReserva.CANCELADA));
        assertEquals("La reserva no existe", e.getMessage());
    }

    @Test
    void reservaDaoConUnRecursoQueFallaNoDejaNadaEnLaBase() throws Exception {
        Funcionario f = new FuncionarioDao().read("TEST-F001");
        Reserva reserva = new Reserva("TEST-RES-FALLA", "TEST-Falla", LocalDate.of(2030, 3, 4), LocalTime.of(9, 0), LocalTime.of(10, 0), f);
        // el primero se guarda bien, el segundo no existe y la llave foránea lo rechaza
        reserva.setRecursos(List.of(new Recurso("TEST-R1", "", null), new Recurso("TEST-NOEXISTE", "", null)));

        Exception e = assertThrows(Exception.class, () -> new reservas.data.mysql.ReservaDao().create(reserva));

        assertEquals("No se pudo guardar la reserva", e.getMessage());
        assertEquals(0, contar("select count(*) from reserva where id = ?", "TEST-RES-FALLA"));
        assertEquals(0, contar("select count(*) from reserva_recurso where reserva = ?", "TEST-RES-FALLA"));
    }

    // ---------- estadísticas ----------

    @Test
    void contarRecursosPorCategoriaCuentaSoloReservasActivas() throws Exception {
        // TEST-RES1 (activa) usa R1 y R2; TEST-RES2 está cancelada
        Map<String, Long> conteo = service.contarRecursosPorCategoria(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31));
        assertEquals(Map.of("TEST-Proyector portátil", 1L, "TEST-Laboratorio", 1L), conteo);
    }

    @Test
    void contarRecursosPorCategoriaUsaLaDescripcionActual() throws Exception {
        ejecutar("update categoria_recurso set descripcion = ? where id = ?", "TEST-Laboratorio renombrado", "TEST-CAT2");
        Map<String, Long> conteo = service.contarRecursosPorCategoria(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31));
        assertEquals(1L, conteo.get("TEST-Laboratorio renombrado"));
    }

    @Test
    void contarActividadesPorSemanaIncluyeSemanasVaciasYNoCuentaCanceladas() throws Exception {
        Map<String, Long> conteo = service.contarActividadesPorSemana(LocalDate.of(2030, 1, 6), LocalDate.of(2030, 1, 20));
        assertEquals(List.of("2029-12-31", "2030-01-07", "2030-01-14"), List.copyOf(conteo.keySet()));
        assertEquals(0L, conteo.get("2029-12-31"));
        assertEquals(1L, conteo.get("2030-01-07"));
        assertEquals(0L, conteo.get("2030-01-14"));
    }

    @Test
    void estadisticasConPeriodoAlRevesOIncompletoLanzanError() {
        LocalDate d = LocalDate.of(2030, 1, 31), h = LocalDate.of(2030, 1, 1);
        assertEquals("'Hasta' debe ser posterior a 'Desde'", assertThrows(Exception.class, () -> service.contarRecursosPorCategoria(d, h)).getMessage());
        assertEquals("'Hasta' debe ser posterior a 'Desde'", assertThrows(Exception.class, () -> service.contarActividadesPorSemana(d, h)).getMessage());
        assertEquals("Seleccioná ambas fechas", assertThrows(Exception.class, () -> service.contarRecursosPorCategoria(null, h)).getMessage());
        assertEquals("Seleccioná ambas fechas", assertThrows(Exception.class, () -> service.contarActividadesPorSemana(d, null)).getMessage());
    }

    private long contar(String sql, String parametro) throws Exception {
        PreparedStatement stm = Database.instance().prepareStatement(sql);
        stm.setString(1, parametro);
        java.sql.ResultSet rs = stm.executeQuery();
        rs.next();
        return rs.getLong(1);
    }

    private void insertarReserva(String id,String actividad, String fecha, String horaInicio, String horaFin, String funcionario, String estado) throws Exception {
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
