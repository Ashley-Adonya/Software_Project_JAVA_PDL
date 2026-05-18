package gui.screen;

import java.util.ArrayList;
import java.util.List;
import gui.components.AlertContainer;
import model.Campaign;
import service.StatisticsService;

/**
 * Generates contextual alert items for the admin dashboard based on the state
 * of the active campaign and live statistical data.
 * <p>
 * Alerts cover the following conditions:
 * <ul>
 *   <li>Missing or null campaign</li>
 *   <li>Current campaign phase (PREPARATION, OPEN, CLOSED, PROCESSING)</li>
 *   <li>Presence of unregistered students</li>
 *   <li>High session fill rate (≥95%)</li>
 *   <li>Absence of any sessions</li>
 * </ul>
 * All methods are static, making them reusable by any dashboard component
 * without requiring an instance of this manager.
 * </p>
 */
public class AdminDashboardAlertManager {
    
    /**
     * Generates a complete list of alert items for the given campaign.
     * <p>
     * If the campaign is {@code null}, a single warning alert ("No active campaign")
     * is returned. Otherwise, alerts are produced for the campaign's current phase
     * status and for noteworthy statistics (e.g. unregistered students, high fill
     * rate, zero sessions).
     * </p>
     *
     * @param campaign          the currently active campaign, or {@code null} if none exists
     * @param statisticsService the service providing aggregated statistics for the campaign
     * @return a list of {@link AlertContainer.AlertItem} instances to be rendered on the dashboard
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