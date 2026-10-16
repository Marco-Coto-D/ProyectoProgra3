package reservas.data.mysql;

import reservas.logic.EstadoReserva;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDao {
    Database db;
    FuncionarioDao funcionarioDao;
    RecursoDao recursoDao;

    public ReservaDao(){
        db = Database.instance();
        funcionarioDao = new FuncionarioDao();
        recursoDao = new RecursoDao();
    }

    public Reserva read(String id){
        Reserva reserva = null;
        try {
            String sql = "select * from reserva re inner join funcionario f on re.funcionario=f.id where re.id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                reserva = from(rs, "re");
            }
        } catch (SQLException ex) { }
        if (reserva != null) {
            reserva.setRecursos(findRecursos(reserva.getId()));
        }
        return reserva;
    }

    public List<Reserva> findAll(){
        List<Reserva> reservas = new ArrayList<>();
        try {
            String sql = "select * from reserva re inner join funcionario f on re.funcionario=f.id " +
                    "order by re.fecha, re.hora_inicio";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                reservas.add(from(rs, "re"));
            }
        } catch (SQLException ex) { }
        cargarRecursos(reservas);
        return reservas;
    }

    public List<Reserva> findByFuncionario(String funcionarioId){
        List<Reserva> reservas = new ArrayList<>();
        try {
            String sql = "select * from reserva re inner join funcionario f on re.funcionario=f.id " +
                    "where re.funcionario=? order by re.fecha, re.hora_inicio";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, funcionarioId);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                reservas.add(from(rs, "re"));
            }
        } catch (SQLException ex) { }
        cargarRecursos(reservas);
        return reservas;
    }

    public List<Reserva> findEntre(LocalDate desde, LocalDate hasta){
        List<Reserva> reservas = new ArrayList<>();
        try {
            String sql = "select * from reserva re inner join funcionario f on re.funcionario=f.id " +
                    "where re.fecha between ? and ? order by re.fecha, re.hora_inicio";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setObject(1, desde);
            stm.setObject(2, hasta);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                reservas.add(from(rs, "re"));
            }
        } catch (SQLException ex) { }
        cargarRecursos(reservas);
        return reservas;
    }

    public void create(Reserva r){
        try {
            String sql = "insert into reserva (id, actividad, fecha, hora_inicio, hora_fin, funcionario, estado) " +
                    "values (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, r.getId());
            stm.setString(2, r.getActividad());
            stm.setObject(3, r.getFecha());
            stm.setObject(4, r.getHoraInicio());
            stm.setObject(5, r.getHoraFin());
            stm.setString(6, r.getFuncionario().getId());
            stm.setString(7, r.getEstado().name());
            db.executeUpdate(stm);

            for (Recurso recurso : r.getRecursos()) {
                String sqlRecurso = "insert into reserva_recurso (reserva, recurso) values (?, ?)";
                PreparedStatement stmRecurso = db.prepareStatement(sqlRecurso);
                stmRecurso.setString(1, r.getId());
                stmRecurso.setString(2, recurso.getId());
                db.executeUpdate(stmRecurso);
            }
        } catch (SQLException ex) { }
    }

    public void updateEstado(String id, EstadoReserva estado){
        try {
            String sql = "update reserva set estado=? where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, estado.name());
            stm.setString(2, id);
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public List<Recurso> findRecursos(String reservaId){
        List<Recurso> recursos = new ArrayList<>();
        try {
            String sql = "select * from reserva_recurso rr " +
                    "inner join recurso r on rr.recurso=r.id " +
                    "inner join categoria_recurso c on r.categoria=c.id " +
                    "where rr.reserva=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, reservaId);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                recursos.add(recursoDao.from(rs, "r"));
            }
        } catch (SQLException ex) { }
        return recursos;
    }

    private void cargarRecursos(List<Reserva> reservas){
        for (Reserva r : reservas) {
            r.setRecursos(findRecursos(r.getId()));
        }
    }

    private Reserva from(ResultSet rs, String alias){
        try {
            Reserva r = new Reserva();
            r.setId(rs.getString(alias + ".id"));
            r.setActividad(rs.getString(alias + ".actividad"));
            // getObject y no getDate pq con serverTimezone=UTC las fechas se corrian un dia
            r.setFecha(rs.getObject(alias + ".fecha", LocalDate.class));
            r.setHoraInicio(rs.getObject(alias + ".hora_inicio", LocalTime.class));
            r.setHoraFin(rs.getObject(alias + ".hora_fin", LocalTime.class));
            r.setEstado(EstadoReserva.valueOf(rs.getString(alias + ".estado")));
            r.setFuncionario(funcionarioDao.from(rs, "f"));
            return r;
        } catch (SQLException ex) {
            return null;
        }
    }
}
