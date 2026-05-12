package dao;

import java.sql.*;
import model.Campaign;
import oracle.jdbc.OraclePreparedStatement;

public class Test {

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

    private static void close(AutoCloseable c) {
        if (c != null && !(c instanceof java.sql.Connection)) {
            try {
                c.close();
            } catch (Exception ignored) {}
        }
    }
}