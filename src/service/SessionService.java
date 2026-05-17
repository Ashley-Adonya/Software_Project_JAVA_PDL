package service;

import dao.CampaignDAO;
import dao.RegistrationDAO;
import dao.SessionDAO;
import model.Campaign;
import model.SessionSlot;
import java.util.Map;
import java.util.List;

/**
 * Service for managing dominant presentation sessions.
 * Offers creation, modification, deletion and search operations for sessions
 * with respect to campaign states and capacity validation.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class SessionService {
    private final SessionDAO sessionDAO;
    private final CampaignDAO campaignDAO;
    private final RegistrationDAO registrationDAO;

    /**
     * Constructs a new SessionService with default DAO implementations.
     */
    public SessionService() {
        this.sessionDAO = new SessionDAO();
        this.campaignDAO = new CampaignDAO();
        this.registrationDAO = new RegistrationDAO();
    }

    /**
     * Creates a new session after validating its fields and checking that the campaign is in PREPARATION status.
     *
     * @param session the session to create
     * @return ServiceResult indicating success with the new ID, or failure
     */
    public ServiceResult createSession(SessionSlot session) {
        String validation = validateSession(session);
        if (validation != null) {
            return ServiceResult.fail(validation);
        }

        Campaign c = campaignDAO.findById(session.getCampaignId());
        if (c == null) {
            return ServiceResult.fail("Campagne introuvable");
        }
        if (!"PREPARATION".equals(c.getStatus())) {
            return ServiceResult.fail("Creation autorisee uniquement en PREPARATION");
        }

        int id = sessionDAO.create(session);
        if (id <= 0) {
            return ServiceResult.fail("Creation session echouee");
        }
        CacheManager.invalidatePrefix("session:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Session creee (id=" + id + ")");
    }

    /**
     * Updates an existing session after validation, only allowed if the campaign is in PREPARATION status.
     *
     * @param session the session with updated data (must have a valid ID)
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult updateSession(SessionSlot session) {
        String validation = validateSession(session);
        if (validation != null) {
            return ServiceResult.fail(validation);
        }

        SessionSlot existing = sessionDAO.findById(session.getId());
        if (existing == null) {
            return ServiceResult.fail("Session introuvable");
        }

        Campaign c = campaignDAO.findById(existing.getCampaignId());
        if (c == null || !"PREPARATION".equals(c.getStatus())) {
            return ServiceResult.fail("Modification autorisee uniquement en PREPARATION");
        }

        boolean ok = sessionDAO.update(session);
        if (!ok) {
            return ServiceResult.fail("Modification session echouee");
        }
        CacheManager.invalidatePrefix("session:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Session modifiee");
    }

    /**
     * Deletes a session if it exists and its campaign is in PREPARATION status.
     *
     * @param sessionId the session ID to delete
     * @return ServiceResult indicating success or failure
     */
    public ServiceResult deleteSession(int sessionId) {
        SessionSlot existing = sessionDAO.findById(sessionId);
        if (existing == null) {
            return ServiceResult.fail("Session introuvable");
        }
        Campaign c = campaignDAO.findById(existing.getCampaignId());
        if (c == null || !"PREPARATION".equals(c.getStatus())) {
            return ServiceResult.fail("Suppression autorisee uniquement en PREPARATION");
        }

        boolean ok = sessionDAO.deleteById(sessionId);
        if (!ok) {
            return ServiceResult.fail("Suppression session echouee");
        }
        CacheManager.invalidatePrefix("session:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Session supprimee");
    }

    /**
     * Updates the capacity of a session if the new capacity is not lower than currently allocated registrations.
     *
     * @param sessionId   the session ID
     * @param newCapacity the new capacity value
     * @return true if the capacity was updated, false otherwise
     */
    public boolean updateCapacity(int sessionId, int newCapacity) {
        if (newCapacity < 0) return false;
        SessionSlot existing = sessionDAO.findById(sessionId);
        if (existing == null) return false;
        Campaign c = campaignDAO.findById(existing.getCampaignId());
        if (c == null) return false;
        String status = c.getStatus();
        if ("PREPARATION".equals(status) || "OPEN".equals(status) || "CLOSED".equals(status) || "PROCESSING".equals(status)) {
            int allocated = registrationDAO.countAllocatedBySession(c.getId(), sessionId);
            if (newCapacity < allocated) return false;
            boolean ok = sessionDAO.updateCapacity(sessionId, newCapacity);
            if (ok) CacheManager.invalidatePrefix("session:");
            return ok;
        }
        return false;
    }

    /**
     * Lists all sessions for a given campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @return list of session slots
     */
    public List<SessionSlot> listByCampaign(int campaignId) {
        return CacheManager.getOrLoad("session:campaign:" + campaignId, () -> sessionDAO.findByCampaign(campaignId));
    }

    /**
     * Searches sessions by campaign, dominante name pattern, and time range, with caching.
     *
     * @param campaignId        the campaign ID
     * @param dominanteNameLike a partial dominante name to match
     * @param fromMinute        start of the time range in minutes from midnight
     * @param toMinute          end of the time range in minutes from midnight
     * @return list of matching session slots
     */
    public List<SessionSlot> searchSessions(int campaignId, String dominanteNameLike, int fromMinute, int toMinute) {
        String key = "session:search:" + campaignId + ":" + safe(dominanteNameLike) + ":" + fromMinute + ":" + toMinute;
        return CacheManager.getOrLoad(key, () -> sessionDAO.searchByDominanteNameAndTime(campaignId, dominanteNameLike, fromMinute, toMinute));
    }

    /**
     * Retrieves a map of session IDs to allocated registration counts for a campaign, with caching.
     *
     * @param campaignId the campaign ID
     * @return map of session ID to allocation count
     */
    public Map<Integer, Integer> countAllocationsBySessionForCampaign(int campaignId) {
        return CacheManager.getOrLoad("session:allocations:" + campaignId, () -> sessionDAO.countBySessionForCampaign(campaignId));
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
     * Validates a session slot's fields (campaign, dominante, capacity, schedule).
     *
     * @param session the session to validate
     * @return an error message if validation fails, or null if valid
     */
    private String validateSession(SessionSlot session) {
        if (session == null) {
            return "Session vide";
        }
        if (session.getCampaignId() <= 0 || session.getDominanteId() <= 0) {
            return "Campagne ou dominante invalide";
        }
        if (session.getCapacity() <= 0) {
            return "Capacite invalide";
        }
        if (session.getStartMinute() < 0 || session.getEndMinute() < 0 || session.getEndMinute() <= session.getStartMinute()) {
            return "Horaire invalide";
        }
        if (!isInsideAllowedWindow(session.getStartMinute(), session.getEndMinute())) {
            return "Hors plage horaire (08:30-12:30, 13:30-17:30)";
        }
        return null;
    }

    /**
     * Checks if a time interval falls within the allowed time windows (morning 08:30-12:30 or afternoon 13:30-17:30).
     *
     * @param startMinute start time in minutes from midnight
     * @param endMinute   end time in minutes from midnight
     * @return true if the interval is within an allowed window, false otherwise
     */
    private boolean isInsideAllowedWindow(int startMinute, int endMinute) {
        boolean morning = startMinute >= 510 && endMinute <= 750;
        boolean afternoon = startMinute >= 810 && endMinute <= 1050;
        return morning || afternoon;
    }
}
