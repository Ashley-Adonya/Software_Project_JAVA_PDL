package service;

import dao.CampaignDAO;
import dao.ChoiceDAO;
import dao.UserDAO;
import model.Campaign;
import model.Choice;
import model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChoiceService {
    private final ChoiceDAO choiceDAO;
    private final CampaignDAO campaignDAO;
    private final UserDAO userDAO;

    public ChoiceService() {
        this.choiceDAO = new ChoiceDAO();
        this.campaignDAO = new CampaignDAO();
        this.userDAO = new UserDAO();
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
            return ServiceResult.fail("Aucun choix fourni");
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
            ranks.add(Integer.valueOf(c.getRankOrder()));
            sessions.add(Integer.valueOf(c.getSessionId()));
            c.setCampaignId(campaignId);
            c.setStudentId(studentId);
        }

        boolean ok = choiceDAO.replaceStudentChoices(campaignId, studentId, newChoices);
        if (!ok) {
            return ServiceResult.fail("Enregistrement des choix echoue");
        }
        return ServiceResult.ok("Choix enregistres");
    }

    public List<Choice> getStudentChoices(int campaignId, int studentId) {
        return choiceDAO.findByStudentAndCampaign(campaignId, studentId);
    }
}
