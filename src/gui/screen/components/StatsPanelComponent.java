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
 * Main statistics panel that orchestrates all sub-sections (KPI, filter,
 * sessions, students, and detail panels) within a tabbed interface.
 * Provides a global overview of campaign statistics as well as drill-down
 * views for individual students and sessions.
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Three-tab navigation: "Vue globale" (overview), "Étudiants" (students), "Détails Sessions" (session details)</li>
 *   <li>Overview tab shows KPI cards, a search/filter section, session distribution, and student lists</li>
 *   <li>Student and session detail panels with a back button to return to the list view</li>
 *   <li>Coordinates data refreshing across all sub-sections when the campaign or promo changes</li>
 *   <li>Supports dark/light mode theming propagated to all child components</li>
 *   <li>Responsive layout that recalculates all bounds on resize</li>
 * </ul>
 */
public class StatsPanelComponent {
    private enum ViewMode { OVERVIEW, STUDENT, SESSION }
    private ViewMode mode = ViewMode.OVERVIEW;
    private final StatisticsService statsService;
    private final SurfaceCard root;
    private final ScrollView scroll;
    private final BaseComp content;

    private final SurfaceCard heroCard;
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

        heroCard = new SurfaceCard(0, 0, 100, 70, new Color(22, 28, 39), new Color(52, 63, 92), 12);
        
        tabOverview = new PrimaryButton("Vue globale", 0, 0, 120, 36, () -> switchView(ViewMode.OVERVIEW));
        tabStudents = new PrimaryButton("Étudiants", 0, 0, 120, 36, () -> switchView(ViewMode.STUDENT));
        tabSessions = new PrimaryButton("Détails Sessions", 0, 0, 140, 36, () -> switchView(ViewMode.SESSION));
        heroCard.addChild(tabOverview); heroCard.addChild(tabStudents); heroCard.addChild(tabSessions);

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

        content.addChild(heroCard);
        content.addChild(kpiSection.getRoot()); content.addChild(filterSection.getRoot());
        content.addChild(sessionsSection.getRoot()); content.addChild(studentsSection.getRoot());
        content.addChild(studentDetail.getRoot()); content.addChild(sessionDetail.getRoot());
        studentDetail.getRoot().setVisible(false); sessionDetail.getRoot().setVisible(false);

        root.addChild(scroll);
    }

    /**
     * Returns the root UI component of the entire stats panel.
     *
     * @return the root {@link BaseComp} container
     */
    public BaseComp getRoot() { return root; }

    /**
     * Registers a callback invoked when a student is selected anywhere within the panel.
     *
     * @param cb a {@link Consumer} accepting the selected {@link User}
     */
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }

    /**
     * Switches the panel between dark and light colour themes.
     * Propagates the theme change to all sub-sections: KPI, filter, sessions,
     * students, and both detail panels. Also updates the tab button colours
     * to highlight the active tab.
     *
     * @param dark {@code true} for dark mode, {@code false} for light mode
     */
    public void setDarkMode(boolean dark) {
        darkMode = dark;
        root.setBackground(dark ? new Color(14, 18, 26) : Color.WHITE);
        heroCard.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
        heroCard.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        
        Color active = new Color(59, 130, 246);
        Color inactive = dark ? new Color(40, 50, 70) : new Color(226, 230, 240);
        Color textAc = Color.WHITE; Color textIn = dark ? new Color(219, 230, 253) : new Color(71, 85, 105);

        tabOverview.setBackground(mode == ViewMode.OVERVIEW ? active : inactive);
        tabStudents.setBackground(mode == ViewMode.STUDENT ? active : inactive);
        tabSessions.setBackground(mode == ViewMode.SESSION ? active : inactive);
        tabOverview.setForeground(mode == ViewMode.OVERVIEW ? textAc : textIn);
        tabStudents.setForeground(mode == ViewMode.STUDENT ? textAc : textIn);
        tabSessions.setForeground(mode == ViewMode.SESSION ? textAc : textIn);

        kpiSection.setDarkMode(dark); filterSection.setDarkMode(dark);
        sessionsSection.setDarkMode(dark); studentsSection.setDarkMode(dark);
        studentDetail.setDarkMode(dark); sessionDetail.setDarkMode(dark);
        root.invalidate();
    }

    private void switchView(ViewMode m) {
        mode = m;
        setDarkMode(darkMode); // Relance les couleurs de l'onglet actif

        kpiSection.getRoot().setVisible(m == ViewMode.OVERVIEW);
        filterSection.getRoot().setVisible(m == ViewMode.OVERVIEW);
        sessionsSection.getRoot().setVisible(m == ViewMode.OVERVIEW || m == ViewMode.SESSION);
        studentsSection.getRoot().setVisible(m == ViewMode.STUDENT);
        studentDetail.getRoot().setVisible(false);
        sessionDetail.getRoot().setVisible(false);

        onResize(root.getWidth(), root.getHeight());
        root.invalidate();
    }

    /**
     * Refreshes all statistics data for the given campaign and promo.
     * Fetches a fresh {@link StatisticsService.StatsSummary} from the service,
     * updates every sub-section (KPI, sessions, filter, registered/unregistered students),
     * and switches the view back to the overview tab.
     *
     * @param campaignId the numerical identifier of the campaign; ignored if {@code <= 0}
     * @param promo      the promo (class year) string; ignored if blank
     */
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

    /**
     * Adjusts the layout of all sub-sections when the parent container is resized.
     * Positions the hero card with tab buttons at the top, then lays out visible
     * sections vertically (KPI, filter, sessions, students, or detail panels)
     * depending on the current view mode. Dynamically calculates heights to
     * prevent ugly overflow.
     *
     * @param mainW the new width in pixels
     * @param mainH the new height in pixels
     */
    public void onResize(int mainW, int mainH) {
        root.setBounds(0, 0, mainW, mainH);
        scroll.setBounds(0, 0, mainW, mainH);
        content.setBounds(0, 0, mainW, Math.max(mainH, 1000));

        int gap = 16;
        int currentY = 0;

        heroCard.setBounds(0, currentY, mainW, 64);
        tabOverview.setBounds(16, 14, 120, 36);
        tabStudents.setBounds(146, 14, 120, 36);
        tabSessions.setBounds(276, 14, 140, 36);
        currentY += 64 + gap;

        if (mode == ViewMode.OVERVIEW) {
            kpiSection.onResize(mainW);
            kpiSection.getRoot().setBounds(0, currentY, mainW, 110);
            currentY += 110 + gap;

            filterSection.onResize(mainW, 140);
            filterSection.getRoot().setBounds(0, currentY, mainW, 140);
            currentY += 140 + gap;
        }

        int contentH = Math.max(400, mainH - currentY - 20); // Hauteur dynamique pour s'assurer que ça remplisse sans déborder mochement
        
        sessionsSection.onResize(mainW, contentH);
        studentsSection.onResize(mainW, contentH);
        studentDetail.onResize(mainW, contentH);
        sessionDetail.onResize(mainW, contentH);

        sessionsSection.getRoot().setBounds(0, currentY, mainW, contentH);
        studentsSection.getRoot().setBounds(0, currentY, mainW, contentH);
        studentDetail.getRoot().setBounds(0, currentY, mainW, contentH);
        sessionDetail.getRoot().setBounds(0, currentY, mainW, contentH);

        scroll.setContentHeight(currentY + contentH + 30);
    }
}