package reservas.data.mysql;

import reservas.logic.CategoriaRecurso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRecursoDao {
    Database db;

    public CategoriaRecursoDao(){
        db = Database.instance();
    }

    public List<CategoriaRecurso> findAll(){
        List<CategoriaRecurso> categorias = new ArrayList<>();
        try {
            String sql = "select * from categoria_recurso c";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                categorias.add(from(rs, "c"));
            }
        } catch (SQLException ex) { }
        return categorias;
    }

    public CategoriaRecurso read(String id){
        try {
            String sql = "select * from categoria_recurso c where c.id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "c");
            }
        } catch (SQLException ex) { }
        return null;
    }

    public List<CategoriaRecurso> findByDescripcion(String descripcion){
        List<CategoriaRecurso> categorias = new ArrayList<>();
        try {
            String sql = "select * from categoria_recurso c where c.descripcion like ?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, "%" + descripcion + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                categorias.add(from(rs, "c"));
            }
        } catch (SQLException ex) { }
        return categorias;
    }

    public void create(CategoriaRecurso c){
        try {
            String sql = "insert into categoria_recurso (id, descripcion) values (?, ?)";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, c.getId());
            stm.setString(2, c.getDescripcion());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void update(CategoriaRecurso c){
        try {
            String sql = "update categoria_recurso set descripcion=? where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, c.getDescripcion());
            stm.setString(2, c.getId());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void delete(String id){
        try {
            String sql = "delete from categoria_recurso where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public CategoriaRecurso from(ResultSet rs, String alias){
        try {
            CategoriaRecurso c = new CategoriaRecurso();
            c.setId(rs.getString(alias + ".id"));
            c.setDescripcion(rs.getString(alias + ".descripcion"));
            return c;
        } catch (SQLException ex) {
            return null;
        }
    }
}
