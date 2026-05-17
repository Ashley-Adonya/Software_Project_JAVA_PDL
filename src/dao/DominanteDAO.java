package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Dominante;

/**
 * Data Access Object for the Dominante entity.
 * <p>
 * This DAO handles all database operations on the {@code dominantes} table,
 * including full CRUD operations, searching by name, and retrieving all
 * records ordered by name.
 * </p>
 *
 * <p>The {@code dominantes} table stores academic focus areas (specialisations),
 * each with a unique code, display name, responsible person, description,
 * display colour, and an active flag.</p>
 */
public class DominanteDAO {

    /**
     * Inserts a new dominante (specialisation) into the {@code dominantes} table.
     * <p>
     * If the dominante's colour is {@code null}, a default blue colour
     * ({@code #3b82f6}) is used. On success the auto-generated primary key
     * is returned.
     * </p>
     *
     * @param dominante the Dominante entity containing code, name,
     *                  responsible name, description, colour, and active flag
     * @return the generated dominante ID on success, or {@code -1} if the
     *         connection could not be obtained or an error occurred
     */
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
            ps = conn.prepareStatement(sql);
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

    /**
     * Updates all fields of an existing dominante.
     * <p>
     * The dominante is identified by its {@code id} field. All other fields
     * (code, name, responsible name, description, colour, active flag) are
     * overwritten with the values from the provided Dominante object.
     * If the colour is {@code null}, the default {@code #3b82f6} is used.
     * </p>
     *
     * @param dominante the Dominante object containing the updated values;
     *                  must have a valid non-null {@code id}
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
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

    /**
     * Deletes a dominante by its primary key.
     * <p>
     * This is a hard delete. Any foreign-key references to this dominante
     * in the {@code sessions} table may prevent deletion or cause referential
     * integrity errors depending on the database configuration.
     * </p>
     *
     * @param dominanteId the ID of the dominante to delete
     * @return {@code true} if exactly one row was deleted, {@code false} otherwise
     */
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

    /**
     * Retrieves a dominante by its primary key.
     *
     * @param id the unique dominante identifier
     * @return the Dominante object if found, or {@code null} if no dominante
     *         exists with the given id, the connection failed, or an error occurred
     */
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

    /**
     * Retrieves all dominantes ordered by their display name.
     *
     * @return a list of all Dominante objects (never {@code null};
     *         empty if none exist or on error)
     */
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

    /**
     * Searches for dominantes whose name contains the given text
     * (case-insensitive).
     * <p>
     * The search uses {@code UPPER(name) LIKE UPPER('%text%')} so a match
     * is found if the search text appears anywhere within the dominante name.
     * Results are ordered by name.
     * </p>
     *
     * @param text the search substring; special SQL wildcard characters
     *             are automatically treated as literals because the method
     *             wraps the value with {@code %} markers
     * @return a list of matching Dominante objects (never {@code null};
     *         empty if none match or on error)
     */
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

    /**
     * Maps the current row of a ResultSet to a Dominante object using
     * <b>column index</b> order.
     * <p>
     * The expected column order (1-indexed) is:
     * <ol>
     *   <li>id</li>
     *   <li>code</li>
     *   <li>name</li>
     *   <li>responsible_name</li>
     *   <li>description</li>
     *   <li>color</li>
     *   <li>is_active</li>
     * </ol>
     * </p>
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a fully populated Dominante instance
     * @throws Exception if a column value cannot be read or the Dominante constructor fails
     */
    private Dominante mapDominante(ResultSet rs) throws Exception {
        return new Dominante(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getInt(7) == 1
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
