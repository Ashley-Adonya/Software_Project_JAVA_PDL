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
 * Service for generating statistics on attribution campaigns.
 * Provides aggregated information on fill rates, registrations,
 * unregistered students, and overall campaign status.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class StatisticsService {
    private final SessionDAO sessionDAO;
    private final RegistrationDAO registrationDAO;
    private final UserDAO userDAO;
    private final DominanteDAO dominanteDAO;
    private final CampaignDAO campaignDAO;

    /**
     * Constructs a new StatisticsService with default DAO implementations.
     */
    public StatisticsService() {
        this.sessionDAO = new SessionDAO();
        this.registrationDAO = new RegistrationDAO();
        this.userDAO = new UserDAO();
        this.dominanteDAO = new DominanteDAO();
        this.campaignDAO = new CampaignDAO();
    }

    /**
     * Aggregated statistics summary for a campaign, including session counts, capacity, fill rates, and student participation.
     */
    public static class StatsSummary {
        public int totalSessions;
        public int completeSessions;
        public int totalCapacity;
        public int totalAllocated;
        public double averageFillRate;
        public int totalStudents;
        public int registeredStudents;
        public int unregisteredStudents;
        public int activeDominantes;
        public List<SessionDetail> sessionDetails = new ArrayList<>();
    }

    /**
     * Represents a summary of a single session's details for statistics display.
     */
    public static class SessionDetail {
        public int sessionId;
        public String sessionTitle;
        public String dominanteName;
        public String timeSlot;
        public int capacity;
        public int allocated;
        public boolean isFull;
    }

    /**
     * Holds a student together with a list of their session information.
     */
    public static class StudentWithSessions {
        public User student;
        public List<SessionInfo> sessions;
    }

    /**
     * Holds detailed information about a session for a specific student's session listing.
     */
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

    /**
     * Generates a full statistics summary for a campaign, including session fill rates, capacity totals, and student counts, with caching.
     *
     * @param campaignId the campaign ID
     * @param promo      the promotional year filter for student counts
     * @return a StatsSummary with all aggregated statistics
     */
    public StatsSummary getStatsForCampaign(int campaignId, String promo) {
        return CacheManager.getOrLoad("stats:summary:" + campaignId + ":" + safe(promo), () -> {
            StatsSummary stats = new StatsSummary();
            List<SessionSlot> sessions = sessionDAO.findByCampaign(campaignId);
            stats.totalSessions = sessions.size();
            List<Integer> registeredStudentIds = registrationDAO.findStudentIdsWithRegistrations(campaignId);
            int totalCapacity = 0, totalAllocated = 0, completeSessions = 0;
            Map<Integer, Integer> allocationsBySession = sessionDAO.countBySessionForCampaign(campaignId);
            List<Dominante> dominantes = dominanteDAO.findAll();
            Map<Integer, Dominante> domMap = new HashMap<>();
            for (Dominante d : dominantes) domMap.put(d.getId(), d);

            for (SessionSlot session : sessions) {
                int alloc = allocationsBySession.getOrDefault(session.getId(), 0);
                totalCapacity += session.getCapacity();
                totalAllocated += alloc;
                if (alloc >= session.getCapacity()) completeSessions++;

                SessionDetail detail = new SessionDetail();
                detail.sessionId = session.getId();
                detail.sessionTitle = session.getTitle();
                detail.capacity = session.getCapacity();
                detail.allocated = alloc;
                detail.isFull = alloc >= session.getCapacity();
                Dominante dom = domMap.get(session.getDominanteId());
                detail.dominanteName = dom != null ? dom.getName() : "#" + session.getDominanteId();
                detail.timeSlot = formatMinute(session.getStartMinute()) + " - " + formatMinute(session.getEndMinute());
                stats.sessionDetails.add(detail);
            }
            stats.completeSessions = completeSessions;
            stats.totalCapacity = totalCapacity;
            stats.activeDominantes = domMap.size();
            stats.totalAllocated = totalAllocated;
            stats.averageFillRate = totalCapacity > 0 ? (totalAllocated * 100.0) / totalCapacity : 0;
            List<User> allStudents = userDAO.findAllStudentsByPromo(promo);
            stats.totalStudents = allStudents.size();
            stats.registeredStudents = registeredStudentIds.size();
            stats.unregisteredStudents = stats.totalStudents - stats.registeredStudents;
            return stats;
        });
    }

    /**
     * Retrieves students who do not have any registration in the specified campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @param promo      the promotional year filter
     * @return list of unregistered students
     */
    public List<User> getUnregisteredStudents(int campaignId, String promo) {
        return CacheManager.getOrLoad("stats:unregistered:" + campaignId + ":" + safe(promo), () -> {
            List<Integer> registeredIds = registrationDAO.findStudentIdsWithRegistrations(campaignId);
            return userDAO.findStudentsWithoutRegistrationInCampaign(promo, campaignId, registeredIds);
        });
    }

    /**
     * Retrieves the list of students who have at least one registration in a given campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @param promo      the promotional year filter
     * @return list of registered students
     */
    public List<User> getRegisteredStudents(int campaignId, String promo) {
        return CacheManager.getOrLoad("stats:registered:" + campaignId + ":" + safe(promo), () -> {
            List<User> all = userDAO.findAllStudentsByPromo(promo);
            List<Integer> registeredIds = registrationDAO.findStudentIdsWithRegistrations(campaignId);
            List<User> result = new ArrayList<>();
            for (User u : all) if (registeredIds.contains(u.getId())) result.add(u);
            return result;
        });
    }

    /**
     * Retourne les étudiants inscrits dans une session spécifique.
     */
    public List<User> getStudentsInSession(int campaignId, int sessionId) {
        return CacheManager.getOrLoad("stats:session:" + campaignId + ":" + sessionId + ":students", () -> {
            List<Registration> regs = registrationDAO.findBySessionAndStatus(campaignId, sessionId, "ALLOCATED");
            List<User> result = new ArrayList<>();
            for (Registration r : regs) {
                User u = userDAO.findById(r.getStudentId());
                if (u != null) result.add(u);
            }
            return result;
        });
    }

    /**
     * Retourne les sessions d'un étudiant dans une campagne.
     */
    public List<SessionDetail> getSessionsForStudent(int campaignId, int studentId) {
        return CacheManager.getOrLoad("stats:student:" + campaignId + ":" + studentId + ":sessions", () -> {
            List<Registration> regs = registrationDAO.findByStudentAndCampaign(campaignId, studentId);
            List<SessionDetail> result = new ArrayList<>();
            List<Dominante> dominantes = dominanteDAO.findAll();
            Map<Integer, Dominante> domMap = new HashMap<>();
            for (Dominante d : dominantes) domMap.put(d.getId(), d);
            List<SessionSlot> sessions = sessionDAO.findByCampaign(campaignId);
            Map<Integer, SessionSlot> sessMap = new HashMap<>();
            for (SessionSlot s : sessions) sessMap.put(s.getId(), s);
            for (Registration r : regs) {
                SessionSlot s = sessMap.get(r.getSessionId());
                if (s != null) {
                    SessionDetail detail = new SessionDetail();
                    detail.sessionId = s.getId();
                    detail.sessionTitle = s.getTitle();
                    detail.capacity = s.getCapacity();
                    detail.allocated = registrationDAO.countAllocatedBySession(campaignId, s.getId());
                    detail.isFull = detail.allocated >= detail.capacity;
                    Dominante dom = domMap.get(s.getDominanteId());
                    detail.dominanteName = dom != null ? dom.getName() : "#" + s.getDominanteId();
                    detail.timeSlot = formatMinute(s.getStartMinute()) + " - " + formatMinute(s.getEndMinute());
                    result.add(detail);
                }
            }
            return result;
        });
    }

    /**
     * Retourne les étudiants d'une dominante (via ses sessions).
     */
    public List<User> getStudentsByDominante(int campaignId, int dominanteId) {
        return CacheManager.getOrLoad("stats:dom:" + campaignId + ":" + dominanteId + ":students", () -> {
            List<SessionSlot> sessions = sessionDAO.findByCampaign(campaignId);
            List<Integer> sessionIds = new ArrayList<>();
            for (SessionSlot s : sessions) if (s.getDominanteId() == dominanteId) sessionIds.add(s.getId());
            List<User> result = new ArrayList<>();
            for (int sessId : sessionIds) {
                List<User> students = getStudentsInSession(campaignId, sessId);
                for (User u : students) if (!result.contains(u)) result.add(u);
            }
            return result;
        });
    }

    /**
     * Retourne les sessions d'une dominante.
     */
    public List<SessionDetail> getSessionsByDominante(int campaignId, int dominanteId) {
        List<SessionDetail> result = new ArrayList<>();
        List<SessionSlot> sessions = sessionDAO.findByCampaign(campaignId);
        List<Dominante> dominantes = dominanteDAO.findAll();
        Map<Integer, Dominante> domMap = new HashMap<>();
        for (Dominante d : dominantes) domMap.put(d.getId(), d);
        Map<Integer, Integer> allocations = sessionDAO.countBySessionForCampaign(campaignId);
        for (SessionSlot s : sessions) {
            if (s.getDominanteId() == dominanteId) {
                SessionDetail detail = new SessionDetail();
                detail.sessionId = s.getId();
                detail.sessionTitle = s.getTitle();
                detail.capacity = s.getCapacity();
                detail.allocated = allocations.getOrDefault(s.getId(), 0);
                detail.isFull = detail.allocated >= detail.capacity;
                Dominante dom = domMap.get(dominanteId);
                detail.dominanteName = dom != null ? dom.getName() : "#" + dominanteId;
                detail.timeSlot = formatMinute(s.getStartMinute()) + " - " + formatMinute(s.getEndMinute());
                result.add(detail);
            }
        }
        return result;
    }

    /**
     * Retrieves a student's profile along with all their registered sessions and session details for a campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @param studentId  the student ID
     * @return a StudentWithSessions object containing the student and their session list
     */
    public StudentWithSessions getStudentSessions(int campaignId, int studentId) {
        return CacheManager.getOrLoad("stats:student:" + campaignId + ":" + studentId, () -> {
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
        });
    }

    /**
     * Retrieves the currently active campaign (first OPEN campaign, or first PREPARATION campaign if none is OPEN), with caching.
     *
     * @return the active Campaign, or null if none found
     */
    public Campaign getActiveCampaign() {
        return CacheManager.getOrLoad("stats:activeCampaign", () -> {
            List<Campaign> openCampaigns = campaignDAO.findByStatus("OPEN");
            if (openCampaigns != null && !openCampaigns.isEmpty()) {
                return openCampaigns.get(0);
            }
            List<Campaign> prepCampaigns = campaignDAO.findByStatus("PREPARATION");
            if (prepCampaigns != null && !prepCampaigns.isEmpty()) {
                return prepCampaigns.get(0);
            }
            return null;
        });
    }

    /**
     * Sanitizes a string value for cache key usage (trims and uppercases, or returns empty string if null).
     *
     * @param value the raw string value
     * @return a sanitized non-null string
     */
    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    /**
     * Formats a minute-of-day value into a HH:mm time string.
     *
     * @param minute the minute value from midnight
     * @return formatted time string (e.g. "08:30")
     */
    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}