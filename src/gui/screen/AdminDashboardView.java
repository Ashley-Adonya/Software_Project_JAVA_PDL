package gui.screen;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import components.Label;
import gui.components.PageHeader;
import gui.components.PrimaryButton;
import gui.components.SidebarMenu;
import gui.screen.components.CampaignFormComponent;
import gui.screen.components.DominanteListComponent;
import gui.screen.components.SessionListComponent;
import gui.screen.components.StatsPanelComponent;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.User;
import service.CampaignService;
import service.CacheManager;
import service.DominanteService;
import service.RegistrationService;
import service.SessionService;
import service.StatisticsService;

/**
 * Vue centralisée du tableau de bord administrateur.
 * Orchestre l'UI avec navigation latérale et sections extraites.
 * Délègue les modales et le dashboard à des classes spécialisées.
 *
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 2.0
 */
public class AdminDashboardView {
    private enum Section { DASHBOARD, DOMINANTES, SESSIONS, CAMPAGNE, STATS }

    private Section activeSection = Section.DASHBOARD;
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaseWindow window;
    private final User user;
    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final StatisticsService statisticsService;
    private final RegistrationService registrationService;
    private final dao.RegistrationDAO registrationDAO;

    private final SessionListComponent sessionListComponent;
    private final CampaignFormComponent campaignFormComponent;
    private final DominanteListComponent dominanteListComponent;
    private final StatsPanelComponent statsPanelComponent;
    private final AdminDashboardRoot dashboardRoot;

    private final AdminSessionModal sessionModals;
    private final AdminManageSessionModal manageSessionModal;
    private final AdminDominanteModal dominanteModals;
    private final AdminStudentRegistrationModal registrationModal;

    final SidebarMenu sidebar;
    final PageHeader header;
    final PrimaryButton refreshButton;
    final BaseComp sectionHost;

    private boolean sectionsMounted;
    private boolean darkMode = true;
    private int mainW = 320;
    private Campaign activeCampaign;
    private Color PAGE_BG = new Color(14, 18, 26);

    public AdminDashboardView(BaseWindow window, User user) {
        this.window = window;
        this.user = user;
        campaignService = new CampaignService();
        sessionService = new SessionService();
        dominanteService = new DominanteService();
        statisticsService = new StatisticsService();
        registrationService = new RegistrationService();
        registrationDAO = new dao.RegistrationDAO();

        ArrayList<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("dashboard", "Tableau de bord"));
        items.add(new SidebarMenu.Item("dominantes", "Dominantes"));
        items.add(new SidebarMenu.Item("sessions", "Sessions"));
        items.add(new SidebarMenu.Item("campagne", "Campagne"));
        items.add(new SidebarMenu.Item("stats", "Statistiques"));
        sidebar = new SidebarMenu("Administration", resolveDisplayName(user), items, "dashboard",
            this::onSidebarSelect, () -> window.closeTopLayer(), this::toggleTheme);

        header = new PageHeader("Tableau de bord", "Vue d'ensemble de la campagne en cours");
        refreshButton = new PrimaryButton("Actualiser", 0, 0, 110, 28, this::refreshCurrentSection);
        refreshButton.setBackground(new Color(44, 54, 76));

        sectionHost = new BaseComp(null);
        dashboardRoot = new AdminDashboardRoot(statisticsService);
        dashboardRoot.setShortcutActions(
            () -> onSidebarSelect("dominantes"),
            () -> onSidebarSelect("sessions"),
            () -> onSidebarSelect("campagne"),
            () -> onSidebarSelect("stats")
        );

        sessionListComponent = new SessionListComponent(window, sessionService, dominanteService);
        campaignFormComponent = new CampaignFormComponent(window);
        dominanteListComponent = new DominanteListComponent(window, dominanteService);
        statsPanelComponent = new StatsPanelComponent(window, statisticsService);

        sessionModals = new AdminSessionModal(this);
        manageSessionModal = new AdminManageSessionModal(this);
        dominanteModals = new AdminDominanteModal(this);
        registrationModal = new AdminStudentRegistrationModal(this);

