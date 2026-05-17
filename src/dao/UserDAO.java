package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    public User findByLoginAndPassword(String login, String password) {
         String sql = "SELECT id, login, password, full_name, role, promo, is_active FROM users "
                + "WHERE login = ? AND password = ? AND is_active = 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return null;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, login);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findByLoginAndPassword: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT id, login, password, full_name, role, promo, is_active FROM users WHERE id = ?";
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
                return mapUser(rs);
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findById: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return null;
    }

    public List<User> findStudentsByPromo(String promo) {
        String sql = "SELECT id, login, password, full_name, role, promo, is_active FROM users "
                + "WHERE role = 'STUDENT' AND promo = ? ORDER BY full_name";
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return users;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, promo);
            rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findStudentsByPromo: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return users;
    }

    public List<User> findAdmins() {
        String sql = "SELECT id, login, password, full_name, role, promo, is_active FROM users "
                + "WHERE role = 'ADMIN' ORDER BY full_name";
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return users;
            }
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findAdmins: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return users;
    }

    public List<User> findStudentsWithoutRegistrationInCampaign(String promo, int campaignId, List<Integer> excludedIds) {
        StringBuilder sql = new StringBuilder("SELECT id, login, password, full_name, role, promo, is_active FROM users ");
        sql.append("WHERE role = 'STUDENT' AND promo = ? AND is_active = 1");
        if (excludedIds != null && !excludedIds.isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < excludedIds.size(); i++) {
                placeholders.append("?");
                if (i < excludedIds.size() - 1) placeholders.append(",");
            }
            sql.append(" AND id NOT IN (").append(placeholders).append(")");
        }
        sql.append(" ORDER BY full_name");
        
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return users;
            }
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, promo);
            int paramIndex = 2;
            if (excludedIds != null) {
                for (Integer id : excludedIds) {
                    ps.setInt(paramIndex++, id);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findStudentsWithoutRegistrationInCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return users;
    }

    public List<User> findAllStudentsByPromo(String promo) {
        String sql = "SELECT id, login, password, full_name, role, promo, is_active FROM users "
                + "WHERE role = 'STUDENT' AND promo = ? AND is_active = 1 ORDER BY full_name";
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return users;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, promo);
            rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findAllStudentsByPromo: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return users;
    }

    public int create(User user) {
        String sql = "INSERT INTO users (login, password, full_name, role, promo, is_active, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, new String[] { "ID" });
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPromo());
            ps.setInt(6, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("UserDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET login = ?, password = ?, full_name = ?, role = ?, promo = ?, is_active = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPromo());
            ps.setInt(6, user.isActive() ? 1 : 0);
            ps.setInt(7, user.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("UserDAO.update: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    public boolean deactivate(int userId) {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("UserDAO.deactivate: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    private User mapUser(ResultSet rs) throws Exception {
        return new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("promo"),
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
