package gui.screen;

import gui.components.AlertContainer;
import java.util.ArrayList;
import java.util.List;
import model.Campaign;
import model.User;
import service.StatisticsService;

/**
 * Gestion des alertes du tableau de bord administrateur.
 * Responsable de la génération et de l'affichage des alertes contextuelles
 * en fonction de l'état de la campagne et des statistiques.
 */
public class AdminDashboardAlertManager {
    
    private final AdminDashboardView view;
    private final StatisticsService statisticsService;
    private AlertContainer dashboardAlertContainer;
    
    public AdminDashboardAlertManager(AdminDashboardView view, StatisticsService statisticsService) {
        this.view = view;
        this.statisticsService = statisticsService;
    }
    
    public void setAlertContainer(AlertContainer alertContainer) {
        this.dashboardAlertContainer = alertContainer;
    }
    
    /**
     * Génère la liste des alertes à afficher basée sur l'état actuel de la campagne.
     * @return Liste des alertes à afficher
     */
    public List<AlertContainer.AlertItem> generateDashboardAlerts() {
        List<AlertContainer.AlertItem> alerts = new ArrayList<>();
        Campaign activeCampaign = view.getActiveCampaign();
        
        if (activeCampaign == null) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.WARNING, 
                "Aucune campagne active", 
                "Creez une campagne pour commencer."
            ));
            return alerts;
        }

        String status = activeCampaign.getStatus();
        if ("PREPARATION".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.INFO, 
                "Phase PREPARATION", 
                "Les inscriptions ne sont pas encore ouvertes."
            ));
        } else if ("OPEN".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.INFO, 
                "Phase OPEN", 
                "Les inscriptions sont ouvertes aux etudiants."
            ));
        } else if ("CLOSED".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.WARNING, 
                "Phase CLOSED", 
                "Les inscriptions sont fermees."
            ));
        } else if ("PROCESSING".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.WARNING, 
                "Phase PROCESSING", 
                "Traitement des allocations en cours."
            ));
        }

        StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(
            activeCampaign.getId(), 
            view.getUserPromo()
        );
        
        if (stats.unregisteredStudents > 0) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.WARNING, 
                stats.unregisteredStudents + " etudiants non inscrits", 
                "Consultez la page Statistiques."
            ));
        }
        if (stats.averageFillRate >= 95) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.ERROR, 
                "Sessions quasi completes", 
                "Taux de remplissage: " + String.format("%.0f%%", stats.averageFillRate)
            ));
        }
        if (stats.totalSessions == 0) {
            alerts.add(new AlertContainer.AlertItem(
                AlertContainer.AlertType.WARNING, 
                "Aucune session definie", 
                "Ajoutez des sessions dans la section Sessions."
            ));
        }
        return alerts;
    }
    
    /**
     * Rafraîchit l'affichage des alertes dans le conteneur d'alertes.
     */
    public void refreshAlerts() {
        if (dashboardAlertContainer != null) {
            List<AlertContainer.AlertItem> alerts = generateDashboardAlerts();
            dashboardAlertContainer.setAlerts(alerts);
        }
    }
}