        sessionListComponent.setOnEditSession(s -> sessionModals.openEditSessionModal(s));
        sessionListComponent.setOnManageSession(s -> manageSessionModal.openManageSessionModal(s, 0));
        sessionListComponent.setOnCreateSession(() -> sessionModals.openCreateSessionModal());
        campaignFormComponent.onSave(c -> saveCampaignFromComponent(c));
        dominanteListComponent.onCreate(() -> dominanteModals.openCreateDominanteModal());
        dominanteListComponent.onEdit(d -> dominanteModals.openEditDominanteModal(d));
    }

    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);
        content.addChild(sidebar); content.addChild(header);
        content.addChild(refreshButton); content.addChild(sectionHost);
        mountSectionsOnce(); refreshAllData(); applyTheme();
        onResize(); applyActiveSectionVisibility(); refreshActiveSection();
    }

    private void toggleTheme() { darkMode = !darkMode; applyTheme(); refreshActiveSection(); }

    private void applyTheme() {
        PAGE_BG = darkMode ? new Color(14, 18, 26) : new Color(243, 246, 252);
        sidebar.setDarkMode(darkMode);
        dashboardRoot.setDarkMode(darkMode);
        header.setDarkMode(darkMode);
        dominanteListComponent.setDarkMode(darkMode);
        sessionListComponent.setDarkMode(darkMode);
        statsPanelComponent.setDarkMode(darkMode);
        BaseComp content = window.getContent();
        if (content != null) content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        window.requestRenderIfNeeded();
    }

    public void onResize() {
        BaseComp content = window.getContent();
        int w = content.getWidth(), h = content.getHeight();
        int sideW = Math.max(210, Math.min(248, w / 4));
        sidebar.setBounds(0, 0, sideW, h);
        int mainX = sideW + 16;
        mainW = Math.max(320, w - mainX - 16);
        header.setBounds(mainX, 18, mainW, 52);
        refreshButton.setBounds(mainX + mainW - 110, 24, 110, 28);
        sectionHost.setBounds(mainX, 82, mainW, h - 98);
        dashboardRoot.onResize(mainW, sectionHost.getHeight());
        dominanteListComponent.onResize(mainW, sectionHost.getHeight());
        sessionListComponent.onResize(mainW, sectionHost.getHeight());
        campaignFormComponent.onResize(mainW);
        statsPanelComponent.onResize(mainW, sectionHost.getHeight());
        window.requestRenderIfNeeded();
    }

    void refreshAllData() { activeCampaign = resolveActiveCampaign(); }

    Campaign resolveActiveCampaign() {
        Campaign open = selectBestCampaign(campaignService.getCampaignsByStatus("OPEN"));
        if (open != null) return open;
        Campaign prep = selectBestCampaign(campaignService.getCampaignsByStatus("PREPARATION"));
        return prep != null ? prep : selectBestCampaign(campaignService.getCampaignsByStatus("CLOSED"));
    }

    Campaign selectBestCampaign(List<Campaign> campaigns) {
        if (campaigns == null || campaigns.isEmpty()) return null;
        String promo = user == null ? null : user.getPromo();
        Campaign best = campaigns.stream()
            .filter(c -> promo != null && promo.equalsIgnoreCase(safe(c.getPromo())))
            .max(Comparator.comparing(this::campaignDateForSort).thenComparingInt(Campaign::getId)).orElse(null);
        return best != null ? best : campaigns.stream()
            .max(Comparator.comparing(this::campaignDateForSort).thenComparingInt(Campaign::getId)).orElse(campaigns.get(0));
    }

    LocalDate campaignDateForSort(Campaign c) {
        if (c == null || c.getRegistrationDay() == null || c.getRegistrationDay().isBlank()) return LocalDate.MIN;
        try { return LocalDate.parse(c.getRegistrationDay(), FR_DATE); } catch (Exception e) { return LocalDate.MIN; }
    }

    private void saveCampaignFromComponent(Campaign c) {
        if (c == null) return;
        int id = campaignService.createCampaign(c);
        campaignFormComponent.setFeedback(id > 0 ? "Campagne enregistree (id=" + id + ")" : "Echec creation.");
        if (id > 0) { refreshAllData(); refreshCurrentSection(); }
    }

    private void mountSectionsOnce() {
        if (sectionsMounted) return;
        sectionHost.addChild(dashboardRoot);
        sectionHost.addChild(dominanteListComponent.getRoot());
        sectionHost.addChild(sessionListComponent.getRoot());
        sectionHost.addChild(campaignFormComponent.getRoot());
        sectionHost.addChild(statsPanelComponent.getRoot());
        sectionsMounted = true;
    }

    private void applyActiveSectionVisibility() {
        updateHeaderSubtitle();
        dashboardRoot.setVisible(activeSection == Section.DASHBOARD);
        dominanteListComponent.getRoot().setVisible(activeSection == Section.DOMINANTES);
        sessionListComponent.getRoot().setVisible(activeSection == Section.SESSIONS);
        campaignFormComponent.getRoot().setVisible(activeSection == Section.CAMPAGNE);
        statsPanelComponent.getRoot().setVisible(activeSection == Section.STATS);
    }

    private void updateHeaderSubtitle() {
        switch (activeSection) {
            case DOMINANTES -> { header.setTitle("Dominantes"); header.setSubtitle("Gerez les domaines d'etudes disponibles"); }
            case SESSIONS -> { header.setTitle("Sessions"); header.setSubtitle("Gerez les creneaux de presentation"); }
            case CAMPAGNE -> { header.setTitle("Campagne"); header.setSubtitle("Configurez les parametres generaux"); }
            case STATS -> { header.setTitle("Statistiques"); header.setSubtitle("Analyse des inscriptions"); }
            default -> { header.setTitle("Tableau de bord"); header.setSubtitle("Vue d'ensemble de la campagne"); }
        }
    }

    void refreshActiveSection() {
        CacheManager.invalidatePrefix("stats:");
        switch (activeSection) {
            case DOMINANTES -> dominanteListComponent.refresh();
            case SESSIONS -> { sessionListComponent.setActiveCampaignId(activeCampaign == null ? -1 : activeCampaign.getId()); sessionListComponent.refresh(); }
            case CAMPAGNE -> campaignFormComponent.refreshFrom(activeCampaign);
            case STATS -> statsPanelComponent.refresh(activeCampaign == null ? -1 : activeCampaign.getId(), user == null ? null : user.getPromo());
            default -> dashboardRoot.refreshDashboardRoot(activeCampaign);
        }
        window.requestRenderIfNeeded();
    }

    private void refreshCurrentSection() { refreshAllData(); refreshActiveSection(); }

    private void onSidebarSelect(String key) {
        activeSection = switch (key) {
            case "dominantes" -> Section.DOMINANTES;
            case "sessions" -> Section.SESSIONS;
            case "campagne" -> Section.CAMPAGNE;
            case "stats" -> Section.STATS;
            default -> Section.DASHBOARD;
        };
        onResize(); applyActiveSectionVisibility(); refreshActiveSection();
    }

    // --- Getters for extracted classes ---
    public Campaign getActiveCampaign() { return activeCampaign; }
    public User getUser() { return user; }
    public BaseWindow getWindow() { return window; }
    public CampaignService getCampaignService() { return campaignService; }
    public SessionService getSessionService() { return sessionService; }
    public DominanteService getDominanteService() { return dominanteService; }
    public RegistrationService getRegistrationService() { return registrationService; }
    public StatisticsService getStatisticsService() { return statisticsService; }
    public dao.RegistrationDAO getRegistrationDAO() { return registrationDAO; }

    String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
    String resolveDisplayName(User u) { if (u == null) return ""; if (u.getFullName() != null && !u.getFullName().isBlank()) return u.getFullName(); return u.getLogin() == null ? "" : u.getLogin(); }

    private void clearChildren(BaseComp parent) { for (BaseComp c : new ArrayList<>(parent.getChildrenList())) parent.removeChild(c); }
}