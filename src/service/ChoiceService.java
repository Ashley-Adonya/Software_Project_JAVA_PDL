package service;

import dao.CampaignDAO;
import dao.ChoiceDAO;
import dao.SessionDAO;
import dao.UserDAO;
import model.Campaign;
import model.Choice;
import model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing student choices regarding presentation sessions.
 * Allows students to create, modify and consult their session preferences
 * with validation of duplicates and time conflict constraints.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class ChoiceService {
    private final ChoiceDAO choiceDAO;
    private final CampaignDAO campaignDAO;
    private final UserDAO userDAO;
    private final RegistrationService registrationService;
    private final SessionDAO sessionDAO;

    /**
     * Default constructor initializing DAOs and the registration service for choice management.
     */
    public ChoiceService() {
        this.choiceDAO = new ChoiceDAO();
        this.campaignDAO = new CampaignDAO();
        this.userDAO = new UserDAO();
        this.sessionDAO = new SessionDAO();
        this.registrationService = new RegistrationService();
    }

    /**
     * Replaces all choices for a student in a given campaign with a new list.
     * Validates campaign status, student eligibility, rank/session uniqueness, and scheduling conflicts.
     *
     * @param campaignId the ID of the campaign
     * @param studentId  the ID of the student
     * @param newChoices the new list of choices to save
     * @return ServiceResult indicating success or failure with a descriptive message
     */
    public ServiceResult replaceStudentChoices(int campaignId, int studentId, List<Choice> newChoices) {
        Campaign campaign = campaignDAO.findById(campaignId);
        if (campaign == null) {
            return ServiceResult.fail("Campagne introuvable");
        }
        if (!"OPEN".equals(campaign.getStatus())) {
            return ServiceResult.fail("Les choix sont possibles uniquement quand la campagne est OPEN");
        }

        User student = userDAO.findById(studentId);
        if (student == null || !"STUDENT".equals(student.getRole())) {
            return ServiceResult.fail("Etudiant invalide");
        }
        if (!campaign.getPromo().equals(student.getPromo())) {
            return ServiceResult.fail("L'etudiant n'appartient pas a la promo de la campagne");
        }

        if (newChoices == null || newChoices.isEmpty()) {
             int removed = choiceDAO.deleteByStudentAndCampaign(campaignId, studentId);
            return ServiceResult.ok("Choix supprimes (" + removed + ")");
        }
        if (newChoices.size() > campaign.getMaxChoices()) {
            return ServiceResult.fail("Nombre de choix superieur a max_choices");
        }

        Set<Integer> ranks = new HashSet<Integer>();
        Set<Integer> sessions = new HashSet<Integer>();
        for (int i = 0; i < newChoices.size(); i++) {
            Choice c = newChoices.get(i);
            if (c.getRankOrder() <= 0) {
                return ServiceResult.fail("Rang invalide");
            }
            if (ranks.contains(Integer.valueOf(c.getRankOrder()))) {
                return ServiceResult.fail("Rang duplique");
            }
            if (sessions.contains(Integer.valueOf(c.getSessionId()))) {
                return ServiceResult.fail("Session dupliquee");
            }
            RegistrationService.ConflictResult conflictCheck = registrationService.checkRegistration(campaignId, studentId, c.getSessionId());
            if (conflictCheck.hasConflict) {
                return ServiceResult.fail("Conflit: " + conflictCheck.conflictMessage);
            }
            if (conflictCheck.sessionFull && (conflictCheck.alternatives == null || conflictCheck.alternatives.isEmpty())) {
                return ServiceResult.fail("Session complete et aucune alternative disponible");
            }
            ranks.add(Integer.valueOf(c.getRankOrder()));
            sessions.add(Integer.valueOf(c.getSessionId()));
            c.setCampaignId(campaignId);
            c.setStudentId(studentId);
        }

        boolean ok = choiceDAO.replaceStudentChoices(campaignId, studentId, newChoices);
        if (!ok) {
            return ServiceResult.fail("Enregistrement des choix echoue");
        }
        CacheManager.invalidatePrefix("choice:");
        CacheManager.invalidatePrefix("registration:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Choix enregistres");
    }

    /**
     * Retrieves the list of choices submitted by a student for a given campaign, using cache.
     *
     * @param campaignId the ID of the campaign
     * @param studentId  the ID of the student
     * @return the list of choices for the student in the campaign
     */
    public List<Choice> getStudentChoices(int campaignId, int studentId) {
        return CacheManager.getOrLoad("choice:student:" + campaignId + ":" + studentId, () -> choiceDAO.findByStudentAndCampaign(campaignId, studentId));
    }

    /**
     * Finds alternative session slots for a student within the same domain when their preferred session is full.
     *
     * @param campaignId the ID of the campaign
     * @param studentId  the ID of the student
     * @param sessionId  the ID of the preferred session
     * @return a list of alternative sessions, or an empty list if none are available
     */
    public List<RegistrationService.AlternativeSession> getAlternativeSessions(int campaignId, int studentId, int sessionId) {
        model.SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) return new java.util.ArrayList<>();
        return registrationService.findAlternativeSessionsForDom(campaignId, studentId, session.getDominanteId(), sessionId);
    }
}
