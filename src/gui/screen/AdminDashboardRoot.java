package gui.screen;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import components.Button;
import components.FormModal;
import components.Label;
import gui.components.KpiCard;
import gui.components.PrimaryButton;
import gui.components.SurfaceCard;
import gui.screen.components.CampaignFormComponent;
import gui.screen.components.DominanteListComponent;
import gui.screen.components.SessionListComponent;
import gui.screen.components.StatsPanelComponent;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.Dominante;
import model.SessionSlot;
import model.User;
import service.CampaignService;
import service.CacheManager;
import service.SessionService;
import service.DominanteService;
import service.StatisticsService;
import service.RegistrationService;
import gui.components.AlertContainer;
import gui.components.ReusableLabeledInput;
import gui.components.ColorPicker;
import dao.RegistrationDAO;
import components.SelectInput;
import components.TextField;
import event.UiEvent;
import service.ServiceResult;
import components.ScrollView;

/**
 * Composant racine de la section tableau de bord.
 * Contient les cartes d'information, les raccourcis, les alertes et la note explicative.
 */
public class AdminDashboardRoot extends BaseComp {

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // UI components
    private SurfaceCard dashboardHeroCard;
    private SurfaceCard dashboardNoteCard;
    private SurfaceCard dashboardKpiCard;
    private SurfaceCard dashboardShortcutCard;
    private AlertContainer dashboardAlertContainer;
    private Label dashboardSubtitleLabel;
    private Label dashboardStatusLabel;
    private KpiCard dashboardSessionsKpi;
    private KpiCard dashboardDominantesKpi;
    private KpiCard dashboardFillKpi;
    private KpiCard dashboardStudentsKpi;
    private PrimaryButton dashboardGoDominantes;
    private PrimaryButton dashboardGoSessions;
    private PrimaryButton dashboardGoCampagne;
    private PrimaryButton dashboardGoStats;

    private final StatisticsService statisticsService;

    public AdminDashboardRoot(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        initializeComponents();
        buildLayout();
    }

