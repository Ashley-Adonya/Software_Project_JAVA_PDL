package service;

import dao.CampaignDAO;
import dao.DominanteDAO;
import dao.RegistrationDAO;
import dao.SessionDAO;
import dao.UserDAO;
import model.Campaign;
import model.Dominante;
import model.Registration;
import model.SessionSlot;
import model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de génération de statistiques sur les campagnes d'attribution.
 * Fournit des informations agrégées sur les taux de remplissage, les inscriptions,
 * les étudiants non inscrits et l'état général des campagnes.
 * 
 * @author PDL Application
 * @version 1.0
 */
public class StatisticsService {
    private final SessionDAO sessionDAO;
    private final RegistrationDAO registrationDAO;
    private final UserDAO userDAO;
    private final DominanteDAO dominanteDAO;
    private final CampaignDAO campaignDAO;

    public StatisticsService() {
        this.sessionDAO = new SessionDAO();
        this.registrationDAO = new RegistrationDAO();
        this.userDAO = new UserDAO();
        this.dominanteDAO = new DominanteDAO();
        this.campaignDAO = new CampaignDAO();
    }

    public static class StatsSummary {
        public int totalSessions;
        public int completeSessions;
        public int totalCapacity;
        public int totalAllocated;
        public double averageFillRate;
        public int totalStudents;
        public int registeredStudents;
        public int unregisteredStudents;
    }

    public static class StudentWithSessions {
        public User student;
        public List<SessionInfo> sessions;
    }

    public static class SessionInfo {
        public String dominanteName;
        public String title;
        public String room;
        public String date;
        public String startTime;
        public String endTime;
        public int capacity;
        public int allocated;
        public double fillRate;
    }

    public StatsSummary getStatsForCampaign(int campaignId, String promo) {
        StatsSummary stats = new StatsSummary();
        
        List<SessionSlot> sessions = sessionDAO.findByCampaign(campaignId);
        stats.totalSessions = sessions.size();
        
        List<Integer> registeredStudentIds = registrationDAO.findStudentIdsWithRegistrations(campaignId);
        
        int totalCapacity = 0;
        int totalAllocated = 0;
        int completeSessions = 0;
        
        Map<Integer, Integer> allocationsBySession = sessionDAO.countBySessionForCampaign(campaignId);
        
        for (SessionSlot session : sessions) {
            totalCapacity += session.getCapacity();
            Integer allocated = allocationsBySession.get(session.getId());
            int alloc = allocated != null ? allocated : 0;
            totalAllocated += alloc;
            
            if (alloc >= session.getCapacity()) {
                completeSessions++;
            }
        }
        
        stats.completeSessions = completeSessions;
        stats.totalCapacity = totalCapacity;
        stats.totalAllocated = totalAllocated;
        
        if (totalCapacity > 0) {
            stats.averageFillRate = (totalAllocated * 100.0) / totalCapacity;
        } else {
            stats.averageFillRate = 0;
        }
        
        List<User> allStudents = userDAO.findAllStudentsByPromo(promo);
        stats.totalStudents = allStudents.size();
        stats.registeredStudents = registeredStudentIds.size();
        stats.unregisteredStudents = stats.totalStudents - stats.registeredStudents;
        
        return stats;
    }

    public List<User> getUnregisteredStudents(int campaignId, String promo) {
        List<Integer> registeredIds = registrationDAO.findStudentIdsWithRegistrations(campaignId);
        return userDAO.findStudentsWithoutRegistrationInCampaign(promo, campaignId, registeredIds);
    }

    public StudentWithSessions getStudentSessions(int campaignId, int studentId) {
        StudentWithSessions result = new StudentWithSessions();
        
        result.student = userDAO.findById(studentId);
        if (result.student == null) {
            return result;
        }
        
        List<Registration> registrations = registrationDAO.findByStudentAndCampaign(campaignId, studentId);
        
        List<SessionSlot> allSessions = sessionDAO.findByCampaign(campaignId);
        Map<Integer, SessionSlot> sessionMap = new HashMap<>();
        for (SessionSlot s : allSessions) {
            sessionMap.put(s.getId(), s);
        }
        
        List<Dominante> dominantes = dominanteDAO.findAll();
        Map<Integer, Dominante> dominanteMap = new HashMap<>();
        for (Dominante d : dominantes) {
            dominanteMap.put(d.getId(), d);
        }
        
        result.sessions = new ArrayList<>();
        
        for (Registration reg : registrations) {
            SessionSlot session = sessionMap.get(reg.getSessionId());
            if (session != null) {
                SessionInfo info = new SessionInfo();
                
                Dominante dom = dominanteMap.get(session.getDominanteId());
                info.dominanteName = dom != null ? dom.getName() : "Dominante #" + session.getDominanteId();
                
                info.title = session.getTitle();
                info.room = session.getRoom();
                info.date = session.getSessionDate();
                info.startTime = formatMinute(session.getStartMinute());
                info.endTime = formatMinute(session.getEndMinute());
                info.capacity = session.getCapacity();
                
                int allocated = registrationDAO.countAllocatedBySession(campaignId, session.getId());
                info.allocated = allocated;
                
                if (session.getCapacity() > 0) {
                    info.fillRate = (allocated * 100.0) / session.getCapacity();
                } else {
                    info.fillRate = 0;
                }
                
                result.sessions.add(info);
            }
        }
        
        return result;
    }

    public Campaign getActiveCampaign() {
        List<Campaign> openCampaigns = campaignDAO.findByStatus("OPEN");
        if (openCampaigns != null && !openCampaigns.isEmpty()) {
            return openCampaigns.get(0);
        }
        List<Campaign> prepCampaigns = campaignDAO.findByStatus("PREPARATION");
        if (prepCampaigns != null && !prepCampaigns.isEmpty()) {
            return prepCampaigns.get(0);
        }
        return null;
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}