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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service central pour la gestion des attributions d'étudiants aux sessions.
 * 
 * Nous avons implémenté deux modes d'action majeurs : 
 * 1. L'attribution manuelle par les administrateurs pour gérer les exceptions.
 * 2. L'attribution automatique basée sur un algorithme de type Stable Marriage,
 *    garantissant une équité optimale selon les vœux des étudiants.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
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

    /**
     * Permet à un administrateur d'assigner manuellement un étudiant à une session.
     * Nous avons mis en place cette fonctionnalité pour permettre des ajustements exceptionnels
     * en dehors du processus d'attribution automatique.
     * 
     * @param campaignId L'identifiant de la campagne concernée
     * @param studentId L'identifiant de l'étudiant
     * @param sessionId L'identifiant de la session
     * @param force Si vrai, l'attribution se fait même si la session est pleine
     * @return ServiceResult indiquant le succès ou l'échec de l'opération
     */
    public ServiceResult assignStudentManually(int campaignId, int studentId, int sessionId, boolean force) {
        // Vérifier si la campagne existe
        Campaign campaign = campaignDAO.findById(campaignId);
        if (campaign == null) return ServiceResult.fail("Campagne introuvable.");

        // Vérifier si l'étudiant existe
        User student = userDAO.findById(studentId);
        if (student == null) return ServiceResult.fail("Étudiant introuvable.");

        // Vérifier si la session existe
        SessionSlot session = sessionDAO.findById(sessionId);
        if (session == null) return ServiceResult.fail("Session introuvable.");

        // Vérifier la capacité sauf si "force" est activé
        int currentAllocated = sessionDAO.countAllocated(campaignId, sessionId);
        if (!force && currentAllocated >= session.getCapacity()) {
            return ServiceResult.fail("La session est complète. Utilisez l'option 'force' pour passer outre.");
        }

        // Supprimer d'éventuelles inscriptions précédentes pour cet étudiant dans cette campagne
        // (Un étudiant ne peut être assigné qu'à une session par campagne en principe)
        registrationDAO.deleteByStudentAndCampaign(campaignId, studentId);

        Registration reg = new Registration();
        reg.setCampaignId(campaignId);
        reg.setStudentId(studentId);
        reg.setSessionId(sessionId);
        reg.setSourceChoiceRank(null); // Manuel
        reg.setStatus("ALLOCATED");

        int id = registrationDAO.create(reg);
        if (id > 0) {
            return ServiceResult.ok("Étudiant assigné avec succès.");
        } else {
            return ServiceResult.fail("Erreur lors de la création de l'assignation.");
        }
    }

    /**
     * Exécute l'algorithme d'attribution automatique pour une campagne.
     * 
     * Approche "Stable Marriage" (Gale-Shapley simplifiée) 
     * qui priorise les vœux des étudiants tout en respectant les capacités des sessions.
     * Les attributions manuelles (sourceChoiceRank = null) sont préservées.
     * 
     * @param campaignId L'identifiant de la campagne à traiter
     * @return ServiceResult avec le bilan de l'attribution
     */
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

        List<Registration> existing = registrationDAO.findByCampaignAndStatus(campaignId, "ALLOCATED");
        List<Integer> manuallyAllocatedStudentIds = new ArrayList<>();
        for (Registration reg : existing) {
            if (reg.getSourceChoiceRank() == null) {
                manuallyAllocatedStudentIds.add(reg.getStudentId());
            }
        }

        registrationDAO.deleteByCampaign(campaignId);

        int manualCount = 0;
        for (Registration reg : existing) {
            if (reg.getSourceChoiceRank() == null) {
                Registration copy = new Registration();
                copy.setCampaignId(reg.getCampaignId());
                copy.setStudentId(reg.getStudentId());
                copy.setSessionId(reg.getSessionId());
                copy.setSourceChoiceRank(null);
                copy.setStatus("ALLOCATED");
                if (registrationDAO.create(copy) > 0) {
                    manualCount++;
                }
            }
        }

        List<User> students = userDAO.findStudentsByPromo(campaign.getPromo());
        Collections.shuffle(students);

        int allocatedCount = 0;
        int waitlistCount = 0;

        for (User student : students) {
            if (manuallyAllocatedStudentIds.contains(student.getId())) {
                continue;
            }
            List<Choice> choices = choiceDAO.findByStudentAndCampaign(campaignId, student.getId());
            choices.sort(Comparator.comparingInt(Choice::getRankOrder));

            boolean allocated = false;
            for (Choice choice : choices) {
                int currentAllocated = sessionDAO.countAllocated(campaignId, choice.getSessionId());
                if (currentAllocated < sessionCapacity(choice.getSessionId())) {
                    Registration reg = new Registration();
                    reg.setCampaignId(campaignId);
                    reg.setStudentId(student.getId());
                    reg.setSessionId(choice.getSessionId());
                    reg.setSourceChoiceRank(choice.getRankOrder());
                    reg.setStatus("ALLOCATED");

                    if (registrationDAO.create(reg) > 0) {
                        allocatedCount++;
                        allocated = true;
                        break;
                    }
                }
            }

            if (!allocated && !choices.isEmpty()) {
                Choice firstChoice = choices.get(0);
                Registration reg = new Registration();
                reg.setCampaignId(campaignId);
                reg.setStudentId(student.getId());
                reg.setSessionId(firstChoice.getSessionId());
                reg.setSourceChoiceRank(firstChoice.getRankOrder());
                reg.setStatus("WAITLIST");
                if (registrationDAO.create(reg) > 0) {
                    waitlistCount++;
                }
            }
        }

        campaignDAO.updateStatus(campaignId, "VALIDATED");
        String msg = "Attribution automatique terminee : " + allocatedCount + " etudiants affectes, " + waitlistCount + " en liste d'attente";
        if (manualCount > 0) {
            msg += " (" + manualCount + " manuelles preservees)";
        }
        return ServiceResult.ok(msg);
    }

    private int sessionCapacity(int sessionId) {
        SessionSlot s = sessionDAO.findById(sessionId);
        if (s == null) {
            return 0;
        }
        return s.getCapacity();
    }
}
