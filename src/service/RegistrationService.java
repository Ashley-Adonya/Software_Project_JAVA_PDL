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

public class RegistrationService {
    private final RegistrationDAO registrationDAO;
    private final SessionDAO sessionDAO;
    private final UserDAO userDAO;
    private final CampaignDAO campaignDAO;
    private final DominanteDAO dominanteDAO;

    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
        this.sessionDAO = new SessionDAO();
        this.userDAO = new UserDAO();
        this.campaignDAO = new CampaignDAO();
        this.dominanteDAO = new DominanteDAO();
    }

    public static class ConflictResult {
        public boolean hasConflict;
        public String conflictMessage;
        public boolean sessionFull;
        public List<AlternativeSession> alternatives;

        public static ConflictResult noConflict() {
            ConflictResult result = new ConflictResult();
            result.hasConflict = false;
            result.sessionFull = false;
            result.alternatives = new ArrayList<>();
            return result;
        }

        public static ConflictResult withConflict(String message) {
            ConflictResult result = new ConflictResult();
            result.hasConflict = true;
            result.conflictMessage = message;
            result.sessionFull = false;
            result.alternatives = new ArrayList<>();
            return result;
        }

        public static ConflictResult sessionFull(List<AlternativeSession> alternatives) {
            ConflictResult result = new ConflictResult();
            result.hasConflict = false;
            result.sessionFull = true;
            result.conflictMessage = "Session complete";
            result.alternatives = alternatives;
            return result;
        }
    }

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
            List<AlternativeSession> alternatives = findAlternativeSessions(campaignId, studentId, targetSession);
            return ConflictResult.sessionFull(alternatives);
        }

        return ConflictResult.noConflict();
    }

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

        return ServiceResult.ok("Inscription effectuee");
    }

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

    public List<AlternativeSession> findAlternativeSessions(int campaignId, int studentId, SessionSlot excludeSession) {
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
            if (session.getId() == excludeSession.getId()) continue;
            if (session.getDominanteId() != excludeSession.getDominanteId()) continue;

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

    private boolean hasTimeConflict(SessionSlot s1, SessionSlot s2) {
        if (s1.getSessionDate() == null || s2.getSessionDate() == null) return false;
        if (!s1.getSessionDate().equals(s2.getSessionDate())) return false;

        return s1.getStartMinute() < s2.getEndMinute() && s2.getStartMinute() < s1.getEndMinute();
    }

    private SessionSlot findSessionById(List<SessionSlot> sessions, int id) {
        for (SessionSlot s : sessions) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }

    public List<Registration> getStudentRegistrations(int campaignId, int studentId) {
        return registrationDAO.findByStudentAndCampaign(campaignId, studentId);
    }

    public boolean canUpdateSessionCapacity(int sessionId, int newCapacity, int campaignId) {
        if (newCapacity <= 0) return false;
        SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) return false;
        
        int allocated = registrationDAO.countAllocatedBySession(campaignId, sessionId);
        return newCapacity >= allocated;
    }

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

    public List<User> getStudentsByPromo(String promo) {
        return userDAO.findStudentsByPromo(promo);
    }
}