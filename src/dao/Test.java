package dao;

import java.sql.*;
import model.Campaign;
import oracle.jdbc.OraclePreparedStatement;

/**
 * Data-access test utility for {@link Campaign} persistence operations.
 * <p>
 * Provides direct (non-DAO) CRUD methods — {@link #create(Campaign, Connection)}
 * and {@link #findById(int, Connection)} — that exercise raw JDBC with Oracle
 * RETURNING clauses. The {@link #main(String[])} method acts as an integration
 * test that creates a sample campaign, retrieves it, and reports timing.
 * </p>
 */
public class Test {

    /**
     * Inserts a new campaign into the database and returns its auto-generated ID.
     * <p>
     * Uses an Oracle {@code RETURNING id INTO ?} clause to retrieve the
     * generated primary key. Date fields are expected as {@code YYYY-MM-DD}
     * strings and are converted via Oracle's {@code TO_DATE}.
     * </p>
     *
     * @param campaign the {@link Campaign} entity to persist (must not be null)
     * @param conn     an active JDBC {@link Connection}; if {@code null} the
     *                 method returns {@code -1} immediately
     * @return the generated campaign ID on success, or {@code -1} on failure
     */
    public int create(Campaign campaign, Connection conn) {

        String sql = "INSERT INTO campaigns " +
                "(name, promo, registration_day, start_date, end_date, max_choices, status, created_by, created_at) " +
                "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, SYSTIMESTAMP) " +
                "RETURNING id INTO ?";

        OraclePreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (conn == null) return -1;

            ps = (OraclePreparedStatement) conn.prepareStatement(sql);

            ps.setString(1, campaign.getName());
            ps.setString(2, campaign.getPromo());
            ps.setString(3, campaign.getRegistrationDay());
            ps.setString(4, campaign.getStartDate());
            ps.setString(5, campaign.getEndDate());
            ps.setInt(6, campaign.getMaxChoices());
            ps.setString(7, campaign.getStatus());
            ps.setInt(8, campaign.getCreatedBy());

            // 🔥 clé Oracle
            ps.registerReturnParameter(9, Types.INTEGER);

            ps.executeUpdate();

            rs = ps.getReturnResultSet();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            System.err.println("CampaignDAO.create: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }

        return -1;
    }

    /**
     * Retrieves a campaign by its primary key.
     * <p>
     * Maps all columns from the {@code campaigns} table onto a {@link Campaign}
     * object. DATE values are converted to their string representation via
     * {@link java.sql.Date#toString()}.
     * </p>
     *
     * @param id   the campaign's database identifier
     * @param conn an active JDBC {@link Connection}; if {@code null} returns
     *             {@code null}
     * @return the populated {@link Campaign} if found, or {@code null} if no
     *         row matches or an error occurs
     */
    public Campaign findById(int id, Connection conn) {

        String sql = "SELECT id, name, promo, registration_day, start_date, end_date, max_choices, status, created_by " +
                     "FROM campaigns WHERE id = ?";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                Campaign c = new Campaign();

                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setPromo(rs.getString("promo"));

                // conversion DATE -> String
                c.setRegistrationDay(rs.getDate("registration_day").toString());
                c.setStartDate(rs.getDate("start_date").toString());
                c.setEndDate(rs.getDate("end_date").toString());

                c.setMaxChoices(rs.getInt("max_choices"));
                c.setStatus(rs.getString("status"));
                c.setCreatedBy(rs.getInt("created_by"));

                return c;
            }

        } catch (Exception e) {
            System.err.println("CampaignDAO.findById: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }

        return null;
    }

    /**
     * Integration test entry point.
     * <p>
     * Creates a new {@link Campaign} with hard-coded sample data, persists it
     * via {@link #create(Campaign, Connection)}, retrieves it via
     * {@link #findById(int, Connection)}, and prints the results together with
     * the total execution time.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        float start = System.currentTimeMillis() / 1000;

        Connection conn = null;

        try {
            conn = ConnectionDAO.getConnection();

            if (conn == null) {
                System.err.println("❌ DB connection failed");
                return;
            }

            Test dao = new Test();

            Campaign c = new Campaign();

            c.setName("Campagne de test i3 - " + System.currentTimeMillis());
            c.setPromo("PDL-2024");

            // ⚠️ format STRICT
            c.setRegistrationDay("2024-09-01");
            c.setStartDate("2024-09-15");
            c.setEndDate("2024-10-15");

            c.setMaxChoices(3);
            c.setStatus("OPEN");
            c.setCreatedBy(1); // ⚠️ doit exister en base

            int id = dao.create(c, conn);

            System.out.println("Created campaign with ID: " + id);
            conn = ConnectionDAO.getConnection();

            if (id != -1) {
                Campaign found = dao.findById(id, conn);

                if (found != null) {
                    System.out.println("Found campaign: " + found.getName());
                } else {
                    System.out.println("⚠️ Campaign not found");
                }
            } else {
                System.out.println("❌ Insert failed");
            }

        } catch (Exception e) {
            System.err.println("GLOBAL ERROR: " + e.getMessage());
        } finally {
            close(conn);
        }

        float end = System.currentTimeMillis() / 1000;
        System.out.println("Execution time: " + (end - start) + " seconds");
    }

    /**
     * Safely closes a JDBC resource, ignoring any exceptions.
     * <p>
     * Connection instances are deliberately not closed to allow reuse by the
     * caller.
     * </p>
     *
     * @param c the {@link AutoCloseable} to close; may be {@code null}
     */
    private static void close(AutoCloseable c) {
        if (c != null && !(c instanceof java.sql.Connection)) {
            try {
                c.close();
            } catch (Exception ignored) {}
        }
    }
}