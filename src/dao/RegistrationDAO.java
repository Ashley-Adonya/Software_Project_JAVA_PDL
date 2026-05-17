package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Registration;

/**
 * Data Access Object for the Registration entity.
 * <p>
 * This DAO handles all database operations on the {@code registrations} table,
 * including creating registrations, updating their status, querying by student,
 * session, campaign, and status, counting allocations, and bulk deletions.
 * </p>
 *
 * <p>The {@code registrations} table records the outcome of the allocation
 * process: each entry links a student to an allocated (or otherwise assigned)
 * session, along with the student's original choice rank that led to this
 * allocation, and the registration status ({@code ALLOCATED}, {@code PENDING},
 * etc.).</p>
 */
public class RegistrationDAO {

    /**
     * Inserts a new registration into the {@code registrations} table.
     * <p>
     * The {@code source_choice_rank} column accepts a {@code NULL} value when
     * the rank is not applicable (e.g. manual assignment). The
     * {@code created_at} timestamp is set automatically. On success the
     * auto-generated primary key is returned.
     * </p>
     *
     * @param registration the Registration entity containing campaign ID,
     *                     student ID, session ID, source choice rank (may be
     *                     {@code null}), and status
     * @return the generated registration ID on success, or {@code -1} if the
     *         connection could not be obtained or an error occurred
     */
    public int create(Registration registration) {
        String sql = "INSERT INTO registrations (campaign_id, student_id, session_id, source_choice_rank, status, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, new String[] { "ID" });
            ps.setInt(1, registration.getCampaignId());
            ps.setInt(2, registration.getStudentId());
            ps.setInt(3, registration.getSessionId());
            if (registration.getSourceChoiceRank() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, registration.getSourceChoiceRank().intValue());
            }
            ps.setString(5, registration.getStatus());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("RegistrationDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    /**
     * Updates the status of an existing registration.
     * <p>
     * The {@code updated_at} timestamp is set automatically.
     * Typical status values include {@code ALLOCATED}, {@code PENDING},
     * {@code WAITLISTED}, or {@code CANCELLED}.
     * </p>
     *
     * @param registrationId the ID of the registration to update
     * @param status         the new status string
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
    public boolean updateStatus(int registrationId, String status) {
        String sql = "UPDATE registrations SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, registrationId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("RegistrationDAO.updateStatus: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Deletes all registrations associated with a given campaign.
     * <p>
     * This is used to reset a campaign's allocation results before
     * re-running the allocation algorithm.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @return the number of rows deleted (may be 0 if none existed)
     */
    public int deleteByCampaign(int campaignId) {
        String sql = "DELETE FROM registrations WHERE campaign_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return 0;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("RegistrationDAO.deleteByCampaign: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Retrieves all registrations for a specific student within a campaign,
     * ordered by ID.
     *
     * @param campaignId the campaign identifier
     * @param studentId  the student identifier
     * @return a list of matching Registration objects (never {@code null};
     *         empty if none found or on error)
     */
    public List<Registration> findByStudentAndCampaign(int campaignId, int studentId) {
        String sql = "SELECT id, campaign_id, student_id, session_id, source_choice_rank, status FROM registrations WHERE campaign_id = ? AND student_id = ? ORDER BY id";
        List<Registration> result = new ArrayList<Registration>();
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
            ps.setInt(2, studentId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRegistration(rs));
            }
        } catch (Exception e) {
            System.err.println("RegistrationDAO.findByStudentAndCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Retrieves all registrations for a specific session with a given status,
     * ordered by ID.
     *
     * @param campaignId the campaign identifier
     * @param sessionId  the session identifier
     * @param status     the status to filter by (e.g. {@code ALLOCATED})
     * @return a list of matching Registration objects (never {@code null};
     *         empty if none found or on error)
     */
    public List<Registration> findBySessionAndStatus(int campaignId, int sessionId, String status) {
        String sql = "SELECT id, campaign_id, student_id, session_id, source_choice_rank, status FROM registrations WHERE campaign_id = ? AND session_id = ? AND status = ? ORDER BY id";
        List<Registration> result = new ArrayList<Registration>();
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
            ps.setInt(2, sessionId);
            ps.setString(3, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRegistration(rs));
            }
        } catch (Exception e) {
            System.err.println("RegistrationDAO.findBySessionAndStatus: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Counts the number of registrations with status {@code ALLOCATED} for a
     * specific session within a campaign.
     *
     * @param campaignId the campaign identifier
     * @param sessionId  the session identifier
     * @return the count of allocated registrations (0 if none or on error)
     */
    public int countAllocatedBySession(int campaignId, int sessionId) {
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
            System.err.println("RegistrationDAO.countAllocatedBySession: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Counts the total number of registrations (regardless of status) for a
     * specific session within a campaign.
     *
     * @param campaignId the campaign identifier
     * @param sessionId  the session identifier
     * @return the total registration count (0 if none or on error)
     */
    public int countBySession(int campaignId, int sessionId) {
        String sql = "SELECT COUNT(*) c FROM registrations WHERE campaign_id = ? AND session_id = ?";
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
            System.err.println("RegistrationDAO.countBySession: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Retrieves all registrations for a campaign that have a specific status,
     * ordered by ID.
     *
     * @param campaignId the campaign identifier
     * @param status     the status to filter by
     * @return a list of matching Registration objects (never {@code null};
     *         empty if none found or on error)
     */
    public List<Registration> findByCampaignAndStatus(int campaignId, String status) {
        String sql = "SELECT id, campaign_id, student_id, session_id, source_choice_rank, status FROM registrations WHERE campaign_id = ? AND status = ? ORDER BY id";
        List<Registration> result = new ArrayList<Registration>();
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
            ps.setString(2, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRegistration(rs));
            }
        } catch (Exception e) {
            System.err.println("RegistrationDAO.findByCampaignAndStatus: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Returns the distinct set of student IDs that have at least one
     * registration in the given campaign.
     * <p>
     * This is used to determine which students have already been processed
     * by the allocation algorithm.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @return a list of distinct student IDs (never {@code null};
     *         empty if none have registrations or on error)
     */
    public List<Integer> findStudentIdsWithRegistrations(int campaignId) {
        String sql = "SELECT DISTINCT student_id FROM registrations WHERE campaign_id = ?";
        List<Integer> result = new ArrayList<Integer>();
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
                result.add(rs.getInt("student_id"));
            }
        } catch (Exception e) {
            System.err.println("RegistrationDAO.findStudentIdsWithRegistrations: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Deletes all registrations for a specific student in a specific campaign.
     *
     * @param campaignId the campaign identifier
     * @param studentId  the student identifier
     * @return the number of rows deleted (may be 0 if none existed)
     */
    public int deleteByStudentAndCampaign(int campaignId, int studentId) {
        String sql = "DELETE FROM registrations WHERE campaign_id = ? AND student_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) return 0;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            ps.setInt(2, studentId);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("RegistrationDAO.deleteByStudentAndCampaign: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Maps the current row of a ResultSet to a Registration object.
     * <p>
     * Expects the ResultSet to contain the following columns in any order:
     * id, campaign_id, student_id, session_id, source_choice_rank, status.
     * The {@code source_choice_rank} column is nullable and will be mapped
     * to {@code null} if the database value is {@code NULL}.
     * </p>
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a fully populated Registration instance
     * @throws Exception if a column value cannot be read or the Registration constructor fails
     */
    private Registration mapRegistration(ResultSet rs) throws Exception {
        int sourceRank = rs.getInt("source_choice_rank");
        Integer sourceChoiceRank = rs.wasNull() ? null : Integer.valueOf(sourceRank);
        return new Registration(
                rs.getInt("id"),
                rs.getInt("campaign_id"),
                rs.getInt("student_id"),
                rs.getInt("session_id"),
                sourceChoiceRank,
                rs.getString("status")
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
