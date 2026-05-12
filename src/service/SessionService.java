package service;

import dao.CampaignDAO;
import dao.SessionDAO;
import model.Campaign;
import model.SessionSlot;
import java.util.Map;
import java.util.List;

public class SessionService {
    private final SessionDAO sessionDAO;
    private final CampaignDAO campaignDAO;

    public SessionService() {
        this.sessionDAO = new SessionDAO();
        this.campaignDAO = new CampaignDAO();
    }

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
        return ServiceResult.ok("Session creee (id=" + id + ")");
    }

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
        return ServiceResult.ok("Session modifiee");
    }

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
        return ServiceResult.ok("Session supprimee");
    }

    public List<SessionSlot> listByCampaign(int campaignId) {
        return sessionDAO.findByCampaign(campaignId);
    }

    public List<SessionSlot> searchSessions(int campaignId, String dominanteNameLike, int fromMinute, int toMinute) {
        return sessionDAO.searchByDominanteNameAndTime(campaignId, dominanteNameLike, fromMinute, toMinute);
    }

    public Map<Integer, Integer> countAllocationsBySessionForCampaign(int campaignId) {
        return sessionDAO.countBySessionForCampaign(campaignId);
    }
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

    private boolean isInsideAllowedWindow(int startMinute, int endMinute) {
        boolean morning = startMinute >= 510 && endMinute <= 750;
        boolean afternoon = startMinute >= 810 && endMinute <= 1050;
        return morning || afternoon;
    }
}
