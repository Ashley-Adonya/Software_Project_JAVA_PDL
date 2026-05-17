package gui.screen.components;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import main.BaseComp;
import main.BaseWindow;
import model.User;
import service.StatisticsService;
import gui.components.SurfaceCard;
import gui.components.PrimaryButton;
import components.Label;
import components.ScrollView;

/**
 * Panneau principal des statistiques.
 * Structure verticale dans un scroll : Hero > Tabs > KPI > Filtres > Contenu
 * Contenu change selon l'onglet actif (Vue/Etudiants/Sessions/Détail)
 */
public class StatsPanelComponent {
    private enum ViewMode { OVERVIEW, STUDENT, SESSION }
    private ViewMode mode = ViewMode.OVERVIEW;
    private final StatisticsService statsService;
    private final SurfaceCard root;
    private final ScrollView scroll;
    private final BaseComp content;

    private final SurfaceCard heroCard;
    private final SurfaceCard tabBar;
    private final PrimaryButton tabOverview, tabStudents, tabSessions;

    private final StatsKpiSection kpiSection;
    private final StatsFilterSection filterSection;
    private final StatsSessionsSection sessionsSection;
    private final StatsStudentsSection studentsSection;
    private final StudentDetailPanel studentDetail;
    private final SessionDetailPanel sessionDetail;

    private Consumer<User> onSelectStudent = u -> {};
    private int campaignId = -1;
    private String promo = "";
    private boolean darkMode = true;

    public StatsPanelComponent(BaseWindow window, StatisticsService statsService) {
        this.statsService = statsService;
        root = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        scroll = new ScrollView(0, 0, 100, 100);
        content = scroll.getContent();

        heroCard = card(0, 0, 100, 120, new Color(18, 24, 35));
        heroCard.addChild(label("Statistiques", 18, 14, 280, 24, 20, true, new Color(239, 244, 252)));
        heroCard.addChild(label("Analyse des inscriptions", 18, 44, 380, 18, 13, false, new Color(161, 175, 202)));
        heroCard.addChild(label("Cliquez sur Actualiser pour mettre a jour", 18, 68, 380, 16, 11, false, new Color(129, 143, 170)));

        tabBar = card(0, 0, 100, 50, new Color(22, 28, 39));
        tabOverview = new PrimaryButton("Vue", 0, 0, 80, 38, () -> switchView(ViewMode.OVERVIEW));
        tabStudents = new PrimaryButton("Etudiants", 88, 0, 108, 38, () -> switchView(ViewMode.STUDENT));
        tabSessions = new PrimaryButton("Sessions", 204, 0, 96, 38, () -> switchView(ViewMode.SESSION));
        tabOverview.setBackground(new Color(59, 130, 246));
        tabStudents.setBackground(new Color(40, 50, 70));
        tabSessions.setBackground(new Color(40, 50, 70));
        tabBar.addChild(tabOverview); tabBar.addChild(tabStudents); tabBar.addChild(tabSessions);

        kpiSection = new StatsKpiSection();
        filterSection = new StatsFilterSection(statsService);
        sessionsSection = new StatsSessionsSection();
        studentsSection = new StatsStudentsSection();
        studentDetail = new StudentDetailPanel(statsService);
        sessionDetail = new SessionDetailPanel(statsService);

        studentsSection.onSelectStudent(u -> {
            if (campaignId > 0) { switchView(ViewMode.STUDENT); studentDetail.loadStudent(u, campaignId); }
        });
        sessionsSection.setOnSelectSession(s -> {
            if (campaignId > 0) { switchView(ViewMode.SESSION); sessionDetail.loadSession(s, campaignId); }
        });
        studentDetail.setOnBack(() -> switchView(ViewMode.STUDENT));
        sessionDetail.setOnBack(() -> switchView(ViewMode.SESSION));

        content.addChild(heroCard); content.addChild(tabBar);
        content.addChild(kpiSection.getRoot()); content.addChild(filterSection.getRoot());
        content.addChild(sessionsSection.getRoot()); content.addChild(studentsSection.getRoot());
        content.addChild(studentDetail.getRoot()); content.addChild(sessionDetail.getRoot());
        studentDetail.getRoot().setVisible(false); sessionDetail.getRoot().setVisible(false);

        root.addChild(scroll);
    }

