package reservas.data.mysql;

import reservas.logic.Funcionario;
import reservas.logic.Rol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDao {
    Database db;

    public FuncionarioDao(){
        db = Database.instance();
    }

    public List<Funcionario> findAll(){
        List<Funcionario> funcionarios = new ArrayList<>();
        try {
            String sql = "select * from funcionario f";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                funcionarios.add(from(rs, "f"));
            }
        } catch (SQLException ex) { }
        return funcionarios;
    }

    public List<Funcionario> findByNombre(String nombre){
        List<Funcionario> funcionarios = new ArrayList<>();
        try {
            String sql = "select * from funcionario f where f.nombre like ?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, "%" + nombre + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                funcionarios.add(from(rs, "f"));
            }
        } catch (SQLException ex) { }
        return funcionarios;
    }

    public Funcionario read(String id){
        try {
            String sql = "select * from funcionario f where f.id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "f");
            }
        } catch (SQLException ex) { }
        return null;
    }

    public void create(Funcionario f){
        try {
            String sql = "insert into funcionario (id, clave, nombre, telefono) values (?, ?, ?, ?)";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, f.getId());
            stm.setString(2, f.getClave());
            stm.setString(3, f.getNombre());
            stm.setString(4, f.getTelefono());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void update(Funcionario f){
        try {
            String sql = "update funcionario set clave=?, nombre=?, telefono=? where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, f.getClave());
            stm.setString(2, f.getNombre());
            stm.setString(3, f.getTelefono());
            stm.setString(4, f.getId());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public void delete(String id){
        try {
            String sql = "delete from funcionario where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    public Funcionario from(ResultSet rs, String alias){
        try {
            Funcionario f = new Funcionario();
            f.setId(rs.getString(alias + ".id"));
            f.setClave(rs.getString(alias + ".clave"));
            f.setNombre(rs.getString(alias + ".nombre"));
            f.setTelefono(rs.getString(alias + ".telefono"));
            f.setRol(Rol.FUNCIONARIO);
            return f;
        } catch (SQLException ex) {
            return null;
        }
    }
}
