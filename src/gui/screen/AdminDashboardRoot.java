package gui.screen;

import java.awt.Color;

import components.Label;
import gui.components.AlertContainer;
import gui.components.KpiCard;
import gui.components.PrimaryButton;
import gui.components.SurfaceCard;
import main.BaseComp;
import model.Campaign;
import service.StatisticsService;

/**
 * Composant racine de la section tableau de bord.
 * Contient les cartes d'information, les KPI, les raccourcis,
 * le conteneur d'alertes et la note explicative.
 * Gère son propre layout et ses alertes de manière autonome.
 */
public class AdminDashboardRoot extends BaseComp {

    private SurfaceCard heroCard, kpiCard, shortcutCard, noteCard;
    private AlertContainer alertContainer;
    private Label subtitleLabel, statusLabel;
    private KpiCard sessionsKpi, dominantesKpi, fillKpi, studentsKpi;
    private PrimaryButton goDominantes, goSessions, goCampagne, goStats;
    private final StatisticsService statisticsService;

    public AdminDashboardRoot(StatisticsService statisticsService) {
        super(null);
        this.statisticsService = statisticsService;
        build();
    }

    private void build() {
        heroCard = card(new Color(18, 24, 35));
        Label title = label("Vue d'ensemble", 18, 18, 400, 24, 22, true, new Color(237, 242, 252));
        subtitleLabel = label("Chargement...", 18, 48, 560, 22, 13, false, new Color(160, 173, 200));
        statusLabel = label("Pilotez les dominantes, sessions et statistiques.", 18, 76, 620, 20, 12, false, new Color(125, 140, 168));
        heroCard.addChild(title); heroCard.addChild(subtitleLabel); heroCard.addChild(statusLabel);

        kpiCard = card(new Color(22, 28, 39));
        sessionsKpi = new KpiCard("Sessions", "0", "Total", new Color(59, 130, 246));
        dominantesKpi = new KpiCard("Dominantes", "0", "Actives", new Color(168, 85, 247));
        fillKpi = new KpiCard("Remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        studentsKpi = new KpiCard("Inscrits", "0", "Total", new Color(34, 197, 94));
        kpiCard.addChild(sessionsKpi); kpiCard.addChild(dominantesKpi);
        kpiCard.addChild(fillKpi); kpiCard.addChild(studentsKpi);

        shortcutCard = card(new Color(23, 30, 45));
        Label shortcutTitle = label("Raccourcis", 16, 12, 140, 18, 12, true, new Color(210, 219, 237));
        Color btnBg = new Color(45, 54, 76);
        goDominantes = shortcutBtn("Dominantes", 16, 38, 112, 32, btnBg);
        goSessions = shortcutBtn("Sessions", 134, 38, 100, 32, btnBg);
        goCampagne = shortcutBtn("Campagne", 240, 38, 104, 32, btnBg);
        goStats = shortcutBtn("Stats", 350, 38, 86, 32, btnBg);
        shortcutCard.addChild(shortcutTitle);
        shortcutCard.addChild(goDominantes); shortcutCard.addChild(goSessions);
        shortcutCard.addChild(goCampagne); shortcutCard.addChild(goStats);

        noteCard = card(new Color(23, 30, 45));
        noteCard.addChild(label("Le tableau de bord charge les données en cache.", 16, 26, 640, 28, 12, false, new Color(145, 158, 184)));

        alertContainer = new AlertContainer();
        alertContainer.setDarkMode(true);

        addChild(heroCard); addChild(kpiCard); addChild(shortcutCard);
        addChild(alertContainer); addChild(noteCard);
    }

    private SurfaceCard card(Color bg) {
        return new SurfaceCard(0, 0, 100, 80, bg, new Color(52, 63, 92), 14);
    }

    private Label label(String text, int x, int y, int w, int h, int size, boolean bold, Color color) {
        Label l = new Label(text, x, y, w, h);
        l.setFont(new java.awt.Font("Dialog", bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN, size));
        l.setColor(color);
        return l;
    }

    private PrimaryButton shortcutBtn(String text, int x, int y, int w, int h, Color bg) {
        PrimaryButton b = new PrimaryButton(text, x, y, w, h, null);
        b.setBackground(bg);
        return b;
    }

    /**
     * Définit les actions pour les boutons de raccourci.
     */
    public void setShortcutActions(Runnable onDom, Runnable onSess, Runnable onCamp, Runnable onStats) {
        goDominantes.setOnClick(onDom);
        goSessions.setOnClick(onSess);
        goCampagne.setOnClick(onCamp);
        goStats.setOnClick(onStats);
    }

    /**
     * Met à jour le contenu en fonction de la campagne active.
     */
    public void refreshDashboardRoot(Campaign activeCampaign) {
        if (subtitleLabel == null || statusLabel == null) return;
        if (activeCampaign == null) {
            subtitleLabel.setText("Aucune campagne ouverte");
            statusLabel.setText("Creez une campagne pour commencer.");
            if (sessionsKpi != null) {
                sessionsKpi.setValue("0"); dominantesKpi.setValue("0");
                fillKpi.setValue("0%"); studentsKpi.setValue("0");
            }
        } else {
            subtitleLabel.setText("Campagne: " + safe(activeCampaign.getName()) + " | Promo " + safe(activeCampaign.getPromo()));
            statusLabel.setText("Periode: " + safe(activeCampaign.getRegistrationDay()) + " | Statut: " + safe(activeCampaign.getStatus()));
            StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(activeCampaign.getId(), activeCampaign.getPromo());
            if (sessionsKpi != null) {
            sessionsKpi.setValue(String.valueOf(stats.totalSessions));
            dominantesKpi.setValue(String.valueOf(stats.activeDominantes));
            fillKpi.setValue(String.format("%.1f%%", stats.averageFillRate));
            studentsKpi.setValue(String.valueOf(stats.registeredStudents));
            }
        }
        if (alertContainer != null) {
            alertContainer.setAlerts(AdminDashboardAlertManager.generateAlerts(activeCampaign, statisticsService));
        }
    }

    /**
     * Définit le mode sombre pour tous les composants du dashboard.
     */
    public void setDarkMode(boolean dark) {
        Color heroBg = dark ? new Color(18, 24, 35) : Color.WHITE;
        Color borderDark = dark ? new Color(52, 63, 92) : new Color(226, 230, 238);
        Color bgDark = dark ? new Color(22, 28, 39) : Color.WHITE;
        if (heroCard != null) { heroCard.setBackground(heroBg); heroCard.setBorderColor(borderDark); }
        if (kpiCard != null) { kpiCard.setBackground(bgDark); kpiCard.setBorderColor(borderDark); }
        if (shortcutCard != null) { shortcutCard.setBackground(dark ? new Color(23, 30, 45) : Color.WHITE); shortcutCard.setBorderColor(borderDark); }
        if (noteCard != null) { noteCard.setBackground(dark ? new Color(23, 30, 45) : Color.WHITE); noteCard.setBorderColor(borderDark); }
        if (alertContainer != null) alertContainer.setDarkMode(dark);
        if (sessionsKpi != null) { sessionsKpi.setDarkMode(dark); dominantesKpi.setDarkMode(dark); fillKpi.setDarkMode(dark); studentsKpi.setDarkMode(dark); }
        if (subtitleLabel != null) subtitleLabel.setColor(dark ? new Color(160, 173, 200) : new Color(26, 34, 49));
        if (statusLabel != null) statusLabel.setColor(dark ? new Color(125, 140, 168) : new Color(76, 87, 104));
        Color btnBg = dark ? new Color(45, 54, 76) : new Color(236, 238, 242);
        if (goDominantes != null) goDominantes.setBackground(btnBg);
        if (goSessions != null) goSessions.setBackground(btnBg);
        if (goCampagne != null) goCampagne.setBackground(btnBg);
        if (goStats != null) goStats.setBackground(btnBg);
    }

    /**
     * Met à jour les dimensions des composants internes.
     */
    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
        if (heroCard != null) heroCard.setBounds(0, 0, width, 150);
        if (kpiCard != null) {
            kpiCard.setBounds(0, 162, width, 120);
            int kw = (width - 36) / 4;
            if (sessionsKpi != null) sessionsKpi.setBounds(0, 0, kw, 120);
            if (dominantesKpi != null) dominantesKpi.setBounds(kw + 12, 0, kw, 120);
            if (fillKpi != null) fillKpi.setBounds((kw + 12) * 2, 0, kw, 120);
            if (studentsKpi != null) studentsKpi.setBounds((kw + 12) * 3, 0, kw, 120);
        }
        if (shortcutCard != null) {
            shortcutCard.setBounds(0, 294, width, 96);
            if (goDominantes != null) { goDominantes.setBounds(16, 38, 112, 32); goSessions.setBounds(134, 38, 100, 32); goCampagne.setBounds(240, 38, 104, 32); goStats.setBounds(350, 38, 86, 32); }
        }
if (alertContainer != null) alertContainer.setBounds(0, 402, width, 90);
if (noteCard != null) noteCard.setBounds(0, 504, width, 60);    }

    private String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}