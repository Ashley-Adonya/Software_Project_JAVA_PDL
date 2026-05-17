package gui.screen;
import java.awt.Color;
import java.util.ArrayList;
import gui.components.PageHeader;
import gui.components.PrimaryButton;
import gui.components.SidebarMenu;
import gui.screen.components.CampaignFormComponent;
import gui.screen.components.DominanteListComponent;
import gui.screen.components.SessionListComponent;
import gui.screen.components.StatsPanelComponent;
import gui.components.ConfirmDeleteModal;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.User;
import service.CampaignService;
import service.DominanteService;
import service.RegistrationService;
import service.SessionService;
import service.StatisticsService;

/**
 * Main orchestrator for the administrator dashboard interface.
 * <p>
 * This class is the central hub of the admin UI. It:
 * <ul>
 *   <li>Initialises all services, resolvers, and section managers</li>
 *   <li>Creates the sidebar navigation, page header, and refresh button</li>
 *   <li>Hosts all section components (dashboard root, dominantes list,
 *       sessions list, campaign form, statistics panel)</li>
 *   <li>Manages modal factories for sessions, dominantes, student registration,
 *       and session management</li>
 *   <li>Handles responsive layout, dark/light theme toggling, and data refresh</li>
 * </ul>
 * </p>
 */
public class AdminDashboardView {
    private final BaseWindow window;
    private final User user;
    private final CampaignResolver campaignResolver;
    private final AdminSectionManager sectionManager;
    private Campaign activeCampaign;
    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final StatisticsService statisticsService;
    private final RegistrationService registrationService;
    private final dao.RegistrationDAO registrationDAO;
    private final AdminDashboardRoot dashboardRoot;
    private final DominanteListComponent dominanteListComponent;
    private final SessionListComponent sessionListComponent;
    private final CampaignFormComponent campaignFormComponent;
    private final StatsPanelComponent statsPanelComponent;
    private final AdminSessionModal sessionModals;
    private final AdminManageSessionModal manageSessionModal;
    private final AdminDominanteModal dominanteModals;
    private final AdminStudentRegistrationModal registrationModal;
    private final Runnable onLogout;
    final SidebarMenu sidebar;
    final PageHeader header;
    final PrimaryButton refreshButton;
    final BaseComp sectionHost;
    private boolean sectionsMounted;
    private boolean darkMode = true;
    private int mainW = 320;
    private Color PAGE_BG = new Color(14, 18, 26);

    /**
     * Constructs the full admin dashboard view.
     * <p>
     * Initialises all services ({@link CampaignService}, {@link SessionService},
     * {@link DominanteService}, {@link StatisticsService}, {@link RegistrationService}),
     * the {@link CampaignResolver}, and the {@link AdminSectionManager}. Builds the
     * sidebar with navigation items, creates all section components (dashboard root,
     * session list, campaign form, dominante list, stats panel), and wires up the
     * modal managers and their callbacks (edit, manage, create).
     * </p>
     *
     * @param window   the application window that this dashboard is rendered into
     * @param user     the currently logged-in administrator user
     * @param onLogout callback to invoke when the user clicks the logout action
     */
    public AdminDashboardView(BaseWindow window, User user, Runnable onLogout) {
        this.window = window;
        this.user = user;
        this.onLogout = onLogout;
        campaignService = new CampaignService(user); sessionService = new SessionService();
        dominanteService = new DominanteService(); statisticsService = new StatisticsService();
        registrationService = new RegistrationService(); registrationDAO = new dao.RegistrationDAO();
        campaignResolver = new CampaignResolver(campaignService, user != null ? user.getPromo() : null);
        sectionManager = new AdminSectionManager(this);

        ArrayList<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("dashboard", "Tableau de bord"));
        items.add(new SidebarMenu.Item("dominantes", "Dominantes"));
        items.add(new SidebarMenu.Item("sessions", "Sessions"));
        items.add(new SidebarMenu.Item("campagne", "Campagne"));
        items.add(new SidebarMenu.Item("stats", "Statistiques"));
        sidebar = new SidebarMenu("Administration", displayName(), items, "dashboard",
            sectionManager::setSection, onLogout, this::toggleTheme);
        header = new PageHeader("Tableau de bord", "Vue d'ensemble");
        refreshButton = new PrimaryButton("Actualiser", 0, 0, 110, 28, this::refreshCurrent);
        refreshButton.setBackground(new Color(44, 54, 76));

