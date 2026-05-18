package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.SessionSlot;

/**
 * Data Access Object for the SessionSlot entity.
 * <p>
 * This DAO handles all database operations on the {@code sessions} table,
 * including full CRUD operations, queries by campaign or dominante/time
 * filters, capacity management, and allocation statistics.
 * </p>
 *
 * <p>The {@code sessions} table stores individual session slots within a
 * campaign. Each session is associated with a specific dominante
 * (specialisation) and has a date, time range (start/end minute of the day),
 * room, title, and maximum capacity.</p>
 */
public class SessionDAO {

    /**
     * Inserts a new session slot into the {@code sessions} table.
     * <p>
     * The date is converted from a string to a {@code java.sql.Date} for
     * database storage. The {@code created_at} timestamp is set automatically.
     * On success the auto-generated primary key is returned.
     * </p>
     *
     * @param session the SessionSlot entity containing campaign ID, dominante ID,
     *                title, room, date, start/end minutes, capacity, and creator ID
     * @return the generated session ID on success, or {@code -1} if the
     *         connection could not be obtained or an error occurred
     */
    public int create(SessionSlot session) {
        String sql = "INSERT INTO sessions (campaign_id, dominante_id, title, room, session_date, start_minute, end_minute, capacity, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, new String[] { "ID" });
            ps.setInt(1, session.getCampaignId());
            ps.setInt(2, session.getDominanteId());
            ps.setString(3, session.getTitle());
            ps.setString(4, session.getRoom());
            java.sql.Date sqlDate = java.sql.Date.valueOf(session.getSessionDate()); 
            ps.setDate(5, sqlDate);
            ps.setInt(6, session.getStartMinute());
            ps.setInt(7, session.getEndMinute());
            ps.setInt(8, session.getCapacity());
            ps.setInt(9, session.getCreatedBy());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    /**
     * Updates the details of an existing session slot.
     * <p>
     * All fields except {@code campaign_id} and {@code created_by} can be
     * modified. The {@code updated_at} timestamp is set automatically.
     * </p>
     *
     * @param session the SessionSlot object containing the updated values;
     *                must have a valid non-null {@code id}
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
    public boolean update(SessionSlot session) {
        String sql = "UPDATE sessions SET dominante_id = ?, title = ?, room = ?, session_date = ?, start_minute = ?, end_minute = ?, capacity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, session.getDominanteId());
            ps.setString(2, session.getTitle());
            ps.setString(3, session.getRoom());
            ps.setString(4, session.getSessionDate());
            ps.setInt(5, session.getStartMinute());
            ps.setInt(6, session.getEndMinute());
            ps.setInt(7, session.getCapacity());
            ps.setInt(8, session.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("SessionDAO.update: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Deletes a session slot by its primary key.
     * <p>
     * This is a hard delete. Related rows in the {@code choices} and
     * {@code registrations} tables that reference this session may prevent
     * deletion depending on foreign-key constraints.
     * </p>
     *
     * @param sessionId the ID of the session to delete
     * @return {@code true} if exactly one row was deleted, {@code false} otherwise
     */
    public boolean deleteById(int sessionId) {
        String sql = "DELETE FROM sessions WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, sessionId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("SessionDAO.deleteById: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Retrieves a session slot by its primary key.
     *
     * @param id the unique session identifier
     * @return the SessionSlot if found, or {@code null} if no session exists
     *         with the given id, the connection failed, or an error occurred
     */
    public SessionSlot findById(int id) {
        String sql = "SELECT id, campaign_id, dominante_id, title, room, session_date, start_minute, end_minute, capacity, created_by FROM sessions WHERE id = ?";
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
                return mapSession(rs);
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.findById: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return null;
    }

    /**
     * Retrieves all session slots for a given campaign.
     * <p>
     * Results are ordered by session date, then start minute, then dominante ID.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @return a list of SessionSlot objects (never {@code null};
     *         empty if none exist or on error)
     */
    public List<SessionSlot> findByCampaign(int campaignId) {
        String sql = "SELECT id, campaign_id, dominante_id, title, room, session_date, start_minute, end_minute, capacity, created_by FROM sessions WHERE campaign_id = ? ORDER BY session_date, start_minute, dominante_id";
        List<SessionSlot> result = new ArrayList<SessionSlot>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapSession(rs));
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.findByCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Searches for sessions within a campaign that match a dominante name
     * pattern and fall within a specified time window.
     * <p>
     * The dominante name comparison is case-insensitive and uses a
     * {@code LIKE '%pattern%'} match via a JOIN with the {@code dominantes}
     * table. The time window is inclusive: sessions whose {@code start_minute}
     * is &ge; {@code fromMinute} and {@code end_minute} is &le; {@code toMinute}
     * are included.
     * </p>
     *
     * @param campaignId        the campaign identifier
     * @param dominanteNameLike the substring to match against dominante names
     * @param fromMinute        the lower bound of the time window (minutes since midnight)
     * @param toMinute          the upper bound of the time window (minutes since midnight)
     * @return a list of matching SessionSlot objects ordered by date and start
     *         time (never {@code null}; empty if none match or on error)
     */
    public List<SessionSlot> searchByDominanteNameAndTime(int campaignId, String dominanteNameLike, int fromMinute, int toMinute) {
        String sql = "SELECT s.id, s.campaign_id, s.dominante_id, s.title, s.room, s.session_date, s.start_minute, s.end_minute, s.capacity, s.created_by FROM sessions s JOIN dominantes d ON d.id = s.dominante_id WHERE s.campaign_id = ? AND UPPER(d.name) LIKE UPPER(?) AND s.start_minute >= ? AND s.end_minute <= ? ORDER BY s.session_date, s.start_minute";
        List<SessionSlot> result = new ArrayList<SessionSlot>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            ps.setString(2, "%" + dominanteNameLike + "%");
            ps.setInt(3, fromMinute);
            ps.setInt(4, toMinute);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapSession(rs));
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.searchByDominanteNameAndTime: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Counts the number of students allocated to a specific session within
     * a campaign.
     * <p>
     * Only registrations with {@code status = 'ALLOCATED'} are counted.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @param sessionId  the session identifier
     * @return the count of allocated registrations (0 if none or on error)
     */
    public int countAllocated(int campaignId, int sessionId) {
        String sql = "SELECT COUNT(*) c FROM registrations WHERE campaign_id = ? AND session_id = ? AND status = 'ALLOCATED'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return 0;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            ps.setInt(2, sessionId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("c");
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.countAllocated: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Maps the current row of a ResultSet to a SessionSlot object.
     * <p>
     * Expects the ResultSet to contain the following columns in any order:
     * id, campaign_id, dominante_id, title, room, session_date, start_minute,
     * end_minute, capacity, created_by.
     * </p>
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a fully populated SessionSlot instance
     * @throws Exception if a column value cannot be read or the SessionSlot constructor fails
     */
    private SessionSlot mapSession(ResultSet rs) throws Exception {
        return new SessionSlot(
                rs.getInt("id"),
                rs.getInt("campaign_id"),
                rs.getInt("dominante_id"),
                rs.getString("title"),
                rs.getString("room"),
                rs.getString("session_date"),
                rs.getInt("start_minute"),
                rs.getInt("end_minute"),
                rs.getInt("capacity"),
                rs.getInt("created_by")
        );
    }

    /**
     * Counts the total number of session slots created for a campaign.
     *
     * @param campaignId the campaign identifier
     * @return the total session count (0 if none or on error)
     */
    public int countByCampaign(int campaignId) {
        String sql = "SELECT COUNT(*) c FROM sessions WHERE campaign_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return 0;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("c");
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.countByCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Updates the capacity of a session slot.
     * <p>
     * Only the capacity column is modified; all other session attributes
     * remain unchanged. The {@code updated_at} timestamp is set automatically.
     * </p>
     *
     * @param sessionId the ID of the session to update
     * @param capacity  the new maximum capacity value
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
    public boolean updateCapacity(int sessionId, int capacity) {
        String sql = "UPDATE sessions SET capacity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, capacity);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("SessionDAO.updateCapacity: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Returns a map of session ID to allocated student count for all sessions
     * within a campaign.
     * <p>
     * Only registrations with {@code status = 'ALLOCATED'} are included in the
     * count. Sessions with no allocations will not appear as keys in the map.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @return a map where keys are session IDs and values are the number of
     *         allocated students for that session (never {@code null};
     *         empty if none exist or on error)
     */
    public Map<Integer, Integer> countBySessionForCampaign(int campaignId) {
        String sql = "SELECT session_id, COUNT(*) c FROM registrations WHERE campaign_id = ? AND status = 'ALLOCATED' GROUP BY session_id";
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getInt("session_id"), rs.getInt("c"));
            }
        } catch (Exception e) {
            System.err.println("SessionDAO.countBySessionForCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
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
