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
 * Service de gestion des choix d'étudiants concernant les sessions de présentation.
 * Permet aux étudiants de créer, modifier et consulter leurs préférences de sessions
 * avec validation des doublons et contraintes de conflits horaires.
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

    public ChoiceService() {
        this.choiceDAO = new ChoiceDAO();
        this.campaignDAO = new CampaignDAO();
        this.userDAO = new UserDAO();
        this.sessionDAO = new SessionDAO();
        this.registrationService = new RegistrationService();
    }

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

    public List<Choice> getStudentChoices(int campaignId, int studentId) {
        return CacheManager.getOrLoad("choice:student:" + campaignId + ":" + studentId, () -> choiceDAO.findByStudentAndCampaign(campaignId, studentId));
    }

    public List<RegistrationService.AlternativeSession> getAlternativeSessions(int campaignId, int studentId, int sessionId) {
        model.SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) return new java.util.ArrayList<>();
        return registrationService.findAlternativeSessionsForDom(campaignId, studentId, session.getDominanteId(), sessionId);
    }
}
