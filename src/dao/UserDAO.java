package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * Data Access Object for the User entity.
 * <p>
 * This DAO handles all database operations on the {@code users} table,
 * including user creation, retrieval by ID or credentials, role-based queries
 * (students by promo, all administrators), and status management (deactivation).
 * </p>
 *
 * <p>The {@code users} table stores login credentials, profile information
 * (full name, role, promo), and an active flag for every application user.
 * Supported roles are {@code ADMIN} and {@code STUDENT}.</p>
 */
public class UserDAO {

    /**
     * Authenticates a user by login and password.
     * <p>
     * Only active users ({@code is_active = 1}) are considered valid.
     * This method is intended for the application's login workflow.
     * </p>
     *
     * @param login    the user's login name
     * @param password the user's password (raw text, compared directly)
     * @return the matching User if found and active, or {@code null} if
     *         authentication fails, credentials are incorrect, the connection
     *         could not be obtained, or an error occurs
     */
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

    /**
     * Retrieves a user by their primary key.
     *
     * @param id the unique user identifier
     * @return the User object if found, or {@code null} if no user exists
     *         with the given id, the connection failed, or an error occurred
     */
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

    /**
     * Finds all student users belonging to a given promo, including inactive
     * ones.
     * <p>
     * Results are ordered by full name alphabetically. This method does
     * <b>not</b> filter by {@code is_active} — it returns every user whose
     * role is {@code STUDENT} and promo matches.
     * </p>
     *
     * @param promo the promo (graduating year) to filter by
     * @return a list of matching User objects (never {@code null};
     *         empty if none found or on error)
     */
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

    /**
     * Retrieves all users with the {@code ADMIN} role, ordered by full name.
     *
     * @return a list of all administrator users (never {@code null};
     *         empty if none exist or on error)
     */
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

    /**
     * Finds active students in a given promo who are <b>not</b> among a list
     * of excluded user IDs.
     * <p>
     * This method is typically used to retrieve students who have not yet
     * registered for a campaign. The {@code excludedIds} parameter should
     * contain IDs of students who have already submitted choices or
     * registrations. If {@code excludedIds} is {@code null} or empty, the
     * exclusion clause is omitted from the query.
     * </p>
     *
     * @param promo       the promo (graduating year) to filter by
     * @param campaignId  the campaign ID (included for query signature
     *                    consistency; not directly used in the WHERE clause)
     * @param excludedIds list of student IDs to exclude, or {@code null}/empty
     *                    to skip exclusion
     * @return a list of active Student User objects not in the exclusion list
     *         (never {@code null}; empty if none found or on error)
     */
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

    /**
     * Finds all <b>active</b> students in a given promo.
     * <p>
     * Unlike {@link #findStudentsByPromo(String)}, this method explicitly
     * filters by {@code is_active = 1}, returning only users whose accounts
     * are currently active.
     * </p>
     *
     * @param promo the promo (graduating year) to filter by
     * @return a list of active Student User objects (never {@code null};
     *         empty if none found or on error)
     */
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

    /**
     * Inserts a new user into the {@code users} table.
     * <p>
     * The {@code created_at} timestamp is set automatically by the database.
     * On success the auto-generated primary key is returned.
     * </p>
     *
     * @param user the User entity containing login, password, full name,
     *             role, promo, and active status
     * @return the generated user ID on success, or {@code -1} if the
     *         connection could not be obtained or an error occurred
     */
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

    /**
     * Updates all fields of an existing user.
     * <p>
     * The user is identified by its {@code id} field. All other fields
     * (login, password, full name, role, promo, active status) are overwritten
     * with the values from the provided User object.
     * </p>
     *
     * @param user the User object containing the updated values; must have
     *             a valid non-null {@code id}
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
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

    /**
     * Deactivates a user by setting their {@code is_active} flag to {@code 0}.
     * <p>
     * This is a soft-delete operation: the user row remains in the database
     * but the account will no longer be usable for authentication or queries
     * that filter on {@code is_active = 1}.
     * </p>
     *
     * @param userId the ID of the user to deactivate
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
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

    /**
     * Maps the current row of a ResultSet to a User object.
     * <p>
     * Expects the ResultSet to contain the following columns in any order:
     * id, login, password, full_name, role, promo, is_active.
     * </p>
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a fully populated User instance
     * @throws Exception if a column value cannot be read or the User constructor fails
     */
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

    /**
     * Closes an {@code AutoCloseable} resource silently.
     * <p>
     * Connections ({@code java.sql.Connection}) are deliberately <b>not</b>
     * closed by this helper because the project uses a shared static connection
     * managed by {@link ConnectionDAO}. Any other resource type
     * (PreparedStatement, ResultSet) is closed and any exception is swallowed.
     * </p>
     *
     * @param c the resource to close; may be {@code null}
     */
    private void close(AutoCloseable c) {
        if (c != null && !(c instanceof java.sql.Connection)) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