    private void initializeComponents() {
        dashboardHeroCard = new SurfaceCard(0, 0, 100, 150, new Color(18, 24, 35), new Color(52, 63, 92), 14);
        Label title = new Label("Vue d'ensemble", 18, 18, 400, 24);
        title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 22));
        title.setColor(new Color(237, 242, 252));

        dashboardSubtitleLabel = new Label("Chargement de la campagne active...", 18, 48, 560, 22);
        dashboardSubtitleLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 13));
        dashboardSubtitleLabel.setColor(new Color(160, 173, 200));

        dashboardStatusLabel = new Label("Tableau central pour piloter les dominantes, sessions et statistiques.", 18, 76, 620, 20);
        dashboardStatusLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        dashboardStatusLabel.setColor(new Color(125, 140, 168));

        dashboardKpiCard = new SurfaceCard(0, 0, 100, 120, new Color(22, 28, 39), new Color(52, 63, 92), 14);
        dashboardSessionsKpi = new KpiCard("Sessions", "0", "Total", new Color(59, 130, 246));
        dashboardDominantesKpi = new KpiCard("Dominantes", "0", "Actives", new Color(168, 85, 247));
        dashboardFillKpi = new KpiCard("Remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        dashboardStudentsKpi = new KpiCard("Inscrits", "0", "Total", new Color(34, 197, 94));
        dashboardKpiCard.addChild(dashboardSessionsKpi);
        dashboardKpiCard.addChild(dashboardDominantesKpi);
        dashboardKpiCard.addChild(dashboardFillKpi);
        dashboardKpiCard.addChild(dashboardStudentsKpi);

        dashboardShortcutCard = new SurfaceCard(0, 0, 100, 96, new Color(23, 30, 45), new Color(52, 63, 92), 12);
        Label shortcutTitle = new Label("Raccourcis", 16, 12, 140, 18);
        shortcutTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        shortcutTitle.setColor(new Color(210, 219, 237));
        dashboardGoDominantes = new PrimaryButton("Dominantes", 16, 38, 112, 32, null); // Action set later
        dashboardGoSessions = new PrimaryButton("Sessions", 134, 38, 100, 32, null);
        dashboardGoCampagne = new PrimaryButton("Campagne", 240, 38, 104, 32, null);
        dashboardGoStats = new PrimaryButton("Stats", 350, 38, 86, 32, null);
        dashboardGoDominantes.setBackground(new Color(45, 54, 76));
        dashboardGoSessions.setBackground(new Color(45, 54, 76));
        dashboardGoCampagne.setBackground(new Color(45, 54, 76));
        dashboardGoStats.setBackground(new Color(45, 54, 76));
        dashboardShortcutCard.addChild(shortcutTitle);
        dashboardShortcutCard.addChild(dashboardGoDominantes);
        dashboardShortcutCard.addChild(dashboardGoSessions);
        dashboardShortcutCard.addChild(dashboardGoCampagne);
        dashboardShortcutCard.addChild(dashboardGoStats);

        dashboardNoteCard = new SurfaceCard(0, 0, 100, 74, new Color(23, 30, 45), new Color(52, 63, 92), 12);
        Label noteText = new Label("Le tableau de bord charge les données en cache et reste réactif tant qu'aucune actualisation n'est demandée.", 16, 26, 640, 28);
        noteText.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        noteText.setColor(new Color(145, 158, 184));
        dashboardNoteCard.addChild(noteText);

        dashboardAlertContainer = new AlertContainer();
        dashboardAlertContainer.setDarkMode(true);
    }

    private void buildLayout() {
        dashboardHeroCard.addChild(dashboardSubtitleLabel);
        dashboardHeroCard.addChild(dashboardStatusLabel);
        // Note: title is added in initializeComponents but we need to add it to dashboardHeroCard
        dashboardHeroCard.addChild(0, new Label("Vue d'ensemble", 18, 18, 400, 24) {{
            setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 22));
            setColor(new Color(237, 242, 252));
        }});
        // Actually, let's rebuild the hero card properly - we'll do it in a simpler way
        // We'll reconstruct the hero card in buildLayout for clarity
    }

    // Let's rebuild the entire UI in buildLayout to avoid confusion
    public void rebuild() {
        removeAllChildren();
        
        // Recreate hero card
        dashboardHeroCard = new SurfaceCard(0, 0, 100, 150, new Color(18, 24, 35), new Color(52, 63, 92), 14);
        Label title = new Label("Vue d'ensemble", 18, 18, 400, 24);
        title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 22));
        title.setColor(new Color(237, 242, 252));
        dashboardSubtitleLabel = new Label("Chargement de la campagne active...", 18, 48, 560, 22);
        dashboardSubtitleLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 13));
        dashboardSubtitleLabel.setColor(new Color(160, 173, 200));
        dashboardStatusLabel = new Label("Tableau central pour piloter les dominantes, sessions et statistiques.", 18, 76, 620, 20);
        dashboardStatusLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        dashboardStatusLabel.setColor(new Color(125, 140, 168));
        dashboardHeroCard.addChild(title);
        dashboardHeroCard.addChild(dashboardSubtitleLabel);
        dashboardHeroCard.addChild(dashboardStatusLabel);

        // Recreate KPI card
        dashboardKpiCard = new SurfaceCard(0, 0, 100, 120, new Color(22, 28, 39), new Color(52, 63, 92), 14);
        dashboardSessionsKpi = new KpiCard("Sessions", "0", "Total", new Color(59, 130, 246));
        dashboardDominantesKpi = new KpiCard("Dominantes", "0", "Actives", new Color(168, 85, 247));
        dashboardFillKpi = new KpiCard("Remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        dashboardStudentsKpi = new KpiCard("Inscrits", "0", "Total", new Color(34, 197, 94));
        dashboardKpiCard.addChild(dashboardSessionsKpi);
        dashboardKpiCard.addChild(dashboardDominantesKpi);
        dashboardKpiCard.addChild(dashboardFillKpi);
        dashboardKpiCard.addChild(dashboardStudentsKpi);

        // Recreate shortcut card
        dashboardShortcutCard = new SurfaceCard(0, 0, 100, 96, new Color(23, 30, 45), new Color(52, 63, 92), 12);
        Label shortcutTitle = new Label("Raccourcis", 16, 12, 140, 18);
        shortcutTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        shortcutTitle.setColor(new Color(210, 219, 237));
        dashboardGoDominantes = new PrimaryButton("Dominantes", 16, 38, 112, 32, null);
        dashboardGoSessions = new PrimaryButton("Sessions", 134, 38, 100, 32, null);
        dashboardGoCampagne = new PrimaryButton("Campagne", 240, 38, 104, 32, null);
        dashboardGoStats = new PrimaryButton("Stats", 350, 38, 86, 32, null);
        dashboardGoDominantes.setBackground(new Color(45, 54, 76));
        dashboardGoSessions.setBackground(new Color(45, 54, 76));
        dashboardGoCampagne.setBackground(new Color(45, 54, 76));
        dashboardGoStats.setBackground(new Color(45, 54, 76));
        dashboardShortcutCard.addChild(shortcutTitle);
        dashboardShortcutCard.addChild(dashboardGoDominantes);
        dashboardShortcutCard.addChild(dashboardGoSessions);
        dashboardShortcutCard.addChild(dashboardGoCampagne);
        dashboardShortcutCard.addChild(dashboardGoStats);

        // Recreate note card
        dashboardNoteCard = new SurfaceCard(0, 0, 100, 74, new Color(23, 30, 45), new Color(52, 63, 92), 12);
        Label noteText = new Label("Le tableau de bord charge les données en cache et reste réactif tant qu'aucune actualisation n'est demandée.", 16, 26, 640, 28);
        noteText.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        noteText.setColor(new Color(145, 158, 184));
        dashboardNoteCard.addChild(noteText);

        // Recreate alert container
        dashboardAlertContainer = new AlertContainer();
        dashboardAlertContainer.setDarkMode(true);

        // Add all to root
        addChild(dashboardHeroCard);
        addChild(dashboardKpiCard);
        addChild(dashboardShortcutCard);
        addChild(dashboardAlertContainer);
        addChild(dashboardNoteCard);
    }

    /**
     * Met à jour le contenu de la racine du tableau de bord en fonction de la campagne active.
     * @param activeCampaign La campagne actuellement active (peut être null)
     */
    public void refreshDashboardRoot(Campaign activeCampaign) {
        if (dashboardSubtitleLabel != null && dashboardStatusLabel != null) {
            if (activeCampaign == null) {
                dashboardSubtitleLabel.setText("Aucune campagne ouverte actuellement");
                dashboardStatusLabel.setText("Passez par Campagne pour creer ou mettre a jour les parametres.");
                if (dashboardSessionsKpi != null) {
                    dashboardSessionsKpi.setValue("0");
                    dashboardDominantesKpi.setValue("0"); // Will be updated by caller if needed
                    dashboardFillKpi.setValue("0%");
                    dashboardStudentsKpi.setValue("0");
                }
            } else {
                dashboardSubtitleLabel.setText("Campagne: " + safe(activeCampaign.getName()) + " | Promo " + safe(activeCampaign.getPromo()));
                dashboardStatusLabel.setText("Periode: " + safe(activeCampaign.getRegistrationDay()) + " | Statut: " + safe(activeCampaign.getStatus()));
                StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(activeCampaign.getId(), activeCampaign.getPromo());
                if (dashboardSessionsKpi != null) {
                    dashboardSessionsKpi.setValue(String.valueOf(stats.totalSessions));
                    dashboardDominantesKpi.setValue(String.valueOf(dashboardDominantesKpi.getValue())); // Keep current dominante count
                    dashboardFillKpi.setValue(String.format("%.1f%%", stats.averageFillRate));
                    dashboardStudentsKpi.setValue(String.valueOf(stats.registeredStudents));
                }
            }

            if (dashboardAlertContainer != null) {
                List<AlertContainer.AlertItem> alerts = generateDashboardAlerts(activeCampaign);
                dashboardAlertContainer.setAlerts(alerts);
            }
        }
    }

    private List<AlertContainer.AlertItem> generateDashboardAlerts(Campaign activeCampaign) {
        List<AlertContainer.AlertItem> alerts = new ArrayList<>();
        if (activeCampaign == null) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Aucune campagne active", "Creez une campagne pour commencer."));
            return alerts;
        }

        String status = activeCampaign.getStatus();
        if ("PREPARATION".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.INFO, "Phase PREPARATION", "Les inscriptions ne sont pas encore ouvertes."));
        } else if ("OPEN".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.INFO, "Phase OPEN", "Les inscriptions sont ouvertes aux etudiants."));
        } else if ("CLOSED".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Phase CLOSED", "Les inscriptions sont fermees."));
        } else if ("PROCESSING".equals(status)) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Phase PROCESSING", "Traitement des allocations en cours."));
        }

        StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(activeCampaign.getId(), activeCampaign.getPromo());
        if (stats.unregisteredStudents > 0) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, stats.unregisteredStudents + " etudiants non inscrits", "Consultez la page Statistiques."));
        }
        if (stats.averageFillRate >= 95) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.ERROR, "Sessions quasi completes", "Taux de remplissage: " + String.format("%.0f%%", stats.averageFillRate)));
        }
        if (stats.totalSessions == 0) {
            alerts.add(new AlertContainer.AlertItem(AlertContainer.AlertType.WARNING, "Aucune session definie", "Ajoutez des sessions dans la section Sessions."));
        }
        return alerts;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    /**
     * Définit les actions pour les boutons de raccourci.
     * @param onDominantes Action pour le bouton Dominantes
     * @param onSessions Action pour le bouton Sessions
     * @param onCampagne Action pour le bouton Campagne
     * @param onStats Action pour le bouton Stats
     */
    public void setShortcutActions(Runnable onDominantes, Runnable onSessions, Runnable onCampagne, Runnable onStats) {
        if (dashboardGoDominantes != null) dashboardGoDominantes.setOnClick(onDominantes);
        if (dashboardGoSessions != null) dashboardGoSessions.setOnClick(onSessions);
        if (dashboardGoCampagne != null) dashboardGoCampagne.setOnClick(onCampagne);
        if (dashboardGoStats != null) dashboardGoStats.setOnClick(onStats);
    }

    /**
     * Définit le mode sombre pour tous les composants.
     * @param dark true pour activer le mode sombre, false pour le mode clair
     */
    public void setDarkMode(boolean dark) {
        if (dashboardHeroCard != null) {
            dashboardHeroCard.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
            dashboardHeroCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardKpiCard != null) {
            dashboardKpiCard.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
            dashboardKpiCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardShortcutCard != null) {
            dashboardShortcutCard.setBackground(dark ? new Color(23, 30, 45) : Color.WHITE);
            dashboardShortcutCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardNoteCard != null) {
            dashboardNoteCard.setBackground(dark ? new Color(23, 30, 45) : Color.WHITE);
            dashboardNoteCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardAlertContainer != null) {
            dashboardAlertContainer.setDarkMode(dark);
        }
        if (dashboardSessionsKpi != null) dashboardSessionsKpi.setDarkMode(dark);
        if (dashboardDominantesKpi != null) dashboardDominantesKpi.setDarkMode(dark);
        if (dashboardFillKpi != null) dashboardFillKpi.setDarkMode(dark);
        if (dashboardStudentsKpi != null) dashboardStudentsKpi.setDarkMode(dark);
        if (dashboardSubtitleLabel != null) {
            dashboardSubtitleLabel.setColor(dark ? new Color(160, 173, 200) : new Color(26, 34, 49));
        }
        if (dashboardStatusLabel != null) {
            dashboardStatusLabel.setColor(dark ? new Color(125, 140, 168) : new Color(76, 87, 104));
        }
        if (dashboardGoDominantes != null) {
            dashboardGoDominantes.setBackground(dark ? new Color(45, 54, 76) : new Color(236, 238, 242));
        }
        if (dashboardGoSessions != null) {
            dashboardGoSessions.setBackground(dark ? new Color(45, 54, 76) : new Color(236, 238, 242));
        }
        if (dashboardGoCampagne != null) {
            dashboardGoCampagne.setBackground(dark ? new Color(45, 54, 76) : new Color(236, 238, 242));
        }
        if (dashboardGoStats != null) {
            dashboardGoStats.setBackground(dark ? new Color(45, 54, 76) : new Color(236, 238, 242));
        }
    }

    /**
     * Met à jour les dimensions et la position des composants internes.
     * Cette méthode doit être appelée lorsque la taille du conteneur parent change.
     * @param width  La largeur disponible
     * @param height La hauteur disponible
     */
    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
        if (dashboardHeroCard != null) {
            dashboardHeroCard.setBounds(0, 0, width, 150);
        }
        if (dashboardKpiCard != null) {
            dashboardKpiCard.setBounds(0, 162, width, 120);
            if (dashboardSessionsKpi != null) {
                int kpiWidth = (width - 36) / 4;
                dashboardSessionsKpi.setBounds(0, 0, kpiWidth, 120);
                dashboardDominantesKpi.setBounds(kpiWidth + 12, 0, kpiWidth, 120);
                dashboardFillKpi.setBounds((kpiWidth + 12) * 2, 0, kpiWidth, 120);
                dashboardStudentsKpi.setBounds((kpiWidth + 12) * 3, 0, kpiWidth, 120);
            }
        }
        if (dashboardShortcutCard != null) {
            dashboardShortcutCard.setBounds(0, 294, width, 96);
            if (dashboardGoDominantes != null) {
                dashboardGoDominantes.setBounds(16, 38, 112, 32);
                dashboardGoSessions.setBounds(134, 38, 100, 32);
                dashboardGoCampagne.setBounds(240, 38, 104, 32);
                dashboardGoStats.setBounds(350, 38, 86, 32);
            }
        }
        if (dashboardNoteCard != null) {
            dashboardNoteCard.setBounds(0, 402, width, 74);
        }
        if (dashboardAlertContainer != null) {
            // Position the alert container below the shortcut card and above the note card
            dashboardAlertContainer.setBounds(0, 390, width, 12); // Height will be adjusted by setAlerts
        }
    }
}