        sectionHost = new BaseComp(null);
        dashboardRoot = new AdminDashboardRoot(statisticsService);
        dashboardRoot.setShortcutActions(
            () -> sectionManager.setSection("dominantes"), () -> sectionManager.setSection("sessions"),
            () -> sectionManager.setSection("campagne"), () -> sectionManager.setSection("stats"));
        sessionListComponent = new SessionListComponent(window, sessionService, dominanteService, this);
        campaignFormComponent = new CampaignFormComponent(window, user);
        dominanteListComponent = new DominanteListComponent(window, dominanteService, this);
        statsPanelComponent = new StatsPanelComponent(window, statisticsService);

        sessionModals = new AdminSessionModal(this); manageSessionModal = new AdminManageSessionModal(this);
        dominanteModals = new AdminDominanteModal(this); registrationModal = new AdminStudentRegistrationModal(this);
        sessionListComponent.setOnEditSession(s -> sessionModals.openEditSessionModal(s));
        sessionListComponent.setOnManageSession(s -> manageSessionModal.openManageSessionModal(s, 0));
        sessionListComponent.setOnCreateSession(() -> sessionModals.openCreateSessionModal());
        campaignFormComponent.onSave(this::saveCampaign);
        dominanteListComponent.onCreate(() -> dominanteModals.openCreateDominanteModal());
        dominanteListComponent.onEdit(d -> dominanteModals.openEditDominanteModal(d));
    }

    /**
     * Mounts the dashboard onto the application window.
     * <p>
     * Configures the style manager with the current background colour, clears
     * existing children, and adds the sidebar, header, refresh button, and
     * section host container. Calls {@link #mountSections()} to populate the
     * section host, then triggers an initial data refresh and applies the
     * current theme and responsive layout. Finally, applies section visibility
     * and refreshes the active section content.
     * </p>
     */
    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);
        content.addChild(sidebar); content.addChild(header); content.addChild(refreshButton); content.addChild(sectionHost);
        mountSections(); refreshAll(); applyTheme(); onResize();
        sectionManager.applyVisibility(); sectionManager.refreshActiveSection();
    }
    private void toggleTheme() { darkMode = !darkMode; applyTheme(); sectionManager.refreshActiveSection(); }
    private void applyTheme() {
        PAGE_BG = darkMode ? new Color(14, 18, 26) : new Color(243, 246, 252);
        sidebar.setDarkMode(darkMode); dashboardRoot.setDarkMode(darkMode); header.setDarkMode(darkMode);
        dominanteListComponent.setDarkMode(darkMode); sessionListComponent.setDarkMode(darkMode); statsPanelComponent.setDarkMode(darkMode);
        BaseComp content = window.getContent();
        if (content != null) content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        window.requestRenderIfNeeded();
    }

    /**
     * Recalculates the layout of all dashboard components when the window is resized.
     * <p>
     * Computes the sidebar width (between 210 and 248 px, or a quarter of the window
     * width), positions the header and refresh button to the right of the sidebar,
     * and resizes the section host to fill the remaining space. Delegates resize
     * to each section component (dashboard root, dominante list, session list,
     * campaign form, stats panel) with the computed main width.
     * </p>
     */
    public void onResize() {
        BaseComp content = window.getContent();
        int w = content.getWidth(), h = content.getHeight();
        int sideW = Math.max(210, Math.min(248, w / 4));
        sidebar.setBounds(0, 0, sideW, h);
        mainW = Math.max(320, w - sideW - 32);
        header.setBounds(sideW + 16, 18, mainW, 52);
        refreshButton.setBounds(sideW + 16 + mainW - 110, 24, 110, 28);
        sectionHost.setBounds(sideW + 16, 82, mainW, h - 98);
        dashboardRoot.onResize(mainW, sectionHost.getHeight());
        dominanteListComponent.onResize(mainW, sectionHost.getHeight());
        sessionListComponent.onResize(mainW, sectionHost.getHeight());
        campaignFormComponent.onResize(mainW); statsPanelComponent.onResize(mainW, sectionHost.getHeight());
        window.requestRenderIfNeeded();
    }

    void applySectionChange() { onResize(); sectionManager.applyVisibility(); sectionManager.refreshActiveSection(); }
    void setDashboardVisible(boolean v) { dashboardRoot.setVisible(v); }
    void setDominantesVisible(boolean v) { dominanteListComponent.getRoot().setVisible(v); }
    void setSessionsVisible(boolean v) { sessionListComponent.getRoot().setVisible(v); }
    void setCampagneVisible(boolean v) { campaignFormComponent.getRoot().setVisible(v); }
    void setStatsVisible(boolean v) { statsPanelComponent.getRoot().setVisible(v); }
    PageHeader getHeader() { return header; }
    void refreshDominantes() { dominanteListComponent.refresh(); }
    void refreshSessions() { sessionListComponent.setActiveCampaignId(activeCampaign == null ? -1 : activeCampaign.getId()); sessionListComponent.refresh(); }
    void refreshCampagne() { campaignFormComponent.refreshFrom(activeCampaign); }
    void refreshStats() { statsPanelComponent.refresh(activeCampaign == null ? -1 : activeCampaign.getId(), user != null ? user.getPromo() : null); }

    /**
     * Displays a confirmation modal dialog for destructive actions.
     * <p>
     * The modal shows the given warning message and provides confirm/cancel
     * buttons. The provided callback is executed only when the user confirms
     * the action.
     * </p>
     *
     * @param message   the confirmation message to display
     * @param onConfirm the action to execute when the user confirms deletion
     */
    public void showConfirmDeleteModal(String message, Runnable onConfirm) {
        ConfirmDeleteModal modal = new ConfirmDeleteModal(window);
        modal.setMessage(message);
        modal.setOnConfirm(onConfirm);
        window.openModal(modal);
    }
    void refreshDashboard() { dashboardRoot.refreshDashboardRoot(activeCampaign); }
    void requestRender() { window.requestRenderIfNeeded(); }
    private void refreshCurrent() { refreshAll(); sectionManager.refreshActiveSection(); }
    void refreshAll() {
    activeCampaign = campaignResolver.resolveActiveCampaign();
    refreshDashboard();
    refreshCampagne();
    refreshStats();
}
    private void saveCampaign(Campaign c) {
        if (c == null) return;
        int id = campaignService.createCampaign(c);
        campaignFormComponent.setFeedback(id > 0 ? "Campagne enregistree (id=" + id + ")" : "Echec creation.");
        if (id > 0) { refreshAll(); refreshCurrent(); }
    }
    private void mountSections() {
        if (sectionsMounted) return;
        sectionHost.addChild(dashboardRoot); sectionHost.addChild(dominanteListComponent.getRoot());
        sectionHost.addChild(sessionListComponent.getRoot()); sectionHost.addChild(campaignFormComponent.getRoot());
        sectionHost.addChild(statsPanelComponent.getRoot()); sectionsMounted = true;
    }
    /**
     * Returns the currently resolved active campaign.
     *
     * @return the active {@link Campaign} resolved by {@link CampaignResolver},
     *         or {@code null} if no campaign is available
     */
    public Campaign getActiveCampaign() { return activeCampaign; }
    /**
     * Returns the currently logged-in administrator user.
     *
     * @return the {@link User} instance representing the logged-in administrator
     */
    public User getUser() { return user; }
    /**
     * Returns the application window into which this dashboard is rendered.
     *
     * @return the {@link BaseWindow} instance used for adding components and opening modals
     */
    public BaseWindow getWindow() { return window; }
    /**
     * Returns the campaign service instance.
     *
     * @return the {@link CampaignService} used for campaign CRUD operations
     */
    public CampaignService getCampaignService() { return campaignService; }
    /**
     * Returns the session service instance.
     *
     * @return the {@link SessionService} used for session slot CRUD operations
     */
    public SessionService getSessionService() { return sessionService; }
    /**
     * Returns the dominante service instance.
     *
     * @return the {@link DominanteService} used for study domain CRUD operations
     */
    public DominanteService getDominanteService() { return dominanteService; }
    /**
     * Returns the registration service instance.
     *
     * @return the {@link RegistrationService} used for student registration operations
     */
    public RegistrationService getRegistrationService() { return registrationService; }
    /**
     * Returns the statistics service instance.
     *
     * @return the {@link StatisticsService} used for aggregating dashboard statistics and KPIs
     */
    public StatisticsService getStatisticsService() { return statisticsService; }
    /**
     * Returns the registration DAO instance.
     *
     * @return the {@link dao.RegistrationDAO} used for direct database access to registrations
     */
    public dao.RegistrationDAO getRegistrationDAO() { return registrationDAO; }
    private String displayName() { return user == null ? "" : (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getLogin() == null ? "" : user.getLogin()); }
    private void clearChildren(BaseComp p) { for (BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    /**
     * Delegates to the {@link AdminSectionManager} to refresh the currently
     * active section's data and request a re-render.
     */
    void refreshActiveSection() { sectionManager.refreshActiveSection(); }
}