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
import service.CacheManager;
import service.SessionService;
import service.DominanteService;
import service.StatisticsService;
import service.RegistrationService;
import gui.components.AlertContainer;
import gui.components.SurfaceCard;
import gui.components.ReusableLabeledInput;
import gui.components.ColorPicker;
import dao.RegistrationDAO;
import components.SelectInput;
import components.TextField;
import event.UiEvent;
import service.ServiceResult;
import components.ScrollView;

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

    private Color PAGE_BG = new Color(14, 18, 26);
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaseWindow window;
    private final User user;

    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final StatisticsService statisticsService;
    private final RegistrationService registrationService;
    private final RegistrationDAO registrationDAO;

    // UI fields (kept package-private for delegator access if needed)
    final SidebarMenu sidebar;
    final PageHeader header;
    final PrimaryButton refreshButton;
    final BaseComp sectionHost;

    private boolean sectionsMounted;
    private boolean darkMode = true;
    private int mainW = 320;

    private Campaign activeCampaign;
    

    public AdminDashboardView(BaseWindow window, User user) {
        this.window = window;
        this.user = user;

        this.campaignService = new CampaignService();
        this.sessionService = new SessionService();
        this.dominanteService = new DominanteService();
        this.statisticsService = new StatisticsService();
        this.registrationService = new RegistrationService();
        this.registrationDAO = new RegistrationDAO();

        ArrayList<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("dashboard", "Tableau de bord"));
        items.add(new SidebarMenu.Item("dominantes", "Dominantes"));
        items.add(new SidebarMenu.Item("sessions", "Sessions"));
        items.add(new SidebarMenu.Item("campagne", "Campagne"));
        items.add(new SidebarMenu.Item("stats", "Statistiques"));
        this.sidebar = new SidebarMenu("Administration", resolveDisplayName(user), items, "dashboard", this::onSidebarSelect,
            () -> window.closeTopLayer(), this::toggleTheme);
        this.darkMode = true;

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
        applyTheme();
        onResize();
        applyActiveSectionVisibility();
        refreshActiveSection();
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
        refreshActiveSection();
    }

    private void applyTheme() {
        if (darkMode) {
            PAGE_BG = new Color(14, 18, 26);
        } else {
            PAGE_BG = new Color(243, 246, 252);
        }
        sidebar.setDarkMode(darkMode);
        
        if (dashboardHeroCard != null) {
            dashboardHeroCard.setBackground(darkMode ? new Color(18, 24, 35) : Color.WHITE);
            dashboardHeroCard.setBorderColor(darkMode ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardKpiCard != null) {
            dashboardKpiCard.setBackground(darkMode ? new Color(22, 28, 39) : Color.WHITE);
            dashboardKpiCard.setBorderColor(darkMode ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardShortcutCard != null) {
            dashboardShortcutCard.setBackground(darkMode ? new Color(23, 30, 45) : Color.WHITE);
            dashboardShortcutCard.setBorderColor(darkMode ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        if (dashboardNoteCard != null) {
            dashboardNoteCard.setBackground(darkMode ? new Color(23, 30, 45) : Color.WHITE);
            dashboardNoteCard.setBorderColor(darkMode ? new Color(52, 63, 92) : new Color(226, 230, 238));
        }
        
        dashboardSessionsKpi.setDarkMode(darkMode);
        dashboardDominantesKpi.setDarkMode(darkMode);
        dashboardFillKpi.setDarkMode(darkMode);
        dashboardStudentsKpi.setDarkMode(darkMode);
        header.setDarkMode(darkMode);
        
        dominanteListComponent.setDarkMode(darkMode);
        sessionListComponent.setDarkMode(darkMode);
        statsPanelComponent.setDarkMode(darkMode);
        
        dashboardGoDominantes.setBackground(darkMode ? new Color(45, 54, 76) : new Color(236, 238, 242));
        dashboardGoSessions.setBackground(darkMode ? new Color(45, 54, 76) : new Color(236, 238, 242));
        dashboardGoCampagne.setBackground(darkMode ? new Color(45, 54, 76) : new Color(236, 238, 242));
        dashboardGoStats.setBackground(darkMode ? new Color(45, 54, 76) : new Color(236, 238, 242));
        
        dashboardSubtitleLabel.setColor(darkMode ? new Color(160, 173, 200) : new Color(26, 34, 49));
        dashboardStatusLabel.setColor(darkMode ? new Color(125, 140, 168) : new Color(76, 87, 104));
        
        BaseComp content = window.getContent();
        if (content != null) {
            content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        }
        window.requestRenderIfNeeded();
    }

    public void onResize() {
        BaseComp content = window.getContent();
        int w = content.getWidth();
        int h = content.getHeight();

        int sideW = Math.max(210, Math.min(248, w / 4));
        sidebar.setBounds(0, 0, sideW, h);

        int mainX = sideW + 16;
        this.mainW = Math.max(320, w - mainX - 16);

        header.setBounds(mainX, 18, mainW, 52);
        refreshButton.setBounds(mainX + mainW - 110, 24, 110, 28);
        sectionHost.setBounds(mainX, 82, mainW, h - 98);

        dashboardRoot.setBounds(0, 0, mainW, sectionHost.getHeight());
        if (dashboardHeroCard != null) {
            dashboardHeroCard.setBounds(0, 0, mainW, 150);
        }
        if (dashboardKpiCard != null) {
            dashboardKpiCard.setBounds(0, 162, mainW, 120);
            if (dashboardSessionsKpi != null) {
                dashboardSessionsKpi.setBounds(0, 0, (mainW - 36) / 4, 120);
                dashboardDominantesKpi.setBounds((mainW - 36) / 4 + 12, 0, (mainW - 36) / 4, 120);
                dashboardFillKpi.setBounds(((mainW - 36) / 4 + 12) * 2, 0, (mainW - 36) / 4, 120);
                dashboardStudentsKpi.setBounds(((mainW - 36) / 4 + 12) * 3, 0, (mainW - 36) / 4, 120);
            }
        }
        if (dashboardShortcutCard != null) {
            dashboardShortcutCard.setBounds(0, 294, mainW, 96);
            if (dashboardGoDominantes != null) {
                dashboardGoDominantes.setBounds(16, 38, 112, 32);
                dashboardGoSessions.setBounds(134, 38, 100, 32);
                dashboardGoCampagne.setBounds(240, 38, 104, 32);
                dashboardGoStats.setBounds(350, 38, 86, 32);
            }
        }
        if (dashboardNoteCard != null) {
            dashboardNoteCard.setBounds(0, 402, mainW, 74);
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

    private List<AlertContainer.AlertItem> generateDashboardAlerts() {
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
            case DOMINANTES -> {
                header.setTitle("Dominantes");
                header.setSubtitle("Gerez les domaines d'etudes disponibles");
            }
            case SESSIONS -> {
                header.setTitle("Sessions");
                header.setSubtitle("Gerez les creneaux de presentation");
            }
            case CAMPAGNE -> {
                header.setTitle("Campagne");
                header.setSubtitle("Configurez les parametres generaux");
            }
            case STATS -> {
                header.setTitle("Statistiques");
                header.setSubtitle("Analyse des inscriptions");
            }
            default -> {
                header.setTitle("Tableau de bord");
                header.setSubtitle("Vue d'ensemble de la campagne");
            }
        }
    }

private void refreshActiveSection() {
        CacheManager.invalidatePrefix("stats:");
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

    // --- modal helpers ---
    private void openCreateSessionModal() {
        FormModal modal = new FormModal(520, 380, "Nouvelle session", window::closeTopLayer);
        BaseComp body = modal.getBody();

        int row1Y = 8;
        ReusableLabeledInput titleInput = new ReusableLabeledInput("Titre", "", 16, row1Y, 230, 54);
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date (yyyy-MM-dd)", "", 254, row1Y, 234, 54);

        int row2Y = 70;
        ReusableLabeledInput startInput = new ReusableLabeledInput("Debut (min)", "510", 16, row2Y, 110, 54);
        ReusableLabeledInput endInput = new ReusableLabeledInput("Fin (min)", "570", 132, row2Y, 110, 54);
        ReusableLabeledInput roomInput = new ReusableLabeledInput("Salle", "", 248, row2Y, 240, 54);
        ReusableLabeledInput capacityInput = new ReusableLabeledInput("Capacite", "30", 16, row2Y + 62, 110, 54);

        int domY = 194;
        Label domLabel = new Label("Dominante", 16, domY, 120, 16);
        domLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        domLabel.setColor(new Color(100, 110, 130));
        SelectInput domSelect = new SelectInput(16, domY + 18, 472, 30);

        List<String> domOptions = new ArrayList<>();
        for (Dominante d : dominanteService.listAll()) domOptions.add(d.getName());
        domSelect.setOptions(domOptions);

        int btnY = 296;
        Label feedback = new Label("", 16, btnY, 300, 16);
        feedback.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedback.setColor(new Color(239, 68, 68));

        Button cancelBtn = new Button("Annuler", 296, btnY, 100, 30, window::closeTopLayer);
        cancelBtn.setBackground(new Color(40, 51, 73));
        cancelBtn.setForeground(new Color(219, 230, 253));

        Button createBtn = new Button("Creer", 404, btnY, 100, 30, () -> {
            if (activeCampaign == null || activeCampaign.getId() <= 0) { feedback.setText("Aucune campagne active."); return; }
            List<Dominante> allDom = dominanteService.listAll();
            String selected = domSelect.getSelectedOption();
            int domId = -1;
            for (Dominante d : allDom) if (d.getName().equals(selected)) { domId = d.getId(); break; }
            if (domId <= 0) { feedback.setText("Selectionnez une dominante."); return; }

            SessionSlot s = new SessionSlot();
            s.setCampaignId(activeCampaign.getId());
            s.setDominanteId(domId);
            s.setTitle(titleInput.getValue());
            s.setSessionDate(dateInput.getValue());
            try { s.setStartMinute(Integer.parseInt(startInput.getValue())); } catch (Exception e) { s.setStartMinute(0); }
            try { s.setEndMinute(Integer.parseInt(endInput.getValue())); } catch (Exception e) { s.setEndMinute(0); }
            s.setRoom(roomInput.getValue());
            try { s.setCapacity(Integer.parseInt(capacityInput.getValue())); } catch (Exception e) { s.setCapacity(0); }

            ServiceResult r = sessionService.createSession(s);
            if (r.isSuccess()) { window.closeTopLayer(); refreshActiveSection(); }
            else { feedback.setText(r.getMessage()); }
        });
        createBtn.setBackground(new Color(30, 93, 57));
        createBtn.setForeground(new Color(233, 247, 238));

        body.addChild(titleInput);
        body.addChild(dateInput);
        body.addChild(startInput);
        body.addChild(endInput);
        body.addChild(roomInput);
        body.addChild(capacityInput);
        body.addChild(domLabel);
        body.addChild(domSelect);
        body.addChild(feedback);
        body.addChild(cancelBtn);
        body.addChild(createBtn);

        window.openModal(modal);
    }

    private void openEditSessionModal(SessionSlot s) {
        if (s == null) return;
        FormModal modal = new FormModal(520, 380, "Modifier session", window::closeTopLayer);
        BaseComp body = modal.getBody();

        int row1Y = 8;
        ReusableLabeledInput titleInput = new ReusableLabeledInput("Titre", safe(s.getTitle()), 16, row1Y, 230, 54);
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date (yyyy-MM-dd)", safe(s.getSessionDate()), 254, row1Y, 234, 54);

        int row2Y = 70;
        ReusableLabeledInput startInput = new ReusableLabeledInput("Debut (min)", String.valueOf(s.getStartMinute()), 16, row2Y, 110, 54);
        ReusableLabeledInput endInput = new ReusableLabeledInput("Fin (min)", String.valueOf(s.getEndMinute()), 132, row2Y, 110, 54);
        ReusableLabeledInput roomInput = new ReusableLabeledInput("Salle", safe(s.getRoom()), 248, row2Y, 240, 54);
        ReusableLabeledInput capacityInput = new ReusableLabeledInput("Capacite", String.valueOf(s.getCapacity()), 16, row2Y + 62, 110, 54);

        int domY = 194;
        Label domLabel = new Label("Dominante", 16, domY, 120, 16);
        domLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        domLabel.setColor(new Color(100, 110, 130));
        SelectInput domSelect = new SelectInput(16, domY + 18, 472, 30);
        Dominante selectedDom = null;
        List<String> domOptions = new ArrayList<>();
        for (Dominante d : dominanteService.listAll()) {
            domOptions.add(d.getName());
            if (d.getId() == s.getDominanteId()) selectedDom = d;
        }
        domSelect.setOptions(domOptions);
        if (selectedDom != null) domSelect.setSelectedOption(selectedDom.getName());

        int btnY = 296;
        Label feedback = new Label("", 16, btnY, 300, 16);
        feedback.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedback.setColor(new Color(239, 68, 68));

        Button cancelBtn = new Button("Annuler", 296, btnY, 100, 30, window::closeTopLayer);
        cancelBtn.setBackground(new Color(40, 51, 73));
        cancelBtn.setForeground(new Color(219, 230, 253));

        Button saveBtn = new Button("Enregistrer", 404, btnY, 100, 30, () -> {
            List<Dominante> allDom = dominanteService.listAll();
            String sel = domSelect.getSelectedOption();
            int domId = -1;
            for (Dominante d : allDom) if (d.getName().equals(sel)) { domId = d.getId(); break; }
            if (domId <= 0) { feedback.setText("Selectionnez une dominante."); return; }

            s.setTitle(titleInput.getValue());
            s.setSessionDate(dateInput.getValue());
            try { s.setStartMinute(Integer.parseInt(startInput.getValue())); } catch (Exception e) { }
            try { s.setEndMinute(Integer.parseInt(endInput.getValue())); } catch (Exception e) { }
            s.setRoom(roomInput.getValue());
            try { s.setCapacity(Integer.parseInt(capacityInput.getValue())); } catch (Exception e) { }

            ServiceResult r = sessionService.updateSession(s);
            if (r.isSuccess()) { window.closeTopLayer(); refreshActiveSection(); }
            else { feedback.setText(r.getMessage()); }
        });
        saveBtn.setBackground(new Color(30, 93, 57));
        saveBtn.setForeground(new Color(233, 247, 238));

        body.addChild(titleInput);
        body.addChild(dateInput);
        body.addChild(startInput);
        body.addChild(endInput);
        body.addChild(roomInput);
        body.addChild(capacityInput);
        body.addChild(domLabel);
        body.addChild(domSelect);
        body.addChild(feedback);
        body.addChild(cancelBtn);
        body.addChild(saveBtn);

        window.openModal(modal);
    }

    private void openManageSessionModal(SessionSlot s, int allocated) {
        if (s == null) return;
        FormModal modal = new FormModal(620, 420, "Gerer session", window::closeTopLayer);
        BaseComp body = modal.getBody();

        Dominante d = dominanteService.findById(s.getDominanteId());
        String domName = d != null ? d.getName() : "Dominante #" + s.getDominanteId();

        Label sessionTitle = new Label(domName + " - " + safe(s.getTitle()), 16, 10, 580, 22);
        sessionTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        sessionTitle.setColor(new Color(27, 39, 56));

        Label sessionDetails = new Label(safe(s.getSessionDate()) + "  |  " + formatMinute(s.getStartMinute()) + "-" + formatMinute(s.getEndMinute()) + "  |  " + safe(s.getRoom()) + "  |  " + allocated + "/" + s.getCapacity() + " places", 16, 36, 580, 16);
        sessionDetails.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        sessionDetails.setColor(new Color(100, 116, 139));

        int editCapY = 58;
        Label feedbackLabel = new Label("", 244, editCapY + 18, 200, 16);
        feedbackLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedbackLabel.setColor(new Color(239, 68, 68));

        ReusableLabeledInput capInput = new ReusableLabeledInput("Modifier capacite", String.valueOf(s.getCapacity()), 16, editCapY, 160, 52);
        Button updateCapBtn = new Button("OK", 184, editCapY + 14, 50, 28, () -> {
            try {
                int newCap = Integer.parseInt(capInput.getValue());
                boolean ok = sessionService.updateCapacity(s.getId(), newCap);
                if (ok) { s.setCapacity(newCap); capInput.setValue(String.valueOf(newCap)); feedbackLabel.setText("Capacite mise a jour."); feedbackLabel.setColor(new Color(34, 197, 94)); refreshActiveSection(); }
                else { feedbackLabel.setText("Modification interdite."); feedbackLabel.setColor(new Color(239, 68, 68)); }
            } catch (Exception ex) { feedbackLabel.setText("Capacite invalide."); feedbackLabel.setColor(new Color(239, 68, 68)); }
        });
        updateCapBtn.setBackground(new Color(59, 130, 246));
        updateCapBtn.setForeground(Color.WHITE);

        Label studentsTitle = new Label("Etudiants inscrits (" + allocated + ")", 16, 120, 200, 16);
        studentsTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        studentsTitle.setColor(new Color(27, 39, 56));

        ScrollView scroll = new ScrollView(8, 142, 596, 220);

        Button closeBtn = new Button("Fermer", 252, 372, 110, 30, window::closeTopLayer);
        closeBtn.setBackground(new Color(40, 51, 73));
        closeBtn.setForeground(new Color(219, 230, 253));

        body.addChild(sessionTitle);
        body.addChild(sessionDetails);
        body.addChild(capInput);
        body.addChild(updateCapBtn);
        body.addChild(feedbackLabel);
        body.addChild(studentsTitle);
        body.addChild(scroll);
        body.addChild(closeBtn);

        window.openModal(modal);
    }

    private void openCreateDominanteModal() {
        FormModal modal = new FormModal(520, 440, "Nouvelle dominante", window::closeTopLayer);
        BaseComp body = modal.getBody();

        int row1Y = 8;
        ReusableLabeledInput codeInput = new ReusableLabeledInput("Code (ex: IA)", "", 16, row1Y, 200, 52);
        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom", "", 224, row1Y, 268, 52);

        int row2Y = 68;
        ReusableLabeledInput descInput = new ReusableLabeledInput("Description", "", 16, row2Y, 476, 52);

        int row3Y = 128;
        ReusableLabeledInput respInput = new ReusableLabeledInput("Responsable", "", 16, row3Y, 320, 52);

        int colorY = 188;
        Label colorLabel = new Label("Couleur", 16, colorY, 120, 16);
        colorLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        colorLabel.setColor(new Color(100, 110, 130));
        ColorPicker colorPicker = new ColorPicker(16, colorY + 18, 200, 76, null);

        int btnY = 324;
        Label feedback = new Label("", 16, btnY, 280, 16);
        feedback.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedback.setColor(new Color(239, 68, 68));

        Button cancelBtn = new Button("Annuler", 296, btnY, 100, 30, window::closeTopLayer);
        cancelBtn.setBackground(new Color(40, 51, 73));
        cancelBtn.setForeground(new Color(219, 230, 253));

        Button createBtn = new Button("Creer", 404, btnY, 100, 30, () -> {
            String code = codeInput.getValue().trim().toUpperCase();
            String name = nameInput.getValue().trim();
            if (code.isEmpty() || name.isEmpty()) { feedback.setText("Code et nom obligatoires."); return; }

            Dominante dom = new Dominante();
            dom.setCode(code);
            dom.setName(name);
            dom.setDescription(descInput.getValue().trim());
            dom.setResponsibleName(respInput.getValue().trim());
            dom.setColor(String.format("#%02x%02x%02x", colorPicker.getSelectedColor().getRed(), colorPicker.getSelectedColor().getGreen(), colorPicker.getSelectedColor().getBlue()));
            dom.setActive(true);

            ServiceResult r = dominanteService.create(dom);
            if (r.isSuccess()) { window.closeTopLayer(); refreshActiveSection(); }
            else { feedback.setText(r.getMessage()); }
        });
        createBtn.setBackground(new Color(30, 93, 57));
        createBtn.setForeground(new Color(233, 247, 238));

        body.addChild(codeInput);
        body.addChild(nameInput);
        body.addChild(descInput);
        body.addChild(respInput);
        body.addChild(colorLabel);
        body.addChild(colorPicker);
        body.addChild(feedback);
        body.addChild(cancelBtn);
        body.addChild(createBtn);

        window.openModal(modal);
    }

    private void openEditDominanteModal(Dominante d) {
        if (d == null) return;
        FormModal modal = new FormModal(520, 440, "Modifier dominante", window::closeTopLayer);
        BaseComp body = modal.getBody();

        int row1Y = 8;
        ReusableLabeledInput codeInput = new ReusableLabeledInput("Code", d.getCode(), 16, row1Y, 200, 52);
        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom", d.getName(), 224, row1Y, 268, 52);

        int row2Y = 68;
        ReusableLabeledInput descInput = new ReusableLabeledInput("Description", d.getDescription(), 16, row2Y, 476, 52);

        int row3Y = 128;
        ReusableLabeledInput respInput = new ReusableLabeledInput("Responsable", d.getResponsibleName(), 16, row3Y, 320, 52);

        int colorY = 188;
        Label colorLabel = new Label("Couleur", 16, colorY, 120, 16);
        colorLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        colorLabel.setColor(new Color(100, 110, 130));
        ColorPicker colorPicker = new ColorPicker(16, colorY + 18, 200, 76, d.getColor());

        int btnY = 324;
        Label feedback = new Label("", 16, btnY, 280, 16);
        feedback.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedback.setColor(new Color(239, 68, 68));

        Button cancelBtn = new Button("Annuler", 296, btnY, 100, 30, window::closeTopLayer);
        cancelBtn.setBackground(new Color(40, 51, 73));
        cancelBtn.setForeground(new Color(219, 230, 253));

        Button saveBtn = new Button("Enregistrer", 404, btnY, 100, 30, () -> {
            String code = codeInput.getValue().trim().toUpperCase();
            String name = nameInput.getValue().trim();
            if (code.isEmpty() || name.isEmpty()) { feedback.setText("Code et nom obligatoires."); return; }

            d.setCode(code);
            d.setName(name);
            d.setDescription(descInput.getValue().trim());
            d.setResponsibleName(respInput.getValue().trim());
            d.setColor(String.format("#%02x%02x%02x", colorPicker.getSelectedColor().getRed(), colorPicker.getSelectedColor().getGreen(), colorPicker.getSelectedColor().getBlue()));

            ServiceResult r = dominanteService.update(d);
            if (r.isSuccess()) { window.closeTopLayer(); refreshActiveSection(); }
            else { feedback.setText(r.getMessage()); }
        });
        saveBtn.setBackground(new Color(30, 93, 57));
        saveBtn.setForeground(new Color(233, 247, 238));

        body.addChild(codeInput);
        body.addChild(nameInput);
        body.addChild(descInput);
        body.addChild(respInput);
        body.addChild(colorLabel);
        body.addChild(colorPicker);
        body.addChild(feedback);
        body.addChild(cancelBtn);
        body.addChild(saveBtn);

        window.openModal(modal);
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
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

    private void openManageStudentModal(User student) {
        if (student == null || activeCampaign == null) return;
        FormModal modal = new FormModal(640, 500, "Inscrire - " + safe(student.getFullName()), window::closeTopLayer);
        BaseComp body = modal.getBody();

        Label studentLabel = new Label("Etudiant: " + safe(student.getFullName()) + " (" + safe(student.getLogin()) + ")", 16, 10, 600, 20);
        studentLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        studentLabel.setColor(new Color(27, 39, 56));

        Label domLabel = new Label("Selectionnez une dominante", 16, 42, 200, 16);
        domLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        domLabel.setColor(new Color(100, 110, 130));

        SelectInput domSelect = new SelectInput(16, 60, 280, 30);
        List<String> domOptions = new ArrayList<>();
        for (Dominante d : dominanteService.listAll()) domOptions.add(d.getName());
        domSelect.setOptions(domOptions);

        int sessionY = 100;
        Label sessionLabel = new Label("Sessions disponibles", 16, sessionY, 200, 16);
        sessionLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        sessionLabel.setColor(new Color(100, 110, 130));

        ScrollView sessionScroll = new ScrollView(8, sessionY + 18, 616, 220);

        Label feedbackLabel = new Label("", 16, 348, 400, 16);
        feedbackLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedbackLabel.setColor(new Color(239, 68, 68));

        Button closeBtn = new Button("Fermer", 252, 430, 110, 30, window::closeTopLayer);
        closeBtn.setBackground(new Color(40, 51, 73));
        closeBtn.setForeground(new Color(219, 230, 253));

        domSelect.setOnChange(selected -> {
            refreshSessionListForDominante(student, selected, sessionScroll, feedbackLabel, modal);
        });

        body.addChild(studentLabel);
        body.addChild(domLabel);
        body.addChild(domSelect);
        body.addChild(sessionLabel);
        body.addChild(sessionScroll);
        body.addChild(feedbackLabel);
        body.addChild(closeBtn);

        window.openModal(modal);
    }

    private void refreshSessionListForDominante(User student, String dominanteName, ScrollView sessionScroll, Label feedbackLabel, FormModal modal) {
        BaseComp list = sessionScroll.getContent();
        clearChildren(list);

        Dominante selectedDom = null;
        for (Dominante d : dominanteService.listAll()) {
            if (d.getName().equals(dominanteName)) { selectedDom = d; break; }
        }

        if (selectedDom == null) return;

        List<SessionSlot> sessions = sessionService.listByCampaign(activeCampaign.getId());
        int y = 0;
        boolean foundAvailable = false;

        for (SessionSlot s : sessions) {
            if (s.getDominanteId() != selectedDom.getId()) continue;

            RegistrationService.ConflictResult check = registrationService.checkRegistration(activeCampaign.getId(), student.getId(), s.getId());
            int allocated = registrationService.getStudentRegistrations(activeCampaign.getId(), student.getId()).size();
            int slotAllocated = registrationDAO.countAllocatedBySession(activeCampaign.getId(), s.getId());

            SurfaceCard sessionCard = new SurfaceCard(0, y, sessionScroll.getWidth() - 16, 60,
                check.hasConflict ? new Color(255, 240, 240) : (check.sessionFull ? new Color(255, 245, 230) : new Color(240, 248, 255)),
                new Color(226, 230, 238), 6);

            String statusText;
            java.awt.Color statusColor;
            if (check.hasConflict) {
                statusText = "Conflit: " + check.conflictMessage;
                statusColor = new Color(196, 61, 61);
            } else if (check.sessionFull) {
                statusText = "Session complete (" + slotAllocated + "/" + s.getCapacity() + ")";
                statusColor = new Color(180, 120, 20);
            } else {
                statusText = (s.getCapacity() - slotAllocated) + " places";
                statusColor = new Color(34, 197, 94);
                foundAvailable = true;
            }

            Label sTitle = new Label(safe(s.getTitle()), 10, 8, 300, 18);
            sTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            sTitle.setColor(new Color(27, 39, 56));

            Label sInfo = new Label(safe(s.getSessionDate()) + " | " + formatMinute(s.getStartMinute()) + "-" + formatMinute(s.getEndMinute()) + " | " + safe(s.getRoom()), 10, 28, 400, 16);
            sInfo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
            sInfo.setColor(new Color(100, 116, 139));

            Label sStatus = new Label(statusText, 10, 44, 300, 14);
            sStatus.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
            sStatus.setColor(statusColor);

            sessionCard.addChild(sTitle);
            sessionCard.addChild(sInfo);
            sessionCard.addChild(sStatus);

            if (!check.hasConflict && !check.sessionFull) {
                Button regBtn = new Button("Inscrire", sessionScroll.getWidth() - 100, 16, 76, 26, () -> {
                    ServiceResult r = registrationService.registerStudent(activeCampaign.getId(), student.getId(), s.getId(), true);
                    if (r.isSuccess()) {
                        feedbackLabel.setText("Inscrit avec succes!");
                        feedbackLabel.setColor(new Color(34, 197, 94));
                        refreshSessionListForDominante(student, dominanteName, sessionScroll, feedbackLabel, modal);
                    } else {
                        feedbackLabel.setText(r.getMessage());
                        feedbackLabel.setColor(new Color(239, 68, 68));
                    }
                });
                regBtn.setBackground(new Color(30, 93, 57));
                regBtn.setForeground(new Color(233, 247, 238));
                sessionCard.addChild(regBtn);
            } else if (check.sessionFull && check.alternatives != null && !check.alternatives.isEmpty()) {
                Button altBtn = new Button("Alternative", sessionScroll.getWidth() - 110, 16, 90, 26, () -> {
                    RegistrationService.AlternativeSession alt = check.alternatives.get(0);
                    ServiceResult r = registrationService.registerStudent(activeCampaign.getId(), student.getId(), alt.sessionId, true);
                    if (r.isSuccess()) {
                        feedbackLabel.setText("Inscrit a: " + alt.title);
                        feedbackLabel.setColor(new Color(34, 197, 94));
                        refreshSessionListForDominante(student, dominanteName, sessionScroll, feedbackLabel, modal);
                    } else {
                        feedbackLabel.setText(r.getMessage());
                        feedbackLabel.setColor(new Color(239, 68, 68));
                    }
                });
                altBtn.setBackground(new Color(245, 158, 11));
                altBtn.setForeground(Color.WHITE);
                sessionCard.addChild(altBtn);
            }

            list.addChild(sessionCard);
            y += 66;
        }

        if (y == 0) {
            Label noSessions = new Label("Aucune session pour cette dominante", 0, 0, 300, 24);
            noSessions.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            noSessions.setColor(new Color(100, 116, 139));
            list.addChild(noSessions);
            y = 24;
        }

        sessionScroll.setContentHeight(Math.max(sessionScroll.getHeight(), y + 10));
    }

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
        dashboardGoDominantes = new PrimaryButton("Dominantes", 16, 38, 112, 32, () -> onSidebarSelect("dominantes"));
        dashboardGoSessions = new PrimaryButton("Sessions", 134, 38, 100, 32, () -> onSidebarSelect("sessions"));
        dashboardGoCampagne = new PrimaryButton("Campagne", 240, 38, 104, 32, () -> onSidebarSelect("campagne"));
        dashboardGoStats = new PrimaryButton("Stats", 350, 38, 86, 32, () -> onSidebarSelect("stats"));
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

        dashboardHeroCard.addChild(title);
        dashboardHeroCard.addChild(dashboardSubtitleLabel);
        dashboardHeroCard.addChild(dashboardStatusLabel);

        root.addChild(dashboardHeroCard);
        root.addChild(dashboardKpiCard);
        root.addChild(dashboardShortcutCard);
        root.addChild(dashboardAlertContainer.getRoot());
        root.addChild(dashboardNoteCard);
        root.setVisible(false);
        return root;
    }

private void refreshDashboardRoot() {
        CacheManager.invalidatePrefix("stats:");
        if (dashboardSubtitleLabel != null && dashboardStatusLabel != null) {
            if (activeCampaign == null) {
                dashboardSubtitleLabel.setText("Aucune campagne ouverte actuellement");
                dashboardStatusLabel.setText("Passez par Campagne pour creer ou mettre a jour les parametres.");
                if (dashboardSessionsKpi != null) {
                    dashboardSessionsKpi.setValue("0");
                    dashboardDominantesKpi.setValue(String.valueOf(dominanteService.listAll().size()));
                    dashboardFillKpi.setValue("0%");
                    dashboardStudentsKpi.setValue("0");
                }
            } else {
                dashboardSubtitleLabel.setText("Campagne: " + safe(activeCampaign.getName()) + " | Promo " + safe(activeCampaign.getPromo()));
                dashboardStatusLabel.setText("Periode: " + safe(activeCampaign.getRegistrationDay()) + " | Statut: " + safe(activeCampaign.getStatus()));
                StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(activeCampaign.getId(), activeCampaign.getPromo());
                if (dashboardSessionsKpi != null) {
                    dashboardSessionsKpi.setValue(String.valueOf(stats.totalSessions));
                    dashboardDominantesKpi.setValue(String.valueOf(dominanteService.listAll().size()));
                    dashboardFillKpi.setValue(String.format("%.1f%%", stats.averageFillRate));
                    dashboardStudentsKpi.setValue(String.valueOf(stats.registeredStudents));
                }
            }

            if (dashboardAlertContainer != null) {
                List<AlertContainer.AlertItem> alerts = generateDashboardAlerts();
                dashboardAlertContainer.setAlerts(alerts);
            }
        }
    }

    // Utility helpers
    String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
    String resolveDisplayName(User user) { if (user == null) return ""; if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName(); return user.getLogin() == null ? "" : user.getLogin(); }

    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp child : snapshot) parent.removeChild(child); }
}
