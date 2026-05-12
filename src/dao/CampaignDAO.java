package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Campaign;

public class CampaignDAO {

    public int create(Campaign campaign) {
        String sql = "INSERT INTO campaigns (name, promo, registration_day, start_date, end_date, max_choices, status, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, campaign.getName());
            ps.setString(2, campaign.getPromo());
            ps.setString(3, campaign.getRegistrationDay());
            ps.setString(4, campaign.getStartDate());
            ps.setString(5, campaign.getEndDate());
            ps.setInt(6, campaign.getMaxChoices());
            ps.setString(7, campaign.getStatus());
            ps.setInt(8, campaign.getCreatedBy());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("CampaignDAO.create: " + e.getMessage());
        } finally {
            close(keys);
            close(ps);
            close(conn);
        }
        return -1;
    }

    public Campaign findById(int id) {
        String sql = "SELECT id, name, promo, registration_day, start_date, end_date, max_choices, status, created_by FROM campaigns WHERE id = ?";
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
                return mapCampaign(rs);
            }
        } catch (Exception e) {
            System.err.println("CampaignDAO.findById: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return null;
    }

    public List<Campaign> findByPromo(String promo) {
        String sql = "SELECT id, name, promo, registration_day, start_date, end_date, max_choices, status, created_by FROM campaigns WHERE promo = ? ORDER BY registration_day DESC";
        List<Campaign> result = new ArrayList<Campaign>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, promo);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapCampaign(rs));
            }
        } catch (Exception e) {
            System.err.println("CampaignDAO.findByPromo: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    public List<Campaign> findByStatus(String status) {
        String sql = "SELECT id, name, promo, registration_day, start_date, end_date, max_choices, status, created_by FROM campaigns WHERE status = ? ORDER BY registration_day DESC";
        List<Campaign> result = new ArrayList<Campaign>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return result;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapCampaign(rs));
            }
        } catch (Exception e) {
            System.err.println("CampaignDAO.findByStatus: " + e.getMessage());
        } finally {
            close(rs);
            close(ps);
            close(conn);
        }
        return result;
    }

    public boolean updateSettings(int campaignId, String name, String registrationDay, String startDate, String endDate, int maxChoices) {
        String sql = "UPDATE campaigns SET name = ?, registration_day = ?, start_date = ?, end_date = ?, max_choices = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, registrationDay);
            ps.setString(3, startDate);
            ps.setString(4, endDate);
            ps.setInt(5, maxChoices);
            ps.setInt(6, campaignId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("CampaignDAO.updateSettings: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    public boolean updateStatus(int campaignId, String status) {
        String sql = "UPDATE campaigns SET status = ?, "
                + "opened_at = CASE WHEN ? = 'OPEN' THEN CURRENT_TIMESTAMP ELSE opened_at END, "
                + "closed_at = CASE WHEN ? = 'CLOSED' THEN CURRENT_TIMESTAMP ELSE closed_at END, "
                + "processed_at = CASE WHEN ? = 'PROCESSING' THEN CURRENT_TIMESTAMP ELSE processed_at END, "
                + "validated_at = CASE WHEN ? = 'VALIDATED' THEN CURRENT_TIMESTAMP ELSE validated_at END, "
                + "archived_at = CASE WHEN ? = 'ARCHIVED' THEN CURRENT_TIMESTAMP ELSE archived_at END "
                + "WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, status);
            ps.setString(4, status);
            ps.setString(5, status);
            ps.setString(6, status);
            ps.setInt(7, campaignId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("CampaignDAO.updateStatus: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    public boolean deleteById(int campaignId) {
        String sql = "DELETE FROM campaigns WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return false;
            }
            ps = conn.prepareStatement(sql);
            ps.setInt(1, campaignId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("CampaignDAO.deleteById: " + e.getMessage());
        } finally {
            close(ps);
            close(conn);
        }
        return false;
    }

    private Campaign mapCampaign(ResultSet rs) throws Exception {
        return new Campaign(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("promo"),
                rs.getString("registration_day"),
                rs.getString("start_date"),
                rs.getString("end_date"),
                rs.getInt("max_choices"),
                rs.getString("status"),
                rs.getInt("created_by")
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
