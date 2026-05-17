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
import java.util.List;

/**
 * Service de gestion des inscriptions d'étudiants aux sessions.
 * Valide les inscriptions pour détecter les conflits horaires, génère des alternatives,
 * et assure l'interdépendance entre choix, allocations et inscriptions.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class RegistrationService {
    private final RegistrationDAO registrationDAO;
    private final SessionDAO sessionDAO;
    private final UserDAO userDAO;
    private final CampaignDAO campaignDAO;
    private final DominanteDAO dominanteDAO;

    /**
     * Constructs a new RegistrationService with default DAO implementations.
     */
    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
        this.sessionDAO = new SessionDAO();
        this.userDAO = new UserDAO();
        this.campaignDAO = new CampaignDAO();
        this.dominanteDAO = new DominanteDAO();
    }

    /**
     * Represents the result of a conflict check during registration,
     * including time conflict status, message, and alternative suggestions.
     */
    public static class ConflictResult {
        public boolean hasConflict;
        public String conflictMessage;
        public boolean sessionFull;
        public List<AlternativeSession> alternatives;

        /**
         * Creates a ConflictResult indicating no conflict was found.
         *
         * @return a ConflictResult with hasConflict=false
         */
        public static ConflictResult noConflict() {
            ConflictResult result = new ConflictResult();
            result.hasConflict = false;
            result.sessionFull = false;
            result.alternatives = new ArrayList<>();
            return result;
        }

        /**
         * Creates a ConflictResult indicating a time conflict with a descriptive message.
         *
         * @param message the conflict description
         * @return a ConflictResult with hasConflict=true
         */
        public static ConflictResult withConflict(String message) {
            ConflictResult result = new ConflictResult();
            result.hasConflict = true;
            result.conflictMessage = message;
            result.sessionFull = false;
            result.alternatives = new ArrayList<>();
            return result;
        }

        /**
         * Creates a ConflictResult indicating the target session is full, with alternative suggestions.
         *
         * @param alternatives list of alternative sessions
         * @return a ConflictResult with sessionFull=true
         */
        public static ConflictResult sessionFull(List<AlternativeSession> alternatives) {
            ConflictResult result = new ConflictResult();
            result.hasConflict = false;
            result.sessionFull = true;
            result.conflictMessage = "Session complete";
            result.alternatives = alternatives;
            return result;
        }
    }

    /**
     * Represents an alternative session suggestion when a student's preferred session is full.
     */
    public static class AlternativeSession {
        public int sessionId;
        public String dominanteName;
        public String title;
        public String date;
        public String startTime;
        public String endTime;
        public String room;
        public int availablePlaces;
    }

    /**
     * Checks whether a student can register for a session, verifying time conflicts and capacity.
     *
     * @param campaignId the campaign ID
     * @param studentId  the student ID
     * @param sessionId  the session ID to check
     * @return ConflictResult indicating no conflict, a time conflict, or a full session with alternatives
     */
    public ConflictResult checkRegistration(int campaignId, int studentId, int sessionId) {
        SessionSlot targetSession = sessionDAO.findById(sessionId);
        if (targetSession == null) {
            return ConflictResult.withConflict("Session introuvable");
        }

        List<Registration> studentRegistrations = registrationDAO.findByStudentAndCampaign(campaignId, studentId);
        List<SessionSlot> allSessions = sessionDAO.findByCampaign(campaignId);
        
        for (Registration reg : studentRegistrations) {
            if (!"ALLOCATED".equals(reg.getStatus()) && !"WAITLIST".equals(reg.getStatus())) {
                continue;
            }
            
            SessionSlot existingSession = findSessionById(allSessions, reg.getSessionId());
            if (existingSession == null) continue;

            if (hasTimeConflict(existingSession, targetSession)) {
                String conflictMsg = String.format("Conflit d'horaire avec %s (%s)",
                        existingSession.getTitle(),
                        existingSession.getSessionDate() + " " + formatMinute(existingSession.getStartMinute()));
                return ConflictResult.withConflict(conflictMsg);
            }
        }

        int allocated = registrationDAO.countAllocatedBySession(campaignId, sessionId);
        if (allocated >= targetSession.getCapacity()) {
            List<AlternativeSession> alternatives = findAlternativeSessionsForDom(campaignId, studentId, targetSession.getDominanteId(), sessionId);
            return ConflictResult.sessionFull(alternatives);
        }

        return ConflictResult.noConflict();
    }

    /**
     * Registers a student for a session after validating campaign state, student existence, and time conflicts.
     *
     * @param campaignId          the campaign ID
     * @param studentId           the student ID
     * @param sessionId           the session ID to register for
     * @param isAdminRegistration whether this registration bypasses the OPEN status check
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult registerStudent(int campaignId, int studentId, int sessionId, boolean isAdminRegistration) {
        Campaign campaign = campaignDAO.findById(campaignId);
        if (campaign == null) {
            return ServiceResult.fail("Campagne introuvable");
        }

        if (!isAdminRegistration) {
            if (!"OPEN".equals(campaign.getStatus())) {
                return ServiceResult.fail("Inscriptions fermees");
            }
        } else {
            if ("VALIDATED".equals(campaign.getStatus()) || "ARCHIVED".equals(campaign.getStatus())) {
                return ServiceResult.fail("Inscription impossible a cette phase");
            }
        }

        User student = userDAO.findById(studentId);
        if (student == null) {
            return ServiceResult.fail("Etudiant introuvable");
        }

        ConflictResult checkResult = checkRegistration(campaignId, studentId, sessionId);
        if (checkResult.hasConflict) {
            return ServiceResult.fail(checkResult.conflictMessage);
        }

        Registration registration = new Registration();
        registration.setCampaignId(campaignId);
        registration.setStudentId(studentId);
        registration.setSessionId(sessionId);
        registration.setStatus("ALLOCATED");

        int id = registrationDAO.create(registration);
        if (id <= 0) {
            return ServiceResult.fail("Echec de l'inscription");
        }

        CacheManager.invalidatePrefix("registration:");
        CacheManager.invalidatePrefix("choice:");
        CacheManager.invalidatePrefix("stats:");

        return ServiceResult.ok("Inscription effectuee");
    }

    /**
     * Registers a student for a session, automatically falling back to the first alternative
     * if the preferred session is full.
     *
     * @param campaignId         the campaign ID
     * @param studentId          the student ID
     * @param preferredSessionId the student's preferred session ID
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult registerStudentWithAlternative(int campaignId, int studentId, int preferredSessionId) {
        ConflictResult check = checkRegistration(campaignId, studentId, preferredSessionId);
        
        if (!check.hasConflict && !check.sessionFull) {
            return registerStudent(campaignId, studentId, preferredSessionId, false);
        }

        if (check.sessionFull && check.alternatives != null && !check.alternatives.isEmpty()) {
            AlternativeSession alt = check.alternatives.get(0);
            return registerStudent(campaignId, studentId, alt.sessionId, false);
        }

        if (check.hasConflict) {
            return ServiceResult.fail(check.conflictMessage);
        }

        return ServiceResult.fail("Aucune session disponible");
    }

    /**
     * Finds alternative sessions for a student within the same dominante,
     * excluding a specific session and those that have time conflicts or are full.
     *
     * @param campaignId       the campaign ID
     * @param studentId        the student ID
     * @param dominanteId      the dominante ID to match
     * @param excludeSessionId the session ID to exclude from results
     * @return list of alternative sessions sorted by available places descending
     */
    public List<AlternativeSession> findAlternativeSessionsForDom(int campaignId, int studentId, int dominanteId, int excludeSessionId) {
        List<AlternativeSession> alternatives = new ArrayList<>();

        List<SessionSlot> allSessions = sessionDAO.findByCampaign(campaignId);
        List<Registration> studentRegs = registrationDAO.findByStudentAndCampaign(campaignId, studentId);
        List<SessionSlot> studentSessions = new ArrayList<>();

        for (Registration reg : studentRegs) {
            if ("ALLOCATED".equals(reg.getStatus()) || "WAITLIST".equals(reg.getStatus())) {
                SessionSlot s = findSessionById(allSessions, reg.getSessionId());
                if (s != null) studentSessions.add(s);
            }
        }

        List<Dominante> dominantes = dominanteDAO.findAll();
        java.util.Map<Integer, Dominante> domMap = new java.util.HashMap<>();
        for (Dominante d : dominantes) {
            domMap.put(d.getId(), d);
        }

        for (SessionSlot session : allSessions) {
            if (session.getId() == excludeSessionId) continue;
            if (session.getDominanteId() != dominanteId) continue;

            boolean hasConflict = false;
            for (SessionSlot existing : studentSessions) {
                if (hasTimeConflict(existing, session)) {
                    hasConflict = true;
                    break;
                }
            }
            if (hasConflict) continue;

            int allocated = registrationDAO.countAllocatedBySession(campaignId, session.getId());
            if (allocated >= session.getCapacity()) continue;

            AlternativeSession alt = new AlternativeSession();
            alt.sessionId = session.getId();
            Dominante d = domMap.get(session.getDominanteId());
            alt.dominanteName = d != null ? d.getName() : "Dominante #" + session.getDominanteId();
            alt.title = session.getTitle();
            alt.date = session.getSessionDate();
            alt.startTime = formatMinute(session.getStartMinute());
            alt.endTime = formatMinute(session.getEndMinute());
            alt.room = session.getRoom();
            alt.availablePlaces = session.getCapacity() - allocated;

            alternatives.add(alt);
        }

        alternatives.sort((a, b) -> Integer.compare(b.availablePlaces, a.availablePlaces));

        return alternatives;
    }

    /**
     * Checks whether two sessions have an overlapping time on the same date.
     *
     * @param s1 the first session
     * @param s2 the second session
     * @return true if the sessions overlap in time on the same date, false otherwise
     */
    private boolean hasTimeConflict(SessionSlot s1, SessionSlot s2) {
        if (s1.getSessionDate() == null || s2.getSessionDate() == null) return false;
        if (!s1.getSessionDate().equals(s2.getSessionDate())) return false;

        return s1.getStartMinute() < s2.getEndMinute() && s2.getStartMinute() < s1.getEndMinute();
    }

    /**
     * Finds a session by its ID from a given list of sessions.
     *
     * @param sessions the list of sessions to search
     * @param id       the session ID to find
     * @return the matching SessionSlot, or null if not found
     */
    private SessionSlot findSessionById(List<SessionSlot> sessions, int id) {
        for (SessionSlot s : sessions) {
            if (s.getId() == id) return s;
        }
        return null;
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

    /**
     * Retrieves all registrations for a student in a campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @param studentId  the student ID
     * @return list of registrations
     */
    public List<Registration> getStudentRegistrations(int campaignId, int studentId) {
        return CacheManager.getOrLoad("registration:student:" + campaignId + ":" + studentId,
                () -> registrationDAO.findByStudentAndCampaign(campaignId, studentId));
    }

    /**
     * Checks whether a session's capacity can be updated without dropping existing allocations.
     *
     * @param sessionId   the session ID
     * @param newCapacity the proposed new capacity
     * @param campaignId  the campaign ID
     * @return true if the capacity can be updated, false otherwise
     */
    public boolean canUpdateSessionCapacity(int sessionId, int newCapacity, int campaignId) {
        if (newCapacity <= 0) return false;
        SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) return false;
        
        int allocated = registrationDAO.countAllocatedBySession(campaignId, sessionId);
        return newCapacity >= allocated;
    }

    /**
     * Updates the capacity of a session; fails if the new capacity is lower than the current allocation count.
     *
     * @param sessionId   the session ID
     * @param newCapacity the desired new capacity
     * @param campaignId  the campaign ID
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult updateSessionCapacity(int sessionId, int newCapacity, int campaignId) {
        SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) {
            return ServiceResult.fail("Session introuvable");
        }

        int allocated = registrationDAO.countAllocatedBySession(campaignId, sessionId);
        if (newCapacity < allocated) {
            return ServiceResult.fail("La capacite (" + newCapacity + ") est inferieure aux inscriptions existantes (" + allocated + ")");
        }

        session.setCapacity(newCapacity);
        boolean success = sessionDAO.update(session);
        if (success) {
            return ServiceResult.ok("Capacite mise a jour");
        }
        return ServiceResult.fail("Echec de la mise a jour");
    }

    /**
     * Retrieves all students belonging to a given promotional year.
     *
     * @param promo the promotional year filter
     * @return list of students in the given promo
     */
    public List<User> getStudentsByPromo(String promo) {
        return userDAO.findStudentsByPromo(promo);
    }
}