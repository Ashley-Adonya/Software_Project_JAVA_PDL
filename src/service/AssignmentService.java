package service;

import dao.CampaignDAO;
import dao.ChoiceDAO;
import dao.RegistrationDAO;
import dao.SessionDAO;
import dao.UserDAO;
import model.Campaign;
import model.Choice;
import model.Registration;
import model.SessionSlot;
import model.User;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AssignmentService {
    private final CampaignDAO campaignDAO;
    private final ChoiceDAO choiceDAO;
    private final RegistrationDAO registrationDAO;
    private final SessionDAO sessionDAO;
    private final UserDAO userDAO;

    public AssignmentService() {
        this.campaignDAO = new CampaignDAO();
        this.choiceDAO = new ChoiceDAO();
        this.registrationDAO = new RegistrationDAO();
        this.sessionDAO = new SessionDAO();
        this.userDAO = new UserDAO();
    }

    public ServiceResult runAutoAssignment(int campaignId) {
        Campaign campaign = campaignDAO.findById(campaignId);
        if (campaign == null) {
            return ServiceResult.fail("Campagne introuvable");
        }
        if (!"CLOSED".equals(campaign.getStatus())) {
            return ServiceResult.fail("Le traitement auto doit partir de l'etat CLOSED");
        }

        boolean statusOk = campaignDAO.updateStatus(campaignId, "PROCESSING");
        if (!statusOk) {
            return ServiceResult.fail("Impossible de passer en PROCESSING");
        }

        registrationDAO.deleteByCampaign(campaignId);

        List<User> students = userDAO.findStudentsByPromo(campaign.getPromo());
        int allocatedCount = 0;
        int waitlistCount = 0;

        for (int i = 0; i < students.size(); i++) {
            User student = students.get(i);
            List<Choice> choices = choiceDAO.findByStudentAndCampaign(campaignId, student.getId());
            Collections.sort(choices, new Comparator<Choice>() {
                public int compare(Choice a, Choice b) {
                    return Integer.compare(a.getRankOrder(), b.getRankOrder());
                }
            });

            for (int j = 0; j < choices.size(); j++) {
                Choice choice = choices.get(j);
                int allocatedInSession = sessionDAO.countAllocated(campaignId, choice.getSessionId());
                Registration reg = new Registration();
                reg.setCampaignId(campaignId);
                reg.setStudentId(student.getId());
                reg.setSessionId(choice.getSessionId());
                reg.setSourceChoiceRank(Integer.valueOf(choice.getRankOrder()));

                if (allocatedInSession < sessionCapacity(choice.getSessionId())) {
                    reg.setStatus("ALLOCATED");
                    int id = registrationDAO.create(reg);
                    if (id > 0) {
                        allocatedCount++;
                    }
                } else {
                    reg.setStatus("WAITLIST");
                    int id = registrationDAO.create(reg);
                    if (id > 0) {
                        waitlistCount++;
                    }
                }
            }
        }

        campaignDAO.updateStatus(campaignId, "VALIDATED");
        return ServiceResult.ok("Traitement termine: " + allocatedCount + " ALLOCATED, " + waitlistCount + " WAITLIST");
    }

    private int sessionCapacity(int sessionId) {
        SessionSlot s = sessionDAO.findById(sessionId);
        if (s == null) {
            return 0;
        }
        return s.getCapacity();
    }
}
