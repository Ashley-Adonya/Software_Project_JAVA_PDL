package gui.screen;

import java.awt.Color;
import components.Label;
import components.ScrollView;
import gui.components.AlertContainer;
import gui.components.KpiCard;
import gui.components.PrimaryButton;
import gui.components.SurfaceCard;
import main.BaseComp;
import model.Campaign;
import service.StatisticsService;

public class AdminDashboardRoot extends BaseComp {

    private SurfaceCard heroCard, kpiCard, shortcutCard, alertCard;
    private Label subtitleLabel, statusLabel, noteLabel;
    private KpiCard sessionsKpi, dominantesKpi, fillKpi, studentsKpi;
    private PrimaryButton goDominantes, goSessions, goCampagne, goStats;
    private AlertContainer alertContainer;
    private ScrollView mainScroll;
    private BaseComp scrollContent;
    private final StatisticsService statisticsService;

    public AdminDashboardRoot(StatisticsService statisticsService) {
        super(null);
        this.statisticsService = statisticsService;
        build();
    }

    private void build() {
        mainScroll = new ScrollView(0, 0, 100, 100);
        scrollContent = mainScroll.getContent();

        heroCard = card(new Color(18, 24, 35));
        Label title = label("Tableau de bord de Campagne", 24, 20, 500, 28, 22, true, new Color(237, 242, 252));
        subtitleLabel = label("Chargement...", 24, 56, 560, 20, 14, false, new Color(160, 173, 200));
        statusLabel   = label("-", 24, 82, 620, 18, 13, false, new Color(125, 140, 168));
        heroCard.addChild(title); heroCard.addChild(subtitleLabel); heroCard.addChild(statusLabel);

        kpiCard = card(new Color(22, 28, 39));
        sessionsKpi   = new KpiCard("Sessions",    "0",    "Total programmées",   new Color(59, 130, 246));
        dominantesKpi = new KpiCard("Dominantes",  "0",    "Offres actives", new Color(168, 85, 247));
        fillKpi       = new KpiCard("Remplissage", "0,0%", "Moyenne totale", new Color(245, 158, 11));
        studentsKpi   = new KpiCard("Inscrits",    "0",    "Étudiants",   new Color(34, 197, 94));
        kpiCard.addChild(sessionsKpi); kpiCard.addChild(dominantesKpi); kpiCard.addChild(fillKpi); kpiCard.addChild(studentsKpi);

        shortcutCard = card(new Color(23, 30, 45));
        Label shortcutTitle = label("Actions Rapides", 20, 16, 200, 18, 14, true, new Color(160, 175, 202));
        Color btnBg = new Color(59, 130, 246);
        goDominantes = shortcutBtn("Dominantes", 0,  0, 0, 0, btnBg);
        goSessions   = shortcutBtn("Sessions",   0,  0, 0, 0, btnBg);
        goCampagne   = shortcutBtn("Campagne",   0,  0, 0, 0, btnBg);
        goStats      = shortcutBtn("Statistiques", 0,  0, 0, 0, btnBg);
        shortcutCard.addChild(shortcutTitle); shortcutCard.addChild(goDominantes);
        shortcutCard.addChild(goSessions); shortcutCard.addChild(goCampagne); shortcutCard.addChild(goStats);

        alertCard = card(new Color(20, 26, 38));
        Label alertTitle = label("Alertes Récentes", 16, 12, 200, 18, 14, true, new Color(160, 175, 202));
        alertContainer = new AlertContainer();
        alertContainer.setDarkMode(true);
        alertCard.addChild(alertTitle);
        alertCard.addChild(alertContainer);

        noteLabel = label("Rafraîchissez pour obtenir les données les plus récentes.", 8, 0, 600, 16, 12, false, new Color(100, 116, 139));

        scrollContent.addChild(heroCard); scrollContent.addChild(kpiCard);
        scrollContent.addChild(shortcutCard); scrollContent.addChild(alertCard);
        scrollContent.addChild(noteLabel);
        addChild(mainScroll);
    }

    public void setShortcutActions(Runnable onDom, Runnable onSess, Runnable onCamp, Runnable onStats) {
        goDominantes.setOnClick(onDom); goSessions.setOnClick(onSess);
        goCampagne.setOnClick(onCamp); goStats.setOnClick(onStats);
    }

