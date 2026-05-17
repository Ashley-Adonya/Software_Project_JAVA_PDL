package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Choice;

/**
 * Data Access Object for the Choice entity.
 * <p>
 * This DAO handles all database operations on the {@code choices} table,
 * including creating, updating, deleting, and querying student choices
 * within a campaign. It also provides an atomic replace operation that
 * deletes all previous choices for a student and inserts new ones within
 * a single database transaction.
 * </p>
 *
 * <p>The {@code choices} table stores the ranked session preferences expressed
 * by each student during a campaign. Each record links a student to a session
 * with an associated rank order.</p>
 */
public class ChoiceDAO {

    /**
     * Inserts a new choice into the {@code choices} table.
     * <p>
     * The {@code created_at} timestamp is set automatically by the database.
     * On success the auto-generated primary key is returned.
     * </p>
     *
     * @param choice the Choice entity containing campaign ID, student ID,
     *               session ID, and rank order
     * @return the generated choice ID on success, or {@code -1} if the
     *         connection could not be obtained or an error occurred
     */
    public int create(Choice choice) {
        String sql = "INSERT INTO choices (campaign_id, student_id, session_id, rank_order, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, new String[] { "ID" });
            ps.setInt(1, choice.getCampaignId());
            ps.setInt(2, choice.getStudentId());
            ps.setInt(3, choice.getSessionId());
            ps.setInt(4, choice.getRankOrder());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt("ID");
            }
        } catch (Exception e) {
            System.err.println("ChoiceDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    /**
     * Updates the session ID and rank order of an existing choice.
     * <p>
     * The {@code updated_at} timestamp is set automatically by the database.
     * Only the session reference and rank can be modified; the campaign
     * and student associations are immutable after creation.
     * </p>
     *
     * @param choice the Choice object containing the updated fields;
     *               must have a valid non-null {@code id}
     * @return {@code true} if exactly one row was updated, {@code false} otherwise
     */
    public boolean update(Choice choice) {
        String sql = "UPDATE choices SET session_id = ?, rank_order = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, choice.getSessionId());
            ps.setInt(2, choice.getRankOrder());
            ps.setInt(3, choice.getId());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("ChoiceDAO.update: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Deletes a single choice by its primary key.
     *
     * @param id the ID of the choice to delete
     * @return {@code true} if exactly one row was deleted, {@code false} otherwise
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM choices WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("ChoiceDAO.deleteById: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    /**
     * Deletes all choices made by a specific student for a specific campaign.
     *
     * @param campaignId the campaign identifier
     * @param studentId  the student identifier
     * @return the number of rows deleted (may be 0 if none existed)
     */
    public int deleteByStudentAndCampaign(int campaignId, int studentId) {
        String sql = "DELETE FROM choices WHERE campaign_id = ? AND student_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return 0;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            ps.setInt(2, studentId);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("ChoiceDAO.deleteByStudentAndCampaign: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return 0;
    }

    /**
     * Retrieves all choices made by a specific student for a specific campaign,
     * ordered by rank order (ascending).
     *
     * @param campaignId the campaign identifier
     * @param studentId  the student identifier
     * @return a list of matching Choice objects sorted by rank (never
     *         {@code null}; empty if none found or on error)
     */
    public List<Choice> findByStudentAndCampaign(int campaignId, int studentId) {
        String sql = "SELECT id, campaign_id, student_id, session_id, rank_order FROM choices WHERE campaign_id = ? AND student_id = ? ORDER BY rank_order";
        List<Choice> result = new ArrayList<Choice>();
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
                result.add(mapChoice(rs));
            }
        } catch (Exception e) {
            System.err.println("ChoiceDAO.findByStudentAndCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Retrieves all choices for a given campaign, ordered by student ID
     * and then by rank order.
     * <p>
     * This is intended for batch processing of all student preferences
     * within a campaign (e.g. during the allocation algorithm).
     * </p>
     *
     * @param campaignId the campaign identifier
     * @return a list of all Choice objects for the campaign (never
     *         {@code null}; empty if none exist or on error)
     */
    public List<Choice> findByCampaign(int campaignId) {
        String sql = "SELECT id, campaign_id, student_id, session_id, rank_order FROM choices WHERE campaign_id = ? ORDER BY student_id, rank_order";
        List<Choice> result = new ArrayList<Choice>();
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
                result.add(mapChoice(rs));
            }
        } catch (Exception e) {
            System.err.println("ChoiceDAO.findByCampaign: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    /**
     * Atomically replaces all choices for a student in a given campaign.
     * <p>
     * The operation runs within a single database transaction:
     * <ol>
     *   <li>All existing choices for the student/campaign pair are deleted.</li>
     *   <li>All new choices from the provided list are inserted in a batch.</li>
     *   <li>The transaction is committed on success.</li>
     * </ol>
     * If any step fails, the transaction is rolled back and the original
     * choices are preserved.
     * </p>
     *
     * @param campaignId the campaign identifier
     * @param studentId  the student identifier
     * @param newChoices the list of new Choice objects to insert (the actual
     *                   campaign_id and student_id in each Choice object are
     *                   ignored in favour of the method parameters)
     * @return {@code true} if the replacement completed successfully,
     *         {@code false} if the connection failed or an error occurred
     */
    public boolean replaceStudentChoices(int campaignId, int studentId, List<Choice> newChoices) {
        Connection conn = null;
        PreparedStatement del = null;
        PreparedStatement ins = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            del = conn.prepareStatement("DELETE FROM choices WHERE campaign_id = ? AND student_id = ?");
            del.setInt(1, campaignId);
            del.setInt(2, studentId);
            del.executeUpdate();

            String insertSql = "INSERT INTO choices (campaign_id, student_id, session_id, rank_order, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            ins = conn.prepareStatement(insertSql);
            for (int i = 0; i < newChoices.size(); i++) {
                Choice c = newChoices.get(i);
                ins.setInt(1, campaignId);
                ins.setInt(2, studentId);
                ins.setInt(3, c.getSessionId());
                ins.setInt(4, c.getRankOrder());
                ins.addBatch();
            }
            ins.executeBatch();

            conn.commit();
            return true;
        } catch (Exception e) {
            System.err.println("ChoiceDAO.replaceStudentChoices: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }
        } finally {
            close(ins);
            close(del);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
            close(conn);
        }
        return false;
    }

    /**
     * Maps the current row of a ResultSet to a Choice object.
     * <p>
     * Expects the ResultSet to contain the following columns in any order:
     * id, campaign_id, student_id, session_id, rank_order.
     * </p>
     *
     * @param rs the ResultSet positioned at the row to map
     * @return a fully populated Choice instance
     * @throws Exception if a column value cannot be read or the Choice constructor fails
     */
    private Choice mapChoice(ResultSet rs) throws Exception {
        return new Choice(
                rs.getInt("id"),
                rs.getInt("campaign_id"),
                rs.getInt("student_id"),
                rs.getInt("session_id"),
                rs.getInt("rank_order")
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
