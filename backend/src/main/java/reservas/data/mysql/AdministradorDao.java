package reservas.data.mysql;

import reservas.logic.Administrador;
import reservas.logic.Rol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdministradorDao {
    Database db;

    public AdministradorDao(){
        db = Database.instance();
    }

    public Administrador read(String id){
        try {
            String sql = "select * from administrador a where a.id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "a");
            }
        } catch (SQLException ex) { }
        return null;
    }

    public void update(Administrador a){
        try {
            String sql = "update administrador set clave=? where id=?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, a.getClave());
            stm.setString(2, a.getId());
            db.executeUpdate(stm);
        } catch (SQLException ex) { }
    }

    private Administrador from(ResultSet rs, String alias){
        try {
            Administrador a = new Administrador();
            a.setId(rs.getString(alias + ".id"));
            a.setClave(rs.getString(alias + ".clave"));
            a.setRol(Rol.ADMINISTRADOR);
            return a;
        } catch (SQLException ex) {
            return null;
        }
    }
}
