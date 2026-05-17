package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Dominante;

public class DominanteDAO {

    public int create(Dominante dominante) {
        String sql = "INSERT INTO dominantes (code, name, responsible_name, description, color, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, new String[]{"ID"})
            ps.setString(1, dominante.getCode());
            ps.setString(2, dominante.getName());
            ps.setString(3, dominante.getResponsibleName());
            ps.setString(4, dominante.getDescription());
            ps.setString(5, dominante.getColor() != null ? dominante.getColor() : "#3b82f6");
            ps.setInt(6, dominante.isActive() ? 1 : 0);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("DominanteDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    public boolean update(Dominante dominante) {
        String sql = "UPDATE dominantes SET code = ?, name = ?, responsible_name = ?, description = ?, color = ?, is_active = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, dominante.getCode());
            ps.setString(2, dominante.getName());
            ps.setString(3, dominante.getResponsibleName());
            ps.setString(4, dominante.getDescription());
            ps.setString(5, dominante.getColor() != null ? dominante.getColor() : "#3b82f6");
            ps.setInt(6, dominante.isActive() ? 1 : 0);
            ps.setInt(7, dominante.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("DominanteDAO.update: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    public boolean deleteById(int dominanteId) {
        String sql = "DELETE FROM dominantes WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, dominanteId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("DominanteDAO.deleteById: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    public Dominante findById(int id) {
        String sql = "SELECT id, code, name, responsible_name, description, color, is_active FROM dominantes WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return null;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapDominante(rs);
            }
        } catch (Exception e) {
            System.err.println("DominanteDAO.findById: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return null;
    }

    public List<Dominante> findAll() {
        String sql = "SELECT id, code, name, responsible_name, description, color, is_active FROM dominantes ORDER BY name";
        List<Dominante> result = new ArrayList<Dominante>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapDominante(rs));
            }
        } catch (Exception e) {
            System.err.println("DominanteDAO.findAll: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    public List<Dominante> searchByName(String text) {
        String sql = "SELECT id, code, name, responsible_name, description, color, is_active FROM dominantes WHERE UPPER(name) LIKE UPPER(?) ORDER BY name";
        List<Dominante> result = new ArrayList<Dominante>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + text + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapDominante(rs));
            }
        } catch (Exception e) {
            System.err.println("DominanteDAO.searchByName: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    private Dominante mapDominante(ResultSet rs) throws Exception {
        return new Dominante(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("responsible_name"),
                rs.getString("description"),
                rs.getString("color"),
                rs.getInt("is_active") == 1
        );
    }

    private void close(AutoCloseable c) {
        if (c != null && !(c instanceof java.sql.Connection)) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