    public BaseComp getRoot() { return root; }
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
        root.setBackground(dark ? new Color(14, 18, 26) : Color.WHITE);
        heroCard.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
        heroCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        tabBar.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
        Color active = new Color(59, 130, 246);
        Color inactive = dark ? new Color(40, 50, 70) : new Color(236, 238, 242);
        tabOverview.setBackground(mode == ViewMode.OVERVIEW ? active : inactive);
        tabStudents.setBackground(mode == ViewMode.STUDENT ? active : inactive);
        tabSessions.setBackground(mode == ViewMode.SESSION ? active : inactive);
        kpiSection.setDarkMode(dark); filterSection.setDarkMode(dark);
        sessionsSection.setDarkMode(dark); studentsSection.setDarkMode(dark);
        studentDetail.setDarkMode(dark); sessionDetail.setDarkMode(dark);
        root.invalidate();
    }

    private void switchView(ViewMode m) {
        mode = m;
        Color active = new Color(59, 130, 246);
        Color inactive = darkMode ? new Color(40, 50, 70) : new Color(236, 238, 242);
        tabOverview.setBackground(m == ViewMode.OVERVIEW ? active : inactive);
        tabStudents.setBackground(m == ViewMode.STUDENT ? active : inactive);
        tabSessions.setBackground(m == ViewMode.SESSION ? active : inactive);

        kpiSection.getRoot().setVisible(false);
        filterSection.getRoot().setVisible(false);
        sessionsSection.getRoot().setVisible(false);
        studentsSection.getRoot().setVisible(false);
        studentDetail.getRoot().setVisible(false);
        sessionDetail.getRoot().setVisible(false);

        if (m == ViewMode.OVERVIEW) {
            kpiSection.getRoot().setVisible(true);
            filterSection.getRoot().setVisible(true);
            sessionsSection.getRoot().setVisible(true);
        } else if (m == ViewMode.STUDENT) {
            studentsSection.getRoot().setVisible(true);
        } else if (m == ViewMode.SESSION) {
            sessionsSection.getRoot().setVisible(true);
        }
        root.invalidate();
    }

    public void refresh(int campaignId, String promo) {
        this.campaignId = campaignId;
        this.promo = promo;
        if (campaignId <= 0 || promo == null || promo.isBlank()) return;
        StatisticsService.StatsSummary s = statsService.getStatsForCampaign(campaignId, promo);
        kpiSection.update(s);
        sessionsSection.refresh(s);
        filterSection.refresh(campaignId, promo);
        List<User> unreg = statsService.getUnregisteredStudents(campaignId, promo);
        List<User> reg = statsService.getRegisteredStudents(campaignId, promo);
        studentsSection.update(s, reg, unreg);
        switchView(ViewMode.OVERVIEW);
    }

    public void onResize(int mainW, int mainH) {
        root.setBounds(0, 0, mainW, mainH);
        scroll.setBounds(0, 0, mainW, mainH);
        content.setBounds(0, 0, mainW, Math.max(mainH, 1000));

        heroCard.setBounds(0, 0, mainW, 120);
        tabBar.setBounds(0, 128, mainW, 50);
        int tabTotal = 80 + 108 + 96;
        int tabStart = (mainW - tabTotal) / 2;
        tabOverview.setBounds(tabStart, 6, 80, 38);
        tabStudents.setBounds(tabStart + 88, 6, 108, 38);
        tabSessions.setBounds(tabStart + 204, 6, 96, 38);

        kpiSection.onResize(mainW);
        kpiSection.getRoot().setBounds(0, 186, mainW, 116);

        filterSection.onResize(mainW, 160);
        filterSection.getRoot().setBounds(0, 310, mainW, 160);

        int contentY = 478;
        int contentH = Math.max(280, mainH - contentY - 16);

        sessionsSection.onResize(mainW, contentH);
        studentsSection.onResize(mainW, contentH);
        sessionsSection.getRoot().setBounds(0, contentY, mainW, contentH);
        studentsSection.getRoot().setBounds(0, contentY, mainW, contentH);
        studentDetail.onResize(mainW, contentH);
        studentDetail.getRoot().setBounds(0, contentY, mainW, contentH);
        sessionDetail.onResize(mainW, contentH);
        sessionDetail.getRoot().setBounds(0, contentY, mainW, contentH);

        scroll.setContentHeight(contentY + contentH + 20);
    }

    private SurfaceCard card(int x, int y, int w, int h, Color bg) {
        return new SurfaceCard(x, y, w, h, bg, new Color(52, 63, 92), 12);
    }

    private Label label(String text, int x, int y, int w, int h, int size, boolean bold, Color color) {
        Label l = new Label(text, x, y, w, h);
        l.setFont(new java.awt.Font("Dialog", bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN, size));
        l.setColor(color);
        return l;
    }
}