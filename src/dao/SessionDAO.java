package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.SessionSlot;

public class SessionDAO {

    public int create(SessionSlot session) {
        String sql = "INSERT INTO sessions (campaign_id, dominante_id, title, room, session_date, start_minute, end_minute, capacity, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = ConnectionDAO.getConnection();
            if (conn == null) {
                return -1;
            }
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getCampaignId());
            ps.setInt(2, session.getDominanteId());
            ps.setString(3, session.getTitle());
            ps.setString(4, session.getRoom());
            ps.setString(5, session.getSessionDate());
            ps.setInt(6, session.getStartMinute());
            ps.setInt(7, session.getEndMinute());
            ps.setInt(8, session.getCapacity());
            ps.setInt(9, session.getCreatedBy());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
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

    private void close(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
