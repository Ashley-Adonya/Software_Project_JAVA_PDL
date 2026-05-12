package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Choice;

public class ChoiceDAO {

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
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, choice.getCampaignId());
            ps.setInt(2, choice.getStudentId());
            ps.setInt(3, choice.getSessionId());
            ps.setInt(4, choice.getRankOrder());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
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

    private Choice mapChoice(ResultSet rs) throws Exception {
        return new Choice(
                rs.getInt("id"),
                rs.getInt("campaign_id"),
                rs.getInt("student_id"),
                rs.getInt("session_id"),
                rs.getInt("rank_order")
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
