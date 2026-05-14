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
import gui.components.PageHeader;
import gui.components.PrimaryButton;
import gui.components.SidebarMenu;
import gui.screen.components.SessionListComponent;
import gui.screen.components.CampaignFormComponent;
import gui.screen.components.DominanteListComponent;
import gui.screen.components.StatsPanelComponent;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.Dominante;
import model.SessionSlot;
import model.User;
import service.CampaignService;
import service.SessionService;
import service.DominanteService;
import service.StatisticsService;
import gui.components.SurfaceCard;

/**
 * Vue centralisée du tableau de bord administrateur.
 * Orchestr l'interface utilisateur complète avec navigation latérale et gestion des sections,
 * intègre les composants extraits (CampaignForm, SessionList, DominanteList, StatsPanel)
 * et coordonne les services pour les opérations métier.
 * 
 * Sections gérées :
 * - DASHBOARD : vue d'ensemble
 * - DOMINANTES : gestion des domaines d'étude
 * - SESSIONS : gestion des créneaux de présentation
 * - CAMPAGNE : configuration de la campagne
 * - STATS : statistiques et indicateurs
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class AdminDashboardView {
    private enum Section { DASHBOARD, DOMINANTES, SESSIONS, CAMPAGNE, STATS }

    private Section activeSection = Section.DASHBOARD;

    // extracted components
    private final SessionListComponent sessionListComponent;
    private final CampaignFormComponent campaignFormComponent;
    private final DominanteListComponent dominanteListComponent;
    private final StatsPanelComponent statsPanelComponent;
    private final BaseComp dashboardRoot;
    private SurfaceCard dashboardHeroCard;
    private SurfaceCard dashboardNoteCard;
    private Label dashboardSubtitleLabel;
    private Label dashboardStatusLabel;

    private Color PAGE_BG = new Color(14, 18, 26);
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaseWindow window;
    private final User user;

    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final StatisticsService statisticsService;

    // UI fields (kept package-private for delegator access if needed)
    final SidebarMenu sidebar;
    final PageHeader header;
    final PrimaryButton refreshButton;
    final BaseComp sectionHost;

    private boolean sectionsMounted;

    private Campaign activeCampaign;
    

    public AdminDashboardView(BaseWindow window, User user) {
        this.window = window;
        this.user = user;

        this.campaignService = new CampaignService();
        this.sessionService = new SessionService();
        this.dominanteService = new DominanteService();
        this.statisticsService = new StatisticsService();

        ArrayList<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("dashboard", "Tableau de bord"));
        items.add(new SidebarMenu.Item("dominantes", "Dominantes"));
        items.add(new SidebarMenu.Item("sessions", "Sessions"));
        items.add(new SidebarMenu.Item("campagne", "Campagne"));
        items.add(new SidebarMenu.Item("stats", "Statistiques"));
        this.sidebar = new SidebarMenu("Administration", resolveDisplayName(user), items, "dashboard", this::onSidebarSelect,
            () -> window.closeTopLayer(), () -> {});

        this.header = new PageHeader("Tableau de bord", "Vue d'ensemble de la campagne en cours");
        this.refreshButton = new PrimaryButton("Actualiser", 0, 0, 110, 28, this::refreshCurrentSection);
        this.refreshButton.setBackground(new Color(44, 54, 76));

        this.sectionHost = new BaseComp(null);
        this.dashboardRoot = createDashboardRoot();

        this.sessionListComponent = new SessionListComponent(window, sessionService, dominanteService);
        this.campaignFormComponent = new CampaignFormComponent(window);
        this.dominanteListComponent = new DominanteListComponent(window, dominanteService);
        this.statsPanelComponent = new StatsPanelComponent(window, statisticsService);

        // wire simple callbacks
        this.sessionListComponent.setOnEditSession(s -> openEditSessionModal(s));
        this.sessionListComponent.setOnManageSession(s -> openManageSessionModal(s, 0));
        this.sessionListComponent.setOnCreateSession(() -> openCreateSessionModal());

        this.campaignFormComponent.onSave(c -> saveCampaignFromComponent(c));
        this.dominanteListComponent.onCreate(() -> openCreateDominanteModal());
        this.dominanteListComponent.onEdit(d -> openEditDominanteModal(d));
    }

    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);
        content.addChild(sidebar);
        content.addChild(header);
        content.addChild(refreshButton);
        content.addChild(sectionHost);

        mountSectionsOnce();
        refreshAllData();
        onResize();
        applyActiveSectionVisibility();
        refreshActiveSection();
    }

    public void onResize() {
        BaseComp content = window.getContent();
        int w = content.getWidth();
        int h = content.getHeight();

        int sideW = Math.max(210, Math.min(248, w / 4));
        sidebar.setBounds(0, 0, sideW, h);

        int mainX = sideW + 16;
        int mainW = Math.max(320, w - mainX - 16);

        header.setBounds(mainX, 18, mainW, 52);
        refreshButton.setBounds(mainX + mainW - 110, 24, 110, 28);
        sectionHost.setBounds(mainX, 82, mainW, h - 98);

        dashboardRoot.setBounds(0, 0, mainW, sectionHost.getHeight());
        if (dashboardHeroCard != null) {
            dashboardHeroCard.setBounds(0, 0, mainW, 150);
        }
        if (dashboardNoteCard != null) {
            dashboardNoteCard.setBounds(0, 162, mainW, 74);
        }
        dominanteListComponent.onResize(mainW, sectionHost.getHeight());
        sessionListComponent.onResize(mainW, sectionHost.getHeight());
        campaignFormComponent.onResize(mainW);
        statsPanelComponent.onResize(mainW, sectionHost.getHeight());

        window.requestRenderIfNeeded();
    }

    // --- Business logic helpers moved here ---
    void refreshAllData() {
        activeCampaign = resolveActiveCampaign();
    }

    Campaign resolveActiveCampaign() {
        Campaign openCampaign = selectBestCampaign(campaignService.getCampaignsByStatus("OPEN"));
        if (openCampaign != null) return openCampaign;
        Campaign preparationCampaign = selectBestCampaign(campaignService.getCampaignsByStatus("PREPARATION"));
        if (preparationCampaign != null) return preparationCampaign;
        return selectBestCampaign(campaignService.getCampaignsByStatus("CLOSED"));
    }

    Campaign selectBestCampaign(List<Campaign> campaigns) {
        if (campaigns == null || campaigns.isEmpty()) return null;
        String promo = user == null ? null : user.getPromo();
        Campaign bestPromo = campaigns.stream()
            .filter(c -> promo != null && promo.equalsIgnoreCase(safe(c.getPromo())))
            .max(Comparator.comparing(this::campaignDateForSort).thenComparingInt(c -> c.getId()))
            .orElse(null);
        if (bestPromo != null) return bestPromo;
        return campaigns.stream().max(Comparator.comparing(this::campaignDateForSort).thenComparingInt(c -> c.getId())).orElse(campaigns.get(0));
    }

    LocalDate campaignDateForSort(Campaign campaign) {
        if (campaign == null || campaign.getRegistrationDay() == null || campaign.getRegistrationDay().isBlank()) return LocalDate.MIN;
        try { return LocalDate.parse(campaign.getRegistrationDay(), FR_DATE); } catch (Exception ignored) { return LocalDate.MIN; }
    }

    

    private void mountSectionsOnce() {
        if (sectionsMounted) {
            return;
        }
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
            case DOMINANTES -> header.setSubtitle("Gerez les domaines d'etudes disponibles — Dominantes");
            case SESSIONS -> header.setSubtitle("Gerez les creneaux de presentation — Sessions");
            case CAMPAGNE -> header.setSubtitle("Configurez les parametres generaux de la campagne d'inscriptions — Campagne");
            case STATS -> header.setSubtitle("Statistiques et analyse des inscriptions — Statistiques");
            default -> header.setSubtitle("Vue d'ensemble de la campagne en cours — Tableau de bord");
        }
    }

    private void refreshActiveSection() {
        switch (activeSection) {
            case DOMINANTES -> dominanteListComponent.refresh();
            case SESSIONS -> {
                sessionListComponent.setActiveCampaignId(activeCampaign == null ? -1 : activeCampaign.getId());
                sessionListComponent.refresh();
            }
            case CAMPAGNE -> campaignFormComponent.refreshFrom(activeCampaign);
            case STATS -> statsPanelComponent.refresh(activeCampaign == null ? -1 : activeCampaign.getId(), user == null ? null : user.getPromo());
            default -> refreshDashboardRoot();
        }
        window.requestRenderIfNeeded();
    }

    private void refreshCurrentSection() {
        refreshAllData();
        refreshActiveSection();
    }

    private void onSidebarSelect(String key) {
        if ("dominantes".equals(key)) activeSection = Section.DOMINANTES;
        else if ("sessions".equals(key)) activeSection = Section.SESSIONS;
        else if ("campagne".equals(key)) activeSection = Section.CAMPAGNE;
        else if ("stats".equals(key)) activeSection = Section.STATS;
        else activeSection = Section.DASHBOARD;
        onResize();
        applyActiveSectionVisibility();
        refreshActiveSection();
    }

    // --- basic modal and callback helpers (minimal implementations) ---
    private void openCreateSessionModal() {
        FormModal modal = new FormModal(520, 260, "Nouvelle session", window::closeTopLayer);
        Label l = new Label("Creation de session (prototype)", 12, 12, 400, 22);
        Button close = new Button("Fermer", 400, 200, 96, 34, window::closeTopLayer);
        modal.getBody().addChild(l);
        modal.getBody().addChild(close);
        window.openModal(modal);
    }

    private void openEditSessionModal(SessionSlot s) {
        if (s == null) return;
        FormModal modal = new FormModal(520, 260, "Modifier session", window::closeTopLayer);
        Label l = new Label("Edition session: " + safe(s.getTitle()), 12, 12, 400, 22);
        Button close = new Button("Fermer", 400, 200, 96, 34, window::closeTopLayer);
        modal.getBody().addChild(l);
        modal.getBody().addChild(close);
        window.openModal(modal);
    }

    private void openManageSessionModal(SessionSlot s, int allocated) {
        if (s == null) return;
        FormModal modal = new FormModal(620, 360, "Gerer session", window::closeTopLayer);
        Label l = new Label("Gerer: " + safe(s.getTitle()), 12, 12, 520, 22);
        Button close = new Button("Fermer", 500, 300, 96, 34, window::closeTopLayer);
        modal.getBody().addChild(l);
        modal.getBody().addChild(close);
        window.openModal(modal);
    }

    private void saveCampaignFromComponent(Campaign c) {
        if (c == null) return;
        int id = campaignService.createCampaign(c);
        if (id > 0) {
            campaignFormComponent.setFeedback("Campagne enregistree (id=" + id + ")");
            refreshAllData();
            refreshCurrentSection();
        } else {
            campaignFormComponent.setFeedback("Echec creation de la campagne.");
        }
    }

    private void openCreateDominanteModal() { FormModal modal = new FormModal(520, 260, "Nouvelle dominante", window::closeTopLayer); modal.getBody().addChild(new Label("Formulaire dominante (prototype)", 12,12,480,22)); modal.getBody().addChild(new Button("Fermer", 400,200,96,34,window::closeTopLayer)); window.openModal(modal); }

    private void openEditDominanteModal(Dominante d) { if (d == null) return; FormModal modal = new FormModal(520,260,"Modifier dominante", window::closeTopLayer); modal.getBody().addChild(new Label("Edition dominante: "+safe(d.getName()),12,12,480,22)); modal.getBody().addChild(new Button("Fermer",400,200,96,34,window::closeTopLayer)); window.openModal(modal); }

    private BaseComp createDashboardRoot() {
        BaseComp root = new BaseComp(null);

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

        SurfaceCard note = new SurfaceCard(0, 0, 100, 74, new Color(23, 30, 45), new Color(52, 63, 92), 12);
        Label noteTitle = new Label("Raccourcis", 16, 12, 100, 18);
        noteTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        noteTitle.setColor(new Color(210, 219, 237));
        Label noteText = new Label("Utilisez le menu à gauche pour naviguer. Le bouton Actualiser recharge uniquement la section active.", 16, 34, 560, 28);
        noteText.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        noteText.setColor(new Color(145, 158, 184));

        dashboardHeroCard.addChild(title);
        dashboardHeroCard.addChild(dashboardSubtitleLabel);
        dashboardHeroCard.addChild(dashboardStatusLabel);
        dashboardNoteCard = note;
        dashboardNoteCard.addChild(noteTitle);
        dashboardNoteCard.addChild(noteText);

        root.addChild(dashboardHeroCard);
        root.addChild(dashboardNoteCard);
        root.setVisible(false);
        return root;
    }

    private void refreshDashboardRoot() {
        if (dashboardSubtitleLabel != null && dashboardStatusLabel != null) {
            if (activeCampaign == null) {
                dashboardSubtitleLabel.setText("Aucune campagne ouverte actuellement");
                dashboardStatusLabel.setText("Passez par Campagne pour créer ou mettre à jour les paramètres.");
            } else {
                dashboardSubtitleLabel.setText("Campagne active: " + safe(activeCampaign.getName()) + " • Promo " + safe(activeCampaign.getPromo()));
                dashboardStatusLabel.setText("Période: " + safe(activeCampaign.getRegistrationDay()) + " • Statut: " + safe(activeCampaign.getStatus()));
            }
        }
    }

    // Utility helpers
    String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
    String resolveDisplayName(User user) { if (user == null) return ""; if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName(); return user.getLogin() == null ? "" : user.getLogin(); }

    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp child : snapshot) parent.removeChild(child); }
}
