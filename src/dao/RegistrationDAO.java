package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Registration;

public class RegistrationDAO {

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
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                return keys.getInt(1);
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
    private void close(AutoCloseable c) {
        if (c != null && !(c instanceof java.sql.Connection)) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
