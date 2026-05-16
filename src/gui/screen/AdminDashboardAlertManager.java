package gui.screen;

import gui.components.AlertContainer;
import java.util.ArrayList;
import java.util.List;
import model.Campaign;
import service.StatisticsService;

/**
 * Gestionnaire d'alertes pour le tableau de bord administrateur.
 * Génère des alertes contextuelles basées sur l'état de la campagne.
 * Fournit des méthodes statiques réutilisables par les composants dashboard.
 */
public class AdminDashboardAlertManager {
    
    /**
     * Génère la liste des alertes pour une campagne donnée.
     * @param campaign La campagne active (peut être null)
     * @param statisticsService Le service de statistiques
     * @return Liste d'alertes à afficher
     */
    public static List<AlertContainer.AlertItem> generateAlerts(Campaign campaign, StatisticsService statisticsService) {
        List<AlertContainer.AlertItem> alerts = new ArrayList<>();
        if (campaign == null) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Aucune campagne active", "Creez une campagne."));
            return alerts;
        }
        String status = campaign.getStatus();
        if ("PREPARATION".equals(status))
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.INFO, "Phase PREPARATION", "Inscriptions pas encore ouvertes."));
        else if ("OPEN".equals(status))
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.INFO, "Phase OPEN", "Inscriptions ouvertes."));
        else if ("CLOSED".equals(status))
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Phase CLOSED", "Inscriptions fermees."));
        else if ("PROCESSING".equals(status))
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Phase PROCESSING", "Traitement en cours."));

        StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(campaign.getId(), campaign.getPromo());
        if (stats.unregisteredStudents > 0)
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, stats.unregisteredStudents + " etudiants non inscrits", "Consultez Statistiques."));
        if (stats.averageFillRate >= 95)
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.ERROR, "Sessions quasi completes", String.format("%.0f%%", stats.averageFillRate)));
        if (stats.totalSessions == 0)
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Aucune session", "Ajoutez des sessions."));
        return alerts;
    }
}