    public void refreshDashboardRoot(Campaign activeCampaign) {
        if (subtitleLabel == null) return;
        if (activeCampaign == null) {
            subtitleLabel.setText("Aucune campagne ouverte");
            statusLabel.setText("Allez dans 'Campagne' pour créer et gérer une session.");
            sessionsKpi.setValue("0"); dominantesKpi.setValue("0");
            fillKpi.setValue("0,0%"); studentsKpi.setValue("0");
        } else {
            subtitleLabel.setText(activeCampaign.getName() + " — Promo " + activeCampaign.getPromo());
            statusLabel.setText("Actif: " + activeCampaign.getStartDate() + " au " + activeCampaign.getEndDate() + "  |  Statut : " + activeCampaign.getStatus());
            StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(activeCampaign.getId(), activeCampaign.getPromo());
            sessionsKpi.setValue(String.valueOf(stats.totalSessions)); dominantesKpi.setValue(String.valueOf(stats.activeDominantes));
            fillKpi.setValue(String.format("%.1f%%", stats.averageFillRate)); studentsKpi.setValue(String.valueOf(stats.registeredStudents));
        }
        if (alertContainer != null) {
            alertContainer.setAlerts(AdminDashboardAlertManager.generateAlerts(activeCampaign, statisticsService));
        }
    }

    public void setDarkMode(boolean dark) {
        Color border = dark ? new Color(52, 63, 92) : new Color(226, 230, 238);
        if (heroCard != null) { heroCard.setBackground(dark ? new Color(20, 26, 38) : Color.WHITE); heroCard.setBorderColor(border); }
        if (kpiCard != null) { kpiCard.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE); kpiCard.setBorderColor(border); }
        if (shortcutCard != null) { shortcutCard.setBackground(dark ? new Color(23, 30, 45) : Color.WHITE); shortcutCard.setBorderColor(border); }
        if (alertCard != null) { alertCard.setBackground(dark ? new Color(20, 26, 38) : Color.WHITE); alertCard.setBorderColor(border); }
        if (alertContainer != null) alertContainer.setDarkMode(dark);
        if (sessionsKpi != null) { sessionsKpi.setDarkMode(dark); dominantesKpi.setDarkMode(dark); fillKpi.setDarkMode(dark); studentsKpi.setDarkMode(dark); }
        Color txt = dark ? new Color(237, 242, 252) : new Color(15, 23, 42);
        if (subtitleLabel != null) subtitleLabel.setColor(dark ? new Color(160, 173, 200) : new Color(71, 85, 105));
        if (statusLabel != null) statusLabel.setColor(dark ? new Color(125, 140, 168) : new Color(100, 116, 139));
    }

    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
        mainScroll.setBounds(0, 0, width, height);
        scrollContent.setBounds(0, 0, width, height);

        int w = width - 16; 
        int y = 0;
        int gap = 16;

        heroCard.setBounds(0, y, w, 120); y += 120 + gap;

        kpiCard.setBounds(0, y, w, 110);
        int kw = (w - 36) / 4;
        sessionsKpi.setBounds(0, 0, kw, 110);
        dominantesKpi.setBounds(kw + 12, 0, kw, 110);
        fillKpi.setBounds((kw + 12) * 2, 0, kw, 110);
        studentsKpi.setBounds((kw + 12) * 3, 0, kw, 110);
        y += 110 + gap;

        int shortH = 100;
        shortcutCard.setBounds(0, y, w, shortH);
        int btnW = (w - (16*5)) / 4;
        goDominantes.setBounds(16, 45, btnW, 40);
        goSessions.setBounds(16*2 + btnW, 45, btnW, 40);
        goCampagne.setBounds(16*3 + btnW*2, 45, btnW, 40);
        goStats.setBounds(16*4 + btnW*3, 45, btnW, 40);
        y += shortH + gap;

        int alertH = 260; // Suffisamment haut pour pas couper
        alertCard.setBounds(0, y, w, alertH);
        alertContainer.onResize(w - 32, alertH - 44);
        alertContainer.setBounds(16, 38, w - 32, alertH - 44);
        y += alertH + gap;

        noteLabel.setBounds(8, y, w - 16, 30);
        mainScroll.setContentHeight(Math.max(y + 40, height));
        mainScroll.setContentWidth(width);
    }

    private SurfaceCard card(Color bg) { return new SurfaceCard(0, 0, 100, 80, bg, new Color(52, 63, 92), 12); }
    private Label label(String text, int x, int y, int w, int h, int size, boolean bold, Color color) {
        Label l = new Label(text, x, y, w, h);
        l.setFont(new java.awt.Font("Dialog", bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN, size));
        l.setColor(color); return l;
    }
    private PrimaryButton shortcutBtn(String text, int x, int y, int w, int h, Color bg) {
        PrimaryButton b = new PrimaryButton(text, x, y, w, h, null);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        return b;
    }
}