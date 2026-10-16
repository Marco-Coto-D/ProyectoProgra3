package reservas.data.mysql;

import reservas.logic.Recurso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecursoDao {
    Database db;
    CategoriaRecursoDao categoriaRecursoDao;

    public RecursoDao(){
        db = Database.instance();
        categoriaRecursoDao = new CategoriaRecursoDao();
    }

    public List<Recurso> findAll(){
        List<Recurso> recursos = new ArrayList<>();
        try {
            String sql = "select * from recurso r inner join categoria_recurso c on r.categoria=c.id";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                recursos.add(from(rs, "r"));
            }
        } catch (SQLException ex) { }
        return recursos;
    }

    public Recurso read(String id){
        try {
            String sql = "select * from recurso r inner join categoria_recurso c on r.categoria=c.id where r.id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "r");
            }
        } catch (SQLException ex) { }
        return null;
    }

    public List<Recurso> findByTexto(String texto){
        List<Recurso> recursos = new ArrayList<>();
        try {
            String sql = "select * from recurso r inner join categoria_recurso c on r.categoria=c.id " +
                    "where r.descripcion like ? or c.descripcion like ?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, "%" + texto + "%");
            stm.setString(2, "%" + texto + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                recursos.add(from(rs, "r"));
            }
        } catch (SQLException ex) { }
        return recursos;
    }

    public List<Recurso> findByCategoria(String categoriaId){
        List<Recurso> recursos = new ArrayList<>();
        try {
            String sql = "select * from recurso r inner join categoria_recurso c on r.categoria=c.id where r.categoria=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, categoriaId);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                recursos.add(from(rs, "r"));
            }
        } catch (SQLException ex) { }
        return recursos;
    }

    public void create(Recurso r){
        try {
            String sql = "insert into recurso (id, descripcion, categoria) values (?, ?, ?)";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, r.getId());
            stm.setString(2, r.getDescripcion());
            stm.setString(3, r.getCategoria().getId());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void update(Recurso r){
        try {
            String sql = "update recurso set descripcion=?, categoria=? where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, r.getDescripcion());
            stm.setString(2, r.getCategoria().getId());
            stm.setString(3, r.getId());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void delete(String id){
        try {
            String sql = "delete from recurso where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public Recurso from(ResultSet rs, String alias){
        try {
            Recurso r = new Recurso();
            r.setId(rs.getString(alias + ".id"));
            r.setDescripcion(rs.getString(alias + ".descripcion"));
            r.setCategoria(categoriaRecursoDao.from(rs, "c"));
            return r;
        } catch (SQLException ex) {
            return null;
        }
    }
}
