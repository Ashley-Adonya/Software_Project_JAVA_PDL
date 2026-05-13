package gui.screen;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import components.Button;
import components.ConfirmDialog;
// import components.Div;
import components.FormModal;
import components.Label;
import components.ScrollView;
import components.SelectInput;
import components.TextAreaInput;
import gui.components.DominanteCardAdmin;
import gui.components.DominanteOverviewRow;
import gui.components.ColorPicker;
import gui.components.KpiCard;
import gui.components.PageHeader;
import gui.components.PrimaryButton;
import gui.components.ReusableLabeledInput;
import gui.components.SessionRowAdmin;
import gui.components.SidebarMenu;
import gui.components.SurfaceCard;
import gui.navigation.AppScreen;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.Dominante;
import model.Registration;
import model.SessionSlot;
import model.User;
import service.CampaignService;
import service.ServiceResult;
import service.SessionService;
import service.AssignmentService;
import service.DominanteService;
import service.StatisticsService;
import service.RegistrationService;

public class AdminDashboardScreen implements AppScreen {
    private enum Section {
        DASHBOARD,
        DOMINANTES,
        SESSIONS,
        CAMPAGNE,
        STATS
    }

    private static class DominanteStat {
        Dominante dominante;
        int sessionCount;
        int capacity;
        int allocated;
    }

    private Color PAGE_BG = new Color(14, 18, 26);
    private Color PANEL_BG = new Color(22, 28, 39);
    private Color PANEL_BORDER = new Color(48, 60, 82);
    private Color TEXT_MAIN = new Color(235, 241, 255);
    private Color TEXT_MUTED = new Color(151, 166, 194);
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaseWindow window;
    private final User user;

    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final AssignmentService assignmentService;
    private final StatisticsService statisticsService;
    private final RegistrationService registrationService;

    private final SidebarMenu sidebar;
    private final PageHeader header;
    private final PrimaryButton refreshButton;

    private final BaseComp sectionHost;

    private final BaseComp dashboardSection;
    private final ScrollView dashboardScroll;
    private final BaseComp dashboardContent;
    private final SurfaceCard campaignCard;
    private final Label campaignTitle;
    private final Label campaignDates;
    private final Label campaignStatus;
    private final PrimaryButton campaignActionButton;
    private final KpiCard dominanteKpi;
    private final KpiCard sessionsKpi;
    private final KpiCard inscriptionsKpi;
    private final KpiCard fillRateKpi;
    private final SurfaceCard dominanteOverviewCard;
    private final Label dominanteOverviewTitle;
    private final Label dominanteOverviewSubtitle;
    private final ArrayList<DominanteOverviewRow> dominanteRows;
    private final SurfaceCard campaignSettingsCard;
    private final Label campaignSettingsTitle;
    private final Label maxChoicesLabel;
    private final Label maxChoicesValue;
    private final Label startDateLabel;
    private final Label startDateValue;
    private final Label endDateLabel;
    private final Label endDateValue;

    private final BaseComp dominantesSection;
    private final Button nouvelleDominanteButton;
    private final ScrollView dominantesScroll;
    private final BaseComp dominantesList;

    private final BaseComp sessionsSection;
    private final PrimaryButton nouvelleSessionButton;
    private final SurfaceCard sessionsFilterCard;
    private final Label sessionsTotalLabel;
    private final SelectInput dominanteFilter;
    private final ScrollView sessionsScroll;
    private final BaseComp sessionsList;

    private final BaseComp campagneSection;
    private final ScrollView campagneScroll;
    private final BaseComp campagneContent;
    private final SurfaceCard campaignFormCard;
    private final ReusableLabeledInput campaignNameInput;
    private final ReusableLabeledInput registrationDateInput;
    private final ReusableLabeledInput maxChoicesInput;
    private final ReusableLabeledInput startDateInput;
    private final ReusableLabeledInput endDateInput;
    private final PrimaryButton saveCampaignButton;
    private final PrimaryButton openCampaignButton;
    private final PrimaryButton closeCampaignButton;
    private final PrimaryButton autoAssignButton;
    private final Label campaignFeedbackLabel;

    private final BaseComp statsSection;
    private final ScrollView statsScroll;
    private final BaseComp statsContent;
    private final KpiCard statsTotalSessionsKpi;
    private final KpiCard statsCompleteSessionsKpi;
    private final KpiCard statsFillRateKpi;
    private final KpiCard statsUnregisteredKpi;
    private final SurfaceCard unregisteredStudentsCard;
    private final Label unregisteredStudentsTitle;
    private final ScrollView unregisteredScroll;
    private final BaseComp unregisteredList;
    private final SurfaceCard studentPlanningCard;
    private final Label studentPlanningTitle;
    private final Label selectedStudentLabel;
    private final ScrollView planningScroll;
    private final BaseComp planningList;
    private final Label noStudentSelectedLabel;
    private User selectedStudentForPlanning;
    private Campaign activeCampaign;
    private Section activeSection;
    private boolean dominanteDarkMode;
    private ArrayList<Dominante> cachedDominantes = new ArrayList<>();
    private boolean dominantesCacheDirty = true;
    private ArrayList<SessionSlot> cachedSessions = new ArrayList<>();
    private int cachedSessionsCampaignId = -1;
    private boolean sessionsCacheDirty = true;
    private Map<Integer, Integer> cachedAllocationsBySession = new HashMap<>();
    private int cachedAllocationsCampaignId = -1;
    private boolean allocationsCacheDirty = true;

    public AdminDashboardScreen(BaseWindow window, User user, Runnable onLogout) {
        this.window = window;
        this.user = user;

        this.campaignService = new CampaignService();
        this.sessionService = new SessionService();
        this.dominanteService = new DominanteService();
        this.assignmentService = new AssignmentService();
        this.statisticsService = new StatisticsService();
        this.registrationService = new RegistrationService();

        ArrayList<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("dashboard", "Tableau de bord"));
        items.add(new SidebarMenu.Item("dominantes", "Dominantes"));
        items.add(new SidebarMenu.Item("sessions", "Sessions"));
        items.add(new SidebarMenu.Item("campagne", "Campagne"));
        items.add(new SidebarMenu.Item("stats", "Statistiques"));
        this.sidebar = new SidebarMenu("Administration", resolveDisplayName(user), items, "dashboard", this::onSidebarSelect,
                onLogout, this::toggleGlobalTheme);

        this.header = new PageHeader("Tableau de bord", "Vue d'ensemble de la campagne en cours");
        this.refreshButton = new PrimaryButton("Actualiser", 0, 0, 110, 28, this::refreshCurrentSection);
        this.refreshButton.setBackground(new Color(44, 54, 76));

        this.sectionHost = new BaseComp(null);

        this.dashboardSection = new BaseComp(null);
        this.dashboardScroll = new ScrollView(0, 0, 100, 100);
        this.dashboardContent = dashboardScroll.getContent();
        dashboardSection.addChild(dashboardScroll);

        this.campaignCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.campaignTitle = new Label("Campagne", 0, 0, 100, 24);
        this.campaignTitle.setFont(new Font("Dialog", Font.BOLD, 27));
        this.campaignTitle.setColor(TEXT_MAIN);
        this.campaignDates = new Label("", 0, 0, 100, 20);
        this.campaignDates.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.campaignDates.setColor(TEXT_MUTED);
        this.campaignStatus = new Label("", 0, 0, 200, 20);
        this.campaignStatus.setFont(new Font("Dialog", Font.BOLD, 12));
        this.campaignStatus.setColor(new Color(42, 144, 82));
        this.campaignActionButton = new PrimaryButton("Fermer les inscriptions", 0, 0, 190, 34, this::toggleCampaignStatus);
        campaignCard.addChild(campaignTitle);
        campaignCard.addChild(campaignDates);
        campaignCard.addChild(campaignStatus);
        campaignCard.addChild(campaignActionButton);

        this.dominanteKpi = new KpiCard("Dominantes", "0", "Domaines disponibles", new Color(126, 79, 255));
        this.sessionsKpi = new KpiCard("Sessions", "0", "Sessions programmees", new Color(73, 139, 255));
        this.inscriptionsKpi = new KpiCard("Inscriptions", "0", "Sur 0 places", new Color(34, 197, 94));
        this.fillRateKpi = new KpiCard("Taux de remplissage", "0%", "Taux global", new Color(245, 158, 11));

        this.dominanteOverviewCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.dominanteOverviewTitle = new Label("Vue d'ensemble des dominantes", 0, 0, 300, 22);
        this.dominanteOverviewTitle.setFont(new Font("Dialog", Font.BOLD, 16));
        this.dominanteOverviewTitle.setColor(TEXT_MAIN);
        this.dominanteOverviewSubtitle = new Label("Sessions et inscriptions par dominante", 0, 0, 360, 16);
        this.dominanteOverviewSubtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.dominanteOverviewSubtitle.setColor(TEXT_MUTED);
        dominanteOverviewCard.addChild(dominanteOverviewTitle);
        dominanteOverviewCard.addChild(dominanteOverviewSubtitle);

        this.dominanteRows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DominanteOverviewRow row = new DominanteOverviewRow();
            dominanteRows.add(row);
            dominanteOverviewCard.addChild(row);
        }

        this.campaignSettingsCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.campaignSettingsTitle = new Label("Parametres de la campagne", 0, 0, 260, 22);
        this.campaignSettingsTitle.setFont(new Font("Dialog", Font.BOLD, 15));
        this.campaignSettingsTitle.setColor(TEXT_MAIN);
        this.maxChoicesLabel = new Label("Nombre de choix maximum", 0, 0, 220, 16);
        this.maxChoicesLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.maxChoicesLabel.setColor(TEXT_MUTED);
        this.maxChoicesValue = new Label("0", 0, 0, 100, 22);
        this.maxChoicesValue.setFont(new Font("Dialog", Font.BOLD, 22));
        this.maxChoicesValue.setColor(TEXT_MAIN);
        this.startDateLabel = new Label("Date de debut", 0, 0, 150, 16);
        this.startDateLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.startDateLabel.setColor(TEXT_MUTED);
        this.startDateValue = new Label("-", 0, 0, 120, 22);
        this.startDateValue.setFont(new Font("Dialog", Font.BOLD, 18));
        this.startDateValue.setColor(TEXT_MAIN);
        this.endDateLabel = new Label("Date de fin", 0, 0, 150, 16);
        this.endDateLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.endDateLabel.setColor(TEXT_MUTED);
        this.endDateValue = new Label("-", 0, 0, 120, 22);
        this.endDateValue.setFont(new Font("Dialog", Font.BOLD, 18));
        this.endDateValue.setColor(TEXT_MAIN);
        campaignSettingsCard.addChild(campaignSettingsTitle);
        campaignSettingsCard.addChild(maxChoicesLabel);
        campaignSettingsCard.addChild(maxChoicesValue);
        campaignSettingsCard.addChild(startDateLabel);
        campaignSettingsCard.addChild(startDateValue);
        campaignSettingsCard.addChild(endDateLabel);
        campaignSettingsCard.addChild(endDateValue);

        dashboardContent.addChild(campaignCard);
        dashboardContent.addChild(dominanteKpi);
        dashboardContent.addChild(sessionsKpi);
        dashboardContent.addChild(inscriptionsKpi);
        dashboardContent.addChild(fillRateKpi);
        dashboardContent.addChild(dominanteOverviewCard);
        dashboardContent.addChild(campaignSettingsCard);

        this.dominantesSection = new BaseComp(null);
        this.dominanteDarkMode = true;
        this.nouvelleDominanteButton = new Button("+ Nouvelle dominante", 0, 0, 210, 32,
                this::openCreateDominanteModal);
        this.dominantesScroll = new ScrollView(0, 0, 100, 100);
        this.dominantesList = dominantesScroll.getContent();
        styleDominantesActionButtons();
        dominantesSection.addChild(nouvelleDominanteButton);
        dominantesSection.addChild(dominantesScroll);

        this.sessionsSection = new BaseComp(null);
        this.nouvelleSessionButton = new PrimaryButton("+ Nouvelle session", 0, 0, 170, 32, this::openCreateSessionModal);
        this.sessionsFilterCard = new SurfaceCard(0, 0, 100, 70, PANEL_BG, PANEL_BORDER, 12);
        Label filterTitle = new Label("Filtrer par dominante", 12, 10, 180, 16);
        filterTitle.setFont(new Font("Dialog", Font.BOLD, 12));
        filterTitle.setColor(TEXT_MAIN);
        this.dominanteFilter = new SelectInput(12, 28, 280, 30);
        this.dominanteFilter.setOptions(List.of("Toutes les dominantes"));
        this.dominanteFilter.setSelectedOption("Toutes les dominantes");
        this.dominanteFilter.setOnChange(value -> refreshSessionsView());
        this.sessionsTotalLabel = new Label("Total sessions 0", 0, 0, 150, 16);
        this.sessionsTotalLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        this.sessionsTotalLabel.setColor(TEXT_MUTED);
        sessionsFilterCard.addChild(filterTitle);
        sessionsFilterCard.addChild(dominanteFilter);
        sessionsFilterCard.addChild(sessionsTotalLabel);
        this.sessionsScroll = new ScrollView(0, 0, 100, 100);
        this.sessionsList = sessionsScroll.getContent();
        sessionsSection.addChild(nouvelleSessionButton);
        sessionsSection.addChild(sessionsFilterCard);
        sessionsSection.addChild(sessionsScroll);

        this.campagneSection = new BaseComp(null);
        this.campagneScroll = new ScrollView(0, 0, 100, 100);
        this.campagneContent = campagneScroll.getContent();
        this.campaignFormCard = new SurfaceCard(0, 0, 100, 310, PANEL_BG, PANEL_BORDER, 12);
        Label formTitle = new Label("Informations generales", 16, 12, 260, 20);
        formTitle.setFont(new Font("Dialog", Font.BOLD, 14));
        formTitle.setColor(TEXT_MAIN);
        campaignNameInput = new ReusableLabeledInput("Nom de la campagne", "Campagne Sessions Dominantes 2026", 16, 42, 380,
                62);
        registrationDateInput = new ReusableLabeledInput("Date inscript (dd/MM/yyyy)", "10/03/2026", 16, 108, 200, 62);
        maxChoicesInput = new ReusableLabeledInput("Nombre max de choix", "5", 232, 108, 164, 62);
        startDateInput = new ReusableLabeledInput("Date de debut (yyyy-MM-dd)", "2026-03-10", 16, 174, 200, 62);
        endDateInput = new ReusableLabeledInput("Date de fin (yyyy-MM-dd)", "2026-03-20", 232, 174, 200, 62);

        saveCampaignButton = new PrimaryButton("Enregistrer", 16, 250, 130, 34, this::saveCampaignSettings);
        openCampaignButton = new PrimaryButton("Ouvrir", 158, 250, 90, 34, this::openCampaign);
        closeCampaignButton = new PrimaryButton("Fermer", 258, 250, 90, 34, this::closeCampaign);
        autoAssignButton = new PrimaryButton("Traitement Auto", 358, 250, 140, 34, this::runAutoAssignment);
        campaignFeedbackLabel = new Label("", 16, 288, 420, 16);
        campaignFeedbackLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        campaignFeedbackLabel.setColor(TEXT_MUTED);
        campaignFormCard.addChild(formTitle);
        campaignFormCard.addChild(campaignNameInput);
        campaignFormCard.addChild(registrationDateInput);
        campaignFormCard.addChild(maxChoicesInput);
        campaignFormCard.addChild(startDateInput);
        campaignFormCard.addChild(endDateInput);
        campaignFormCard.addChild(saveCampaignButton);
        campaignFormCard.addChild(openCampaignButton);
        campaignFormCard.addChild(closeCampaignButton);
        campaignFormCard.addChild(autoAssignButton);
        campaignFormCard.addChild(campaignFeedbackLabel);
        campagneContent.addChild(campaignFormCard);
        campagneSection.addChild(campagneScroll);

        this.statsSection = new BaseComp(null);
        this.statsScroll = new ScrollView(0, 0, 100, 100);
        this.statsContent = statsScroll.getContent();
        statsSection.addChild(statsScroll);

        this.statsTotalSessionsKpi = new KpiCard("Sessions totales", "0", "Nombre total", new Color(59, 130, 246));
        this.statsCompleteSessionsKpi = new KpiCard("Sessions completes", "0", "Plein", new Color(34, 197, 94));
        this.statsFillRateKpi = new KpiCard("Taux remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        this.statsUnregisteredKpi = new KpiCard("Non inscrits", "0", "Etudiants", new Color(239, 68, 68));

        statsContent.addChild(statsTotalSessionsKpi);
        statsContent.addChild(statsCompleteSessionsKpi);
        statsContent.addChild(statsFillRateKpi);
        statsContent.addChild(statsUnregisteredKpi);

        this.unregisteredStudentsCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.unregisteredStudentsTitle = new Label("Etudiants non inscrits", 0, 0, 200, 22);
        unregisteredStudentsTitle.setFont(new Font("Dialog", Font.BOLD, 16));
        unregisteredStudentsTitle.setColor(TEXT_MAIN);
        unregisteredStudentsCard.addChild(unregisteredStudentsTitle);

        this.unregisteredScroll = new ScrollView(0, 0, 100, 100);
        this.unregisteredList = unregisteredScroll.getContent();
        unregisteredStudentsCard.addChild(unregisteredScroll);
        statsContent.addChild(unregisteredStudentsCard);

        this.studentPlanningCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.studentPlanningTitle = new Label("Planning de l'etudiant", 0, 0, 250, 22);
        studentPlanningTitle.setFont(new Font("Dialog", Font.BOLD, 16));
        studentPlanningTitle.setColor(TEXT_MAIN);
        studentPlanningCard.addChild(studentPlanningTitle);

        this.selectedStudentLabel = new Label("Aucun selectionne", 0, 0, 300, 20);
        selectedStudentLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        selectedStudentLabel.setColor(TEXT_MUTED);
        studentPlanningCard.addChild(selectedStudentLabel);

        this.planningScroll = new ScrollView(0, 0, 100, 100);
        this.planningList = planningScroll.getContent();

        this.noStudentSelectedLabel = new Label("Selectionnez un etudiant pour voir son planning", 0, 0, 300, 20);
        noStudentSelectedLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        noStudentSelectedLabel.setColor(TEXT_MUTED);
        planningList.addChild(noStudentSelectedLabel);
        studentPlanningCard.addChild(planningScroll);
        statsContent.addChild(studentPlanningCard);

        this.activeSection = Section.DASHBOARD;
        applyDarkTheme();
    }

    @Override
    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);
        content.addChild(sidebar);
        content.addChild(header);
        content.addChild(refreshButton);
        content.addChild(sectionHost);

        refreshAllData();
        renderActiveSection();
        onResize();
    }

    @Override
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

        dashboardSection.setBounds(0, 0, mainW, sectionHost.getHeight());
        dominantesSection.setBounds(0, 0, mainW, sectionHost.getHeight());
        sessionsSection.setBounds(0, 0, mainW, sectionHost.getHeight());
        campagneSection.setBounds(0, 0, mainW, sectionHost.getHeight());
        statsSection.setBounds(0, 0, mainW, sectionHost.getHeight());

        layoutDashboard(mainW);
        layoutDominantes(mainW);
        layoutSessions(mainW);
        layoutCampagne(mainW);
        layoutStats(mainW);
        relayoutActiveSection();

        window.invalidateAll();
        window.requestRenderIfNeeded();
    }

    private void relayoutActiveSection() {
        if (activeSection == Section.DOMINANTES) {
            relayoutDominantesList();
            return;
        }
        if (activeSection == Section.SESSIONS) {
            relayoutSessionsList();
        }
    }

    private void layoutDashboard(int mainW) {
        dashboardScroll.setBounds(0, 0, mainW, sectionHost.getHeight());

        campaignCard.setBounds(0, 0, mainW, 118);
        campaignTitle.setBounds(16, 12, mainW - 220, 28);
        campaignDates.setBounds(16, 44, mainW - 220, 20);
        campaignStatus.setBounds(mainW - 220, 14, 190, 20);
        campaignActionButton.setBounds(16, 78, 200, 30);

        int gap = 12;
        int kpiW = (mainW - gap) / 2;
        int kpiH = 102;
        int kpiStartY = 130;
        dominanteKpi.setBounds(0, kpiStartY, kpiW, kpiH);
        sessionsKpi.setBounds(kpiW + gap, kpiStartY, kpiW, kpiH);
        inscriptionsKpi.setBounds(0, kpiStartY + kpiH + gap, kpiW, kpiH);
        fillRateKpi.setBounds(kpiW + gap, kpiStartY + kpiH + gap, kpiW, kpiH);

        int overviewY = kpiStartY + (kpiH * 2) + (gap * 2);
        dominanteOverviewCard.setBounds(0, overviewY, mainW, 252);
        dominanteOverviewTitle.setBounds(16, 12, mainW - 32, 22);
        dominanteOverviewSubtitle.setBounds(16, 34, mainW - 32, 16);
        int rowY = 58;
        for (DominanteOverviewRow row : dominanteRows) {
            row.setBounds(12, rowY, mainW - 24, 36);
            rowY += 38;
        }

        campaignSettingsCard.setBounds(0, overviewY + 264, mainW, 112);
        campaignSettingsTitle.setBounds(16, 12, mainW - 32, 22);
        int colW = (mainW - 32) / 3;
        maxChoicesLabel.setBounds(16, 42, colW, 16);
        maxChoicesValue.setBounds(16, 58, colW, 24);
        startDateLabel.setBounds(16 + colW, 42, colW, 16);
        startDateValue.setBounds(16 + colW, 58, colW, 24);
        endDateLabel.setBounds(16 + (colW * 2), 42, colW, 16);
        endDateValue.setBounds(16 + (colW * 2), 58, colW, 24);

        int dashboardHeight = overviewY + 264 + 112 + 24;
        dashboardScroll.setContentHeight(Math.max(sectionHost.getHeight(), dashboardHeight));
        dashboardScroll.setContentWidth(mainW);
    }

    private void layoutDominantes(int mainW) {
        nouvelleDominanteButton.setBounds(mainW - 220, 0, 220, 32);
        dominantesScroll.setBounds(0, 44, mainW, Math.max(220, sectionHost.getHeight() - 44));
    }

    private void layoutSessions(int mainW) {
        nouvelleSessionButton.setBounds(mainW - 188, 0, 178, 32);
        sessionsFilterCard.setBounds(0, 44, mainW, 70);
        sessionsTotalLabel.setBounds(mainW - 170, 12, 150, 16);
        sessionsScroll.setBounds(0, 126, mainW, Math.max(220, sectionHost.getHeight() - 126));
    }

    private void relayoutDominantesList() {
        int containerWidth = Math.max(300, dominantesScroll.getWidth());
        int gap = 12;
        int cardW = (containerWidth - gap) / 2;
        int cardH = 220;

        int idx = 0;
        for (BaseComp child : dominantesList.getChildrenList()) {
            if (child instanceof DominanteCardAdmin) {
                int x = (idx % 2) * (cardW + gap);
                int y = (idx / 2) * (cardH + gap);
                child.setBounds(x, y, cardW, cardH);
                idx++;
                continue;
            }
            if (child instanceof Label) {
                child.setBounds(0, 8, Math.max(260, containerWidth - 16), 22);
            }
        }

        int rows = (int) Math.ceil(Math.max(1, idx) / 2.0);
        int contentHeight = rows * (cardH + gap) + 8;
        dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), contentHeight));
        dominantesScroll.setContentWidth(Math.max(dominantesScroll.getWidth(), containerWidth));
    }

    private void relayoutSessionsList() {
        int y = 0;
        int rowWidth = Math.max(120, sessionsScroll.getWidth() - 12);
        int labelWidth = Math.max(120, sessionsScroll.getWidth() - 16);

        for (BaseComp child : sessionsList.getChildrenList()) {
            if (child instanceof SessionRowAdmin) {
                child.setBounds(0, y, rowWidth, 66);
                y += 76;
                continue;
            }
            if (child instanceof Label) {
                child.setBounds(0, y, labelWidth, 22);
                y += 28;
            }
        }

        sessionsScroll.setContentHeight(Math.max(sessionsScroll.getHeight(), y + 10));
        sessionsScroll.setContentWidth(sessionsScroll.getWidth());
    }

    private void layoutCampagne(int mainW) {
        campagneScroll.setBounds(0, 0, mainW, sectionHost.getHeight());
        campaignFormCard.setBounds(0, 0, mainW, 310);
        campaignNameInput.setBounds(16, 42, mainW - 32, 62);
        registrationDateInput.setBounds(16, 108, 220, 62);
        maxChoicesInput.setBounds(252, 108, 180, 62);
        startDateInput.setBounds(16, 174, 220, 62);
        endDateInput.setBounds(252, 174, 180, 62);
        saveCampaignButton.setBounds(16, 250, 130, 34);
        openCampaignButton.setBounds(158, 250, 90, 34);
        closeCampaignButton.setBounds(258, 250, 90, 34);
        autoAssignButton.setBounds(358, 250, 140, 34);
        campaignFeedbackLabel.setBounds(16, 288, mainW - 32, 16);
        campagneScroll.setContentHeight(Math.max(sectionHost.getHeight(), 334));
        campagneScroll.setContentWidth(mainW);
    }

    private void layoutStats(int mainW) {
        int gap = 12;
        int kpiW = (mainW - gap * 3) / 4;
        int kpiH = 100;
        int startY = 12;

        statsScroll.setBounds(0, 0, mainW, sectionHost.getHeight());

        statsTotalSessionsKpi.setBounds(0, startY, kpiW, kpiH);
        statsCompleteSessionsKpi.setBounds(kpiW + gap, startY, kpiW, kpiH);
        statsFillRateKpi.setBounds((kpiW + gap) * 2, startY, kpiW, kpiH);
        statsUnregisteredKpi.setBounds((kpiW + gap) * 3, startY, kpiW, kpiH);

        int cardY = startY + kpiH + gap;
        int cardH = Math.max(200, sectionHost.getHeight() - cardY - 220);
        int halfW = (mainW - gap) / 2;

        unregisteredStudentsCard.setBounds(0, cardY, halfW, cardH);
        unregisteredStudentsTitle.setBounds(16, 12, halfW - 32, 22);
        unregisteredScroll.setBounds(8, 40, halfW - 16, cardH - 48);

        studentPlanningCard.setBounds(halfW + gap, cardY, halfW, cardH);
        studentPlanningTitle.setBounds(16, 12, halfW - 32, 22);
        selectedStudentLabel.setBounds(16, 38, halfW - 32, 20);
        planningScroll.setBounds(8, 64, halfW - 16, cardH - 80);

        int contentHeight = cardY + cardH + 24;
        statsScroll.setContentHeight(Math.max(sectionHost.getHeight(), contentHeight));
        statsScroll.setContentWidth(mainW);
    }

    private void onSidebarSelect(String key) {
        if ("dominantes".equals(key)) {
            activeSection = Section.DOMINANTES;
        } else if ("sessions".equals(key)) {
            activeSection = Section.SESSIONS;
        } else if ("campagne".equals(key)) {
            activeSection = Section.CAMPAGNE;
        } else if ("stats".equals(key)) {
            activeSection = Section.STATS;
        } else {
            activeSection = Section.DASHBOARD;
        }
        renderActiveSection();
        onResize();
    }

    private void renderActiveSection() {
        clearChildren(sectionHost);
        if (activeSection == Section.DOMINANTES) {
            header.setSubtitle("Gerez les domaines d'etudes disponibles");
            headerTitle("Dominantes");
            sectionHost.addChild(dominantesSection);
            refreshDominantesView();
            return;
        }
        if (activeSection == Section.SESSIONS) {
            header.setSubtitle("Gerez les creneaux de presentation");
            headerTitle("Sessions");
            sectionHost.addChild(sessionsSection);
            refreshSessionsView();
            return;
        }
        if (activeSection == Section.CAMPAGNE) {
            header.setSubtitle("Configurez les parametres generaux de la campagne d'inscriptions");
            headerTitle("Parametrage de la campagne");
            sectionHost.addChild(campagneSection);
            refreshCampaignForm();
            return;
        }
        if (activeSection == Section.STATS) {
            header.setSubtitle("Statistiques et analyse des inscriptions");
            headerTitle("Statistiques");
            sectionHost.addChild(statsSection);
            refreshStatsView();
            return;
        }

        header.setSubtitle("Vue d'ensemble de la campagne en cours");
        headerTitle("Tableau de bord");
        sectionHost.addChild(dashboardSection);
        refreshDashboardView();
    }

    private void refreshAllData() {
        activeCampaign = resolveActiveCampaign();
    }

    private void invalidateDataCaches() {
        dominantesCacheDirty = true;
        sessionsCacheDirty = true;
        allocationsCacheDirty = true;
    }

    private ArrayList<Dominante> getDominantes(boolean forceReload) {
        if (forceReload || dominantesCacheDirty) {
            cachedDominantes = dominanteService.listAll();
            dominantesCacheDirty = false;
        }
        return cachedDominantes;
    }

    private ArrayList<SessionSlot> getSessions(boolean forceReload) {
        int campaignId = activeCampaign == null ? -1 : activeCampaign.getId();
        if (forceReload || sessionsCacheDirty || cachedSessionsCampaignId != campaignId) {
            cachedSessionsCampaignId = campaignId;
            cachedSessions = campaignId <= 0 ? new ArrayList<>() : new ArrayList<>(sessionService.listByCampaign(campaignId));
            sessionsCacheDirty = false;
        }
        return cachedSessions;
    }

    private Map<Integer, Integer> getAllocations(boolean forceReload) {
        int campaignId = activeCampaign == null ? -1 : activeCampaign.getId();
        if (forceReload || allocationsCacheDirty || cachedAllocationsCampaignId != campaignId) {
            cachedAllocationsCampaignId = campaignId;
            cachedAllocationsBySession = campaignId <= 0 ? new HashMap<>()
                    : sessionService.countAllocationsBySessionForCampaign(campaignId);
            allocationsCacheDirty = false;
        }
        return cachedAllocationsBySession;
    }

    private void refreshCurrentSection() {
        invalidateDataCaches();
        switch (activeSection) {
            case DOMINANTES -> refreshDominantesView();
            case SESSIONS -> refreshSessionsView();
            case CAMPAGNE -> refreshCampaignForm();
            case STATS -> refreshStatsView();
            case DASHBOARD -> refreshDashboardView();
        }
        window.requestRenderIfNeeded();
    }

    private void refreshDashboardView() {
        activeCampaign = resolveActiveCampaign();
        ArrayList<Dominante> dominantes = getDominantes(false);
        int dominanteCount = dominantes.size();
        if (activeCampaign == null) {
            campaignTitle.setText("Aucune campagne active");
            campaignDates.setText("Creez une campagne pour commencer");
            campaignStatus.setText("Statut: -");
            campaignActionButton.setText("Creer campagne");
            dominanteKpi.setValue(String.valueOf(dominanteCount));
            sessionsKpi.setValue("0");
            inscriptionsKpi.setValue("0");
            inscriptionsKpi.setSubtitle("Sur 0 places");
            fillRateKpi.setValue("0%");
            fillRateKpi.setSubtitle("Taux global");
            hideDominanteRows();
            maxChoicesValue.setText("0");
            startDateValue.setText("-");
            endDateValue.setText("-");
            return;
        }

        ArrayList<SessionSlot> sessions = getSessions(false);
        Map<Integer, Integer> allocationsBySession = getAllocations(false);
        ArrayList<DominanteStat> stats = computeDominanteStats(dominantes, sessions, allocationsBySession);
        int totalAllocated = 0;
        int totalCapacity = 0;
        for (DominanteStat stat : stats) {
            totalAllocated += stat.allocated;
            totalCapacity += stat.capacity;
        }

        campaignTitle.setText(safe(activeCampaign.getName()));
        campaignDates.setText("Du " + safe(activeCampaign.getRegistrationDay()) + " | Promo " + safe(activeCampaign.getPromo()));
        campaignStatus.setText(statusLabel(activeCampaign.getStatus()));
        campaignActionButton.setText(buttonTextForStatus(activeCampaign.getStatus()));

        dominanteKpi.setValue(String.valueOf(dominanteCount));
        sessionsKpi.setValue(String.valueOf(sessions.size()));
        inscriptionsKpi.setValue(String.valueOf(totalAllocated));
        inscriptionsKpi.setSubtitle("Sur " + totalCapacity + " places");
        int fillRate = totalCapacity <= 0 ? 0 : (int) Math.round((totalAllocated * 100.0) / totalCapacity);
        fillRateKpi.setValue(fillRate + "%");

        applyDominanteRows(stats);

        maxChoicesValue.setText(String.valueOf(activeCampaign.getMaxChoices()));
        startDateValue.setText(safe(activeCampaign.getStartDate()));
        endDateValue.setText(safe(activeCampaign.getEndDate()));
    }

    private void refreshDominantesView() {
        activeCampaign = resolveActiveCampaign();
        clearChildren(dominantesList);

        ArrayList<Dominante> dominantes = getDominantes(false);
        ArrayList<SessionSlot> sessions = getSessions(false);
        Map<Integer, Integer> allocationsBySession = getAllocations(false);
        ArrayList<DominanteStat> stats = computeDominanteStats(dominantes, sessions, allocationsBySession);
        Map<Integer, DominanteStat> byId = new HashMap<>();
        for (DominanteStat stat : stats) {
            byId.put(Integer.valueOf(stat.dominante.getId()), stat);
        }

        int containerWidth = Math.max(300, dominantesScroll.getWidth());
        int gap = 12;
        int cardW = (containerWidth - gap) / 2;
        int cardH = 220;

        int idx = 0;
        for (Dominante d : dominantes) {
            DominanteStat stat = byId.get(Integer.valueOf(d.getId()));
            int sessionsCount = stat == null ? 0 : stat.sessionCount;
            int capacity = stat == null ? 0 : stat.capacity;
            int allocated = stat == null ? 0 : stat.allocated;
            int fillRate = capacity <= 0 ? 0 : (int) Math.round((allocated * 100.0) / capacity);

            final Dominante current = d;
            DominanteCardAdmin card = new DominanteCardAdmin(
                    () -> openEditDominanteModal(current),
                    () -> confirmDeleteDominante(current));
            
            card.getEventManager().register(event.UiEvent.Type.POINTER_UP, (c, e) -> {
                if (e.getTarget() == card || e.getTarget() == card.getChildrenList().get(2) || e.getTarget() == card.getChildrenList().get(3)) {
                     openPreviewDominanteModal(current, sessionsCount, capacity, allocated);
                }
            });

                card.setDarkMode(dominanteDarkMode);
            int x = (idx % 2) * (cardW + gap);
            int y = (idx / 2) * (cardH + gap);
            card.setBounds(x, y, cardW, cardH);
            card.setData(d.getCode(), d.getName(), d.getDescription(), sessionsCount, capacity, allocated, fillRate,
                    accentForCode(d.getCode()));
            dominantesList.addChild(card);
            idx++;
        }

        if (dominantes.isEmpty()) {
            Label empty = new Label("Aucune dominante. Utilisez le bouton + Nouvelle dominante.", 0, 8,
                    Math.max(260, dominantesScroll.getWidth() - 16), 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            empty.setColor(dominanteDarkMode ? TEXT_MUTED : new Color(96, 109, 130));
            dominantesList.addChild(empty);
        }

        int rows = (int) Math.ceil(Math.max(1, dominantes.size()) / 2.0);
        int contentHeight = rows * (cardH + gap) + 8;
        dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), contentHeight));
        dominantesScroll.setContentWidth(Math.max(dominantesScroll.getWidth(), containerWidth));
    }

    private void refreshSessionsView() {
        activeCampaign = resolveActiveCampaign();
        clearChildren(sessionsList);

        if (activeCampaign == null) {
            sessionsTotalLabel.setText("Total sessions 0");
            sessionsScroll.setContentHeight(sessionsScroll.getHeight());
            return;
        }

        ArrayList<Dominante> dominantes = getDominantes(false);
        ArrayList<String> options = new ArrayList<>();
        options.add("Toutes les dominantes");
        for (Dominante dominante : dominantes) {
            options.add(dominante.getName());
        }
        dominanteFilter.setOptions(options);
        String selected = dominanteFilter.getSelectedOption();

        ArrayList<SessionSlot> sessions = getSessions(false);
        Map<Integer, Integer> allocationsBySession = getAllocations(false);
        Map<Integer, Dominante> dominantById = new HashMap<>();
        for (Dominante d : dominantes) {
            dominantById.put(Integer.valueOf(d.getId()), d);
        }

        int y = 0;
        String lastDate = null;
        int visibleCount = 0;
        for (SessionSlot session : sessions) {
            Dominante dominante = dominantById.get(Integer.valueOf(session.getDominanteId()));
            String dominanteName = dominante == null ? ("Dominante #" + session.getDominanteId()) : dominante.getName();
            if (!"Toutes les dominantes".equals(selected) && !selected.equals(dominanteName)) {
                continue;
            }

            if (!safe(session.getSessionDate()).equals(lastDate)) {
                Label dateLabel = new Label("🗓  " + safe(session.getSessionDate()), 0, y, sessionsScroll.getWidth() - 16, 22);
                dateLabel.setFont(new Font("Dialog", Font.BOLD, 14));
                dateLabel.setColor(TEXT_MAIN);
                sessionsList.addChild(dateLabel);
                y += 28;
                lastDate = safe(session.getSessionDate());
            }

            final SessionSlot current = session;
            SessionRowAdmin row = new SessionRowAdmin(
                    () -> openEditSessionModal(current),
                    () -> deleteSession(current));
            row.setBounds(0, y, sessionsScroll.getWidth() - 12, 66);
            int allocated = allocationsBySession.getOrDefault(Integer.valueOf(session.getId()), 0);
            int fillRate = session.getCapacity() <= 0 ? 0 : (int) Math.round((allocated * 100.0) / session.getCapacity());
            String room = safe(session.getRoom());
            String details = formatMinute(session.getStartMinute()) + " - " + formatMinute(session.getEndMinute())
                    + "  |  " + room;
            row.setData(dominanteName + " - " + safe(session.getTitle()), details, fillRate,
                    accentForCode(dominante == null ? "--" : dominante.getCode()));
            row.setOnManage(() -> openManageSessionModal(current, allocated));
            sessionsList.addChild(row);
            y += 76;
            visibleCount++;
        }

        sessionsTotalLabel.setText("Total sessions " + visibleCount);
        sessionsScroll.setContentHeight(Math.max(sessionsScroll.getHeight(), y + 10));
        sessionsScroll.setContentWidth(sessionsScroll.getWidth());
    }

    private void refreshCampaignForm() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) {
            campaignNameInput.setValue("");
            registrationDateInput.setValue("");
            maxChoicesInput.setValue("5");
            startDateInput.setValue("");
            endDateInput.setValue("");
            campaignFeedbackLabel.setText("Aucune campagne active.");
            return;
        }

        campaignNameInput.setValue(safe(activeCampaign.getName()));
        registrationDateInput.setValue(safe(activeCampaign.getRegistrationDay()));
        maxChoicesInput.setValue(String.valueOf(activeCampaign.getMaxChoices()));
        startDateInput.setValue(safe(activeCampaign.getStartDate()));
        endDateInput.setValue(safe(activeCampaign.getEndDate()));
        campaignFeedbackLabel.setText("Statut actuel: " + safe(activeCampaign.getStatus()));
    }

    private void refreshStatsView() {
        activeCampaign = resolveActiveCampaign();
        
        if (activeCampaign == null || user.getPromo() == null) {
            statsTotalSessionsKpi.setValue("0");
            statsCompleteSessionsKpi.setValue("0");
            statsFillRateKpi.setValue("0%");
            statsUnregisteredKpi.setValue("0");
            clearChildren(unregisteredList);
            clearChildren(planningList);
            planningList.addChild(noStudentSelectedLabel);
            return;
        }

        StatisticsService.StatsSummary stats = statisticsService.getStatsForCampaign(
                activeCampaign.getId(), user.getPromo());

        statsTotalSessionsKpi.setValue(String.valueOf(stats.totalSessions));
        statsCompleteSessionsKpi.setValue(String.valueOf(stats.completeSessions));
        statsFillRateKpi.setValue(String.format("%.1f%%", stats.averageFillRate));
        statsUnregisteredKpi.setValue(String.valueOf(stats.unregisteredStudents));

        refreshUnregisteredStudentsList();
        
        if (selectedStudentForPlanning != null) {
            refreshStudentPlanning(selectedStudentForPlanning);
        } else {
            clearChildren(planningList);
            planningList.addChild(noStudentSelectedLabel);
        }
    }

    private void refreshUnregisteredStudentsList() {
        clearChildren(unregisteredList);
        
        List<User> unregistered = statisticsService.getUnregisteredStudents(
                activeCampaign.getId(), user.getPromo());
        
        int y = 0;
        for (User student : unregistered) {
            final User currentStudent = student;
            Button studentButton = new Button(
                    safe(student.getFullName()), 
                    0, y, unregisteredScroll.getWidth() - 8, 36,
                    () -> selectStudentForPlanning(currentStudent));
            studentButton.setBackground(new Color(40, 50, 70));
            studentButton.setForeground(TEXT_MAIN);
            studentButton.setFont(new Font("Dialog", Font.PLAIN, 13));
            unregisteredList.addChild(studentButton);
            y += 40;
        }

        if (unregistered.isEmpty()) {
            Label emptyLabel = new Label("Tous les etudiants sont inscrits", 0, 0, 200, 24);
            emptyLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
            emptyLabel.setColor(TEXT_MUTED);
            unregisteredList.addChild(emptyLabel);
        }

        unregisteredScroll.setContentHeight(Math.max(unregisteredScroll.getHeight(), y + 10));
    }

    private void selectStudentForPlanning(User student) {
        selectedStudentForPlanning = student;
        refreshStudentPlanning(student);
    }

    private void refreshStudentPlanning(User student) {
        if (student == null) {
            clearChildren(planningList);
            planningList.addChild(noStudentSelectedLabel);
            return;
        }

        selectedStudentLabel.setText("Planning de: " + safe(student.getFullName()));
        
        StatisticsService.StudentWithSessions data = statisticsService.getStudentSessions(
                activeCampaign.getId(), student.getId());
        
        clearChildren(planningList);
        
        if (data.sessions == null || data.sessions.isEmpty()) {
            Label noSessionsLabel = new Label("Aucune session assignee", 0, 0, 250, 24);
            noSessionsLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
            noSessionsLabel.setColor(TEXT_MUTED);
            planningList.addChild(noSessionsLabel);
            planningScroll.setContentHeight(40);
            return;
        }

        int y = 0;
        for (StatisticsService.SessionInfo session : data.sessions) {
            SurfaceCard sessionCard = new SurfaceCard(0, y, planningScroll.getWidth() - 8, 80, 
                    new Color(30, 40, 55), PANEL_BORDER, 8);
            
            Label domLabel = new Label(session.dominanteName, 8, 8, 200, 18);
            domLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            domLabel.setColor(new Color(96, 165, 250));
            
            Label titleLabel = new Label(session.title, 8, 28, 250, 16);
            titleLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            titleLabel.setColor(TEXT_MAIN);
            
            Label detailsLabel = new Label(
                    session.date + " | " + session.startTime + "-" + session.endTime + " | " + session.room,
                    8, 48, 250, 16);
            detailsLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
            detailsLabel.setColor(TEXT_MUTED);
            
            sessionCard.addChild(domLabel);
            sessionCard.addChild(titleLabel);
            sessionCard.addChild(detailsLabel);
            planningList.addChild(sessionCard);
            
            y += 88;
        }

        planningScroll.setContentHeight(Math.max(planningScroll.getHeight(), y + 10));
    }

    private void openCreateDominanteModal() {
        FormModal modal = new FormModal(520, 360, "Nouvelle dominante", this::closeTopLayer);

        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom de la dominante", "ex: Intelligence Artificielle", 18, 12,
                484, 62);
        TextAreaInput descInput = new TextAreaInput(18, 82, 484, 110);
        descInput.setPlaceholder("Description");

        ColorPicker colorPicker = new ColorPicker(18, 202, 240, 80, null);

        Button cancel = new Button("Annuler", 292, 304, 96, 34, this::closeTopLayer);
        cancel.setBackground(new Color(200, 204, 214));

        Button create = new Button("Creer", 398, 304, 104, 34, () -> {
            String name = nameInput.getValue();
            if (name == null || name.isBlank()) {
                return;
            }
            Dominante d = new Dominante();
            d.setCode(buildCode(name));
            d.setName(name.trim());
            d.setDescription(descInput.getText());
            d.setColor(colorPicker.getSelectedColor());
            d.setResponsibleName(resolveDisplayName(user));
            d.setActive(true);
            ServiceResult result = dominanteService.create(d);
            System.out.println("[AdminDominante] " + result.getMessage());
            if (result.isSuccess()) {
                invalidateDataCaches();
                closeTopLayer();
                refreshDominantesView();
                refreshDashboardView();
                onResize();
            }
        });
        create.setBackground(new Color(12, 16, 44));

        modal.getBody().addChild(nameInput);
        modal.getBody().addChild(descInput);
        modal.getBody().addChild(colorPicker);
        modal.getBody().addChild(cancel);
        modal.getBody().addChild(create);

        window.openModal(modal);
    }

    private void openEditDominanteModal(Dominante dominante) {
        if (dominante == null) {
            return;
        }
        FormModal modal = new FormModal(520, 360, "Modifier dominante", this::closeTopLayer);

        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom", safe(dominante.getName()), 18, 12, 484, 62);
        nameInput.setValue(safe(dominante.getName()));

        TextAreaInput descInput = new TextAreaInput(18, 82, 484, 110);
        descInput.setText(safe(dominante.getDescription()));

        ColorPicker colorPicker = new ColorPicker(18, 202, 240, 80, dominante.getColor());

        Button cancel = new Button("Annuler", 292, 304, 96, 34, this::closeTopLayer);
        cancel.setBackground(new Color(200, 204, 214));

        Button save = new Button("Sauver", 398, 304, 104, 34, () -> {
            dominante.setName(nameInput.getValue());
            dominante.setDescription(descInput.getText());
            dominante.setColor(colorPicker.getSelectedColor());
            dominante.setCode(buildCode(nameInput.getValue()));
            ServiceResult result = dominanteService.update(dominante);
            System.out.println("[AdminDominante] " + result.getMessage());
            if (result.isSuccess()) {
                invalidateDataCaches();
                closeTopLayer();
                refreshDominantesView();
                refreshDashboardView();
                onResize();
            }
        });
        save.setBackground(new Color(12, 16, 44));

        modal.getBody().addChild(nameInput);
        modal.getBody().addChild(descInput);
        modal.getBody().addChild(colorPicker);
        modal.getBody().addChild(cancel);
        modal.getBody().addChild(save);

        window.openModal(modal);
    }

    private void openPreviewDominanteModal(Dominante dominante, int sessions, int capacity, int allocated) {
        if (dominante == null) {
            return;
        }
        FormModal modal = new FormModal(480, 240, "Details Dominante", this::closeTopLayer);

        Label codeLabel = new Label("Code: " + safe(dominante.getCode()), 18, 12, 440, 24);
        codeLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        codeLabel.setColor(new Color(66, 133, 244));

        Label nameLabel = new Label("Nom: " + safe(dominante.getName()), 18, 40, 440, 24);
        nameLabel.setFont(new Font("Dialog", Font.BOLD, 14));

        Label descLabel = new Label(safe(dominante.getDescription()), 18, 70, 440, 60);
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 13));

        Label statsLabel = new Label(String.format("Sessions: %d | Capacite: %d | Inscrits: %d", sessions, capacity, allocated), 18, 140, 440, 24);
        statsLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        statsLabel.setColor(new Color(110, 120, 137));

        Button close = new Button("Fermer", 360, 180, 100, 34, this::closeTopLayer);
        close.setBackground(new Color(230, 235, 243));

        modal.getBody().addChild(codeLabel);
        modal.getBody().addChild(nameLabel);
        modal.getBody().addChild(descLabel);
        modal.getBody().addChild(statsLabel);
        modal.getBody().addChild(close);

        window.openModal(modal);
    }

    private void confirmDeleteDominante(Dominante dominante) {
        if (dominante == null) {
            return;
        }
        ConfirmDialog dialog = new ConfirmDialog(420, 220, "Supprimer dominante",
                "Supprimer " + safe(dominante.getName()) + " ?",
                () -> {
                    ServiceResult result = dominanteService.deleteById(dominante.getId());
                    System.out.println("[AdminDominante] " + result.getMessage());
                    if (result.isSuccess()) {
                        invalidateDataCaches();
                        closeTopLayer();
                        refreshDominantesView();
                        refreshDashboardView();
                        onResize();
                    }
                },
                this::closeTopLayer);
        window.openModal(dialog);
    }

    private void openCreateSessionModal() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) {
            return;
        }
        FormModal modal = new FormModal(560, 380, "Nouvelle session", this::closeTopLayer);

        ReusableLabeledInput titleInput = new ReusableLabeledInput("Titre", "Introduction IA", 24, 24, 512, 62);
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date (dd/MM/yyyy)", "19/03/2026", 24, 102, 160, 62);
        ReusableLabeledInput startInput = new ReusableLabeledInput("Debut (HH:mm)", "09:00", 200, 102, 160, 62);
        ReusableLabeledInput endInput = new ReusableLabeledInput("Fin (HH:mm)", "09:30", 376, 102, 160, 62);
        ReusableLabeledInput roomInput = new ReusableLabeledInput("Salle", "Amphi A", 24, 180, 160, 62);
        ReusableLabeledInput capInput = new ReusableLabeledInput("Capacite", "30", 200, 180, 160, 62);

        Label domLabel = new Label("Dominante", 376, 180, 160, 20);
        domLabel.setFont(new Font("Inter", Font.BOLD, 12));
        domLabel.setColor(new Color(113, 113, 122));

        SelectInput dominanteSelect = new SelectInput(376, 204, 160, 38);
        ArrayList<Dominante> dominantes = getDominantes(true);
        ArrayList<String> domNames = new ArrayList<>();
        for (Dominante d : dominantes) {
            domNames.add(d.getName());
        }
        if (domNames.isEmpty()) {
            domNames.add("Aucune dominante");
        }
        dominanteSelect.setOptions(domNames);
        dominanteSelect.setSelectedOption(domNames.get(0));

        Button cancel = new Button("Annuler", 320, 266, 100, 36, this::closeTopLayer);
        cancel.setBackground(new Color(228, 228, 231));
        cancel.setForeground(new Color(39, 39, 42));

        Button create = new Button("Creer", 436, 266, 100, 36, () -> {
            if (dominantes.isEmpty()) {
                return;
            }
            SessionSlot session = new SessionSlot();
            session.setCampaignId(activeCampaign.getId());
            session.setDominanteId(resolveDominanteId(dominantes, dominanteSelect.getSelectedOption()));
            session.setTitle(titleInput.getValue());
            session.setRoom(roomInput.getValue());
            session.setSessionDate(dateInput.getValue());
            session.setCapacity(parseInt(capInput.getValue(), 20));
            session.setStartMinute(parseTimeToMinute(startInput.getValue()));
            session.setEndMinute(parseTimeToMinute(endInput.getValue()));
            session.setCreatedBy(user.getId());
            ServiceResult result = sessionService.createSession(session);
            System.out.println("[AdminSession] " + result.getMessage());
            if (result.isSuccess()) {
                invalidateDataCaches();
                closeTopLayer();
                refreshSessionsView();
                refreshDashboardView();
                onResize();
            }
        });
        create.setBackground(new Color(12, 16, 44));

        modal.getBody().addChild(titleInput);
        modal.getBody().addChild(roomInput);
        modal.getBody().addChild(dateInput);
        modal.getBody().addChild(capInput);
        modal.getBody().addChild(startInput);
        modal.getBody().addChild(endInput);
        modal.getBody().addChild(domLabel);
        modal.getBody().addChild(dominanteSelect);
        modal.getBody().addChild(cancel);
        modal.getBody().addChild(create);

        window.openModal(modal);
    }

    private void openEditSessionModal(SessionSlot existing) {
        if (existing == null) {
            return;
        }
        FormModal modal = new FormModal(560, 380, "Modifier session", this::closeTopLayer);

        ReusableLabeledInput titleInput = new ReusableLabeledInput("Titre", safe(existing.getTitle()), 24, 24, 512, 62);
        titleInput.setValue(safe(existing.getTitle()));
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date (dd/MM/yyyy)", safe(existing.getSessionDate()), 24, 102, 160, 62);
        dateInput.setValue(safe(existing.getSessionDate()));
        ReusableLabeledInput startInput = new ReusableLabeledInput("Debut (HH:mm)", formatMinute(existing.getStartMinute()), 200, 102, 160, 62);
        ReusableLabeledInput endInput = new ReusableLabeledInput("Fin (HH:mm)", formatMinute(existing.getEndMinute()), 376, 102, 160, 62);
        ReusableLabeledInput roomInput = new ReusableLabeledInput("Salle", safe(existing.getRoom()), 24, 180, 160, 62);
        roomInput.setValue(safe(existing.getRoom()));
        ReusableLabeledInput capInput = new ReusableLabeledInput("Capacite", String.valueOf(existing.getCapacity()), 200, 180, 160, 62);
        
        Label domLabel = new Label("Dominante", 376, 180, 160, 20);
        domLabel.setFont(new Font("Inter", Font.BOLD, 12));
        domLabel.setColor(new Color(113, 113, 122));

        SelectInput dominanteSelect = new SelectInput(376, 204, 160, 38);
        ArrayList<Dominante> dominantes = getDominantes(true);
        ArrayList<String> domNames = new ArrayList<>();
        String selectedName = null;
        for (Dominante d : dominantes) {
            domNames.add(d.getName());
            if (d.getId() == existing.getDominanteId()) {
                selectedName = d.getName();
            }
        }
        if (domNames.isEmpty()) {
            domNames.add("Aucune dominante");
        }
        dominanteSelect.setOptions(domNames);
        dominanteSelect.setSelectedOption(selectedName == null ? domNames.get(0) : selectedName);

        Button cancel = new Button("Annuler", 320, 266, 100, 36, this::closeTopLayer);
        cancel.setBackground(new Color(228, 228, 231));
        cancel.setForeground(new Color(39, 39, 42));

        Button save = new Button("Sauver", 436, 266, 100, 36, () -> {
            existing.setDominanteId(resolveDominanteId(dominantes, dominanteSelect.getSelectedOption()));
            existing.setTitle(titleInput.getValue());
            existing.setRoom(roomInput.getValue());
            existing.setSessionDate(dateInput.getValue());
            existing.setCapacity(parseInt(capInput.getValue(), existing.getCapacity()));
            existing.setStartMinute(parseTimeToMinute(startInput.getValue()));
            existing.setEndMinute(parseTimeToMinute(endInput.getValue()));
            ServiceResult result = sessionService.updateSession(existing);
            System.out.println("[AdminSession] " + result.getMessage());
            if (result.isSuccess()) {
                invalidateDataCaches();
                closeTopLayer();
                refreshSessionsView();
                refreshDashboardView();
                onResize();
            }
        });
        save.setBackground(new Color(12, 16, 44));

        modal.getBody().addChild(titleInput);
        modal.getBody().addChild(roomInput);
        modal.getBody().addChild(dateInput);
        modal.getBody().addChild(capInput);
        modal.getBody().addChild(startInput);
        modal.getBody().addChild(endInput);
        modal.getBody().addChild(domLabel);
        modal.getBody().addChild(dominanteSelect);
        modal.getBody().addChild(cancel);
        modal.getBody().addChild(save);

        window.openModal(modal);
    }

    private void deleteSession(SessionSlot session) {
        if (session == null) {
            return;
        }
        ConfirmDialog dialog = new ConfirmDialog(420, 220, "Supprimer session", "Supprimer cette session ?",
                () -> {
                    ServiceResult result = sessionService.deleteSession(session.getId());
                    System.out.println("[AdminSession] " + result.getMessage());
                    if (result.isSuccess()) {
                        invalidateDataCaches();
                    }
                    closeTopLayer();
                    refreshSessionsView();
                    refreshDashboardView();
                    onResize();
                },
                this::closeTopLayer);
        window.openModal(dialog);
    }

    private void openManageSessionModal(SessionSlot session, int currentAllocated) {
        if (session == null || activeCampaign == null) {
            return;
        }
        FormModal modal = new FormModal(600, 480, "Gerer inscriptions", this::closeTopLayer);

        Label sessionInfoLabel = new Label(session.getTitle(), 18, 12, 560, 24);
        sessionInfoLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        sessionInfoLabel.setColor(TEXT_MAIN);

        Label capacityLabel = new Label("Capacite: " + session.getCapacity() + " | Inscrits: " + currentAllocated, 18, 40, 300, 20);
        capacityLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        capacityLabel.setColor(TEXT_MUTED);

        Label studentListTitle = new Label("Etudiants non inscrits", 18, 70, 250, 20);
        studentListTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        studentListTitle.setColor(TEXT_MAIN);

        ScrollView studentScroll = new ScrollView(18, 94, 260, 320);
        BaseComp studentList = studentScroll.getContent();
        
        Label planningTitle = new Label("Planning de l'etudiant", 322, 70, 250, 20);
        planningTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        planningTitle.setColor(TEXT_MAIN);

        ScrollView planningScroll = new ScrollView(322, 94, 260, 320);
        BaseComp planningList = planningScroll.getContent();
        
        Label noStudentLabel = new Label("Selectionnez un etudiant", 0, 0, 240, 20);
        noStudentLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        noStudentLabel.setColor(TEXT_MUTED);
        planningList.addChild(noStudentLabel);

        modal.getBody().addChild(sessionInfoLabel);
        modal.getBody().addChild(capacityLabel);
        modal.getBody().addChild(studentListTitle);
        modal.getBody().addChild(studentScroll);
        modal.getBody().addChild(planningTitle);
        modal.getBody().addChild(planningScroll);

        List<User> allStudents = registrationService.getStudentsByPromo(activeCampaign.getPromo());
        
        int y = 0;

        for (User student : allStudents) {
            List<Registration> regs = registrationService.getStudentRegistrations(activeCampaign.getId(), student.getId());
            boolean isRegistered = false;
            for (Registration r : regs) {
                if (r.getSessionId() == session.getId() && "ALLOCATED".equals(r.getStatus())) {
                    isRegistered = true;
                    break;
                }
            }
            
            final User currentStudent = student;
            Button studentBtn = new Button(safe(student.getFullName()), 0, y, 240, 36, () -> {
                refreshStudentPlanningInModal(currentStudent, activeCampaign.getId(), planningList, noStudentLabel);
            });
            studentBtn.setBackground(isRegistered ? new Color(34, 197, 94) : new Color(40, 50, 70));
            studentBtn.setForeground(TEXT_MAIN);
            studentBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
            studentList.addChild(studentBtn);
            y += 40;
        }

        studentScroll.setContentHeight(Math.max(studentScroll.getHeight(), y + 10));
        
        Button closeBtn = new Button("Fermer", 480, 424, 100, 34, this::closeTopLayer);
        closeBtn.setBackground(new Color(100, 116, 139));
        modal.getBody().addChild(closeBtn);

        window.openModal(modal);
    }

    private void refreshStudentPlanningInModal(User student, int campaignId, BaseComp planningList, Label noStudentLabel) {
        if (student == null) {
            clearChildren(planningList);
            planningList.addChild(noStudentLabel);
            return;
        }
        
        clearChildren(planningList);
        
        StatisticsService.StudentWithSessions data = statisticsService.getStudentSessions(campaignId, student.getId());
        
        if (data.sessions == null || data.sessions.isEmpty()) {
            Label emptyLabel = new Label("Aucun cours", 0, 0, 240, 20);
            emptyLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            emptyLabel.setColor(TEXT_MUTED);
            planningList.addChild(emptyLabel);
            return;
        }
        
        int y = 0;
        for (StatisticsService.SessionInfo s : data.sessions) {
            Label sessionLabel = new Label(s.dominanteName + " - " + s.date + " " + s.startTime, 0, y, 240, 32);
            sessionLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
            sessionLabel.setColor(TEXT_MAIN);
            planningList.addChild(sessionLabel);
            y += 36;
        }
    }

    private void saveCampaignSettings() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) {
            campaignFeedbackLabel.setColor(new java.awt.Color(239, 68, 68));
            campaignFeedbackLabel.setText("Aucune campagne active.");
            window.requestRenderIfNeeded();
            return;
        }
        
        campaignFeedbackLabel.setColor(new java.awt.Color(66, 133, 244));
        campaignFeedbackLabel.setText("Enregistrement en cours...");
        window.requestRenderIfNeeded();

        final int id = activeCampaign.getId();
        final String nameStr = campaignNameInput.getValue();
        final String regDate = registrationDateInput.getValue();
        final String start = startDateInput.getValue();
        final String endStr = endDateInput.getValue();
        final int maxChoices = parseInt(maxChoicesInput.getValue(), activeCampaign.getMaxChoices());

        Thread t = new Thread(() -> {
            boolean ok = campaignService.updateSettings(id, nameStr, regDate, start, endStr, maxChoices);
            javax.swing.SwingUtilities.invokeLater(() -> {
                campaignFeedbackLabel.setColor(ok ? new java.awt.Color(34, 197, 94) : new java.awt.Color(239, 68, 68));
                campaignFeedbackLabel.setText(ok ? "Parametres enregistres." : "Echec enregistrement.");
                refreshDashboardView();
                window.requestRenderIfNeeded();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void openCampaign() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) return;
        
        campaignFeedbackLabel.setColor(new java.awt.Color(66, 133, 244));
        campaignFeedbackLabel.setText("Ouverture en cours...");
        window.requestRenderIfNeeded();
        final int id = activeCampaign.getId();
        
        Thread t = new Thread(() -> {
            ServiceResult result = campaignService.changeStatus(id, "OPEN");
            javax.swing.SwingUtilities.invokeLater(() -> {
                campaignFeedbackLabel.setColor(result.isSuccess() ? new java.awt.Color(34, 197, 94) : new java.awt.Color(239, 68, 68));
                campaignFeedbackLabel.setText(result.getMessage());
                refreshAllData();
                refreshDashboardView();
                refreshCampaignForm();
                window.requestRenderIfNeeded();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void closeCampaign() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) return;
        
        campaignFeedbackLabel.setColor(new java.awt.Color(66, 133, 244));
        campaignFeedbackLabel.setText("Fermeture en cours...");
        window.requestRenderIfNeeded();
        final int id = activeCampaign.getId();
        
        Thread t = new Thread(() -> {
            ServiceResult result = campaignService.changeStatus(id, "CLOSED");
            javax.swing.SwingUtilities.invokeLater(() -> {
                campaignFeedbackLabel.setColor(result.isSuccess() ? new java.awt.Color(34, 197, 94) : new java.awt.Color(239, 68, 68));
                campaignFeedbackLabel.setText(result.getMessage());
                refreshAllData();
                refreshDashboardView();
                refreshCampaignForm();
                window.requestRenderIfNeeded();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void openCreateCampaignModal() {
        FormModal modal = new FormModal(560, 420, "Nouvelle campagne", this::closeTopLayer);

        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom", "Campagne Dominantes 2026", 18, 12, 524, 62);
        ReusableLabeledInput promoInput = new ReusableLabeledInput("Promo", user.getPromo() == null ? "ING3" : user.getPromo(), 18,
                78, 170, 62);
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date inscript (dd/MM/yyyy)", "10/03/2026", 198, 78, 170, 62);
        ReusableLabeledInput maxInput = new ReusableLabeledInput("Max choix", "5", 378, 78, 164, 62);
        ReusableLabeledInput startDateInput = new ReusableLabeledInput("Date de debut (yyyy-MM-dd)", "2026-03-10", 18, 144, 250, 62);
        ReusableLabeledInput endDateInput = new ReusableLabeledInput("Date de fin (yyyy-MM-dd)", "2026-03-20", 278, 144, 250, 62);

        Button cancel = new Button("Annuler", 332, 364, 96, 34, this::closeTopLayer);
        cancel.setBackground(new Color(200, 204, 214));

        Button create = new Button("Creer", 438, 364, 104, 34, () -> {
            model.Campaign c = new model.Campaign();
            c.setName(nameInput.getValue());
            c.setPromo(promoInput.getValue());
            c.setRegistrationDay(dateInput.getValue());
            c.setMaxChoices(parseInt(maxInput.getValue(), 5));
            c.setStartDate(startDateInput.getValue());
            c.setEndDate(endDateInput.getValue());
            c.setStatus("PREPARATION");
            c.setCreatedBy(user.getId());

            int id = campaignService.createCampaign(c);
            if (id > 0) {
                closeTopLayer();
                refreshAllData();
                refreshDashboardView();
                refreshCampaignForm();
                onResize();
            }
        });
        create.setBackground(new Color(12, 16, 44));

        modal.getBody().addChild(nameInput);
        modal.getBody().addChild(promoInput);
        modal.getBody().addChild(dateInput);
        modal.getBody().addChild(maxInput);
        modal.getBody().addChild(startDateInput);
        modal.getBody().addChild(endDateInput);
        modal.getBody().addChild(cancel);
        modal.getBody().addChild(create);

        window.openModal(modal);
    }

    private void toggleCampaignStatus() {
        activeCampaign = resolveActiveCampaign();
        if (activeCampaign == null) {
            openCreateCampaignModal();
            return;
        }
        String next = "OPEN".equals(activeCampaign.getStatus()) ? "CLOSED" : "OPEN";
        if ("PREPARATION".equals(activeCampaign.getStatus())) {
            next = "OPEN";
        }
        ServiceResult result = campaignService.changeStatus(activeCampaign.getId(), next);
        System.out.println("[AdminDashboard] " + result.getMessage());
        refreshDashboardView();
    }

    private void runAutoAssignment() {
        if (activeCampaign == null) return;
        
        campaignFeedbackLabel.setColor(new java.awt.Color(66, 133, 244));
        campaignFeedbackLabel.setText("Assignation automatique en cours. Merci de patienter...");
        window.requestRenderIfNeeded();
        final int id = activeCampaign.getId();

        Thread t = new Thread(() -> {
            ServiceResult result = assignmentService.runAutoAssignment(id);
            javax.swing.SwingUtilities.invokeLater(() -> {
                campaignFeedbackLabel.setColor(result.isSuccess() ? new java.awt.Color(34, 197, 94) : new java.awt.Color(239, 68, 68));
                campaignFeedbackLabel.setText(result.getMessage());
                refreshAllData();
                refreshDashboardView();
                refreshCampaignForm();
                window.requestRenderIfNeeded();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private Campaign resolveActiveCampaign() {
        Campaign openCampaign = selectBestCampaign(campaignService.getCampaignsByStatus("OPEN"));
        if (openCampaign != null) {
            return openCampaign;
        }
        Campaign preparationCampaign = selectBestCampaign(campaignService.getCampaignsByStatus("PREPARATION"));
        if (preparationCampaign != null) {
            return preparationCampaign;
        }
        return selectBestCampaign(campaignService.getCampaignsByStatus("CLOSED"));
    }

    private Campaign selectBestCampaign(List<Campaign> campaigns) {
        if (campaigns == null || campaigns.isEmpty()) {
            return null;
        }

        String promo = user == null ? null : user.getPromo();
        Campaign bestPromo = campaigns.stream()
                .filter(c -> promo != null && promo.equalsIgnoreCase(safe(c.getPromo())))
                .max(Comparator.comparing(this::campaignDateForSort)
                        .thenComparing(Campaign::getId))
                .orElse(null);
        if (bestPromo != null) {
            return bestPromo;
        }

        return campaigns.stream()
                .max(Comparator.comparing(this::campaignDateForSort)
                        .thenComparing(Campaign::getId))
                .orElse(campaigns.get(0));
    }

    private LocalDate campaignDateForSort(Campaign campaign) {
        if (campaign == null || campaign.getRegistrationDay() == null || campaign.getRegistrationDay().isBlank()) {
            return LocalDate.MIN;
        }
        try {
            return LocalDate.parse(campaign.getRegistrationDay(), FR_DATE);
        } catch (Exception ignored) {
            return LocalDate.MIN;
        }
    }

    private ArrayList<DominanteStat> computeDominanteStats(List<Dominante> dominantes, List<SessionSlot> sessions, Map<Integer, Integer> allocationsBySession) {
        ArrayList<DominanteStat> result = new ArrayList<>();
        Map<Integer, DominanteStat> byId = new HashMap<>();

        for (Dominante dominante : dominantes) {
            DominanteStat stat = new DominanteStat();
            stat.dominante = dominante;
            byId.put(Integer.valueOf(dominante.getId()), stat);
        }

        for (SessionSlot session : sessions) {
            DominanteStat stat = byId.get(Integer.valueOf(session.getDominanteId()));
            if (stat == null) {
                continue;
            }
            stat.sessionCount += 1;
            stat.capacity += session.getCapacity();
            Integer allocated = allocationsBySession.get(Integer.valueOf(session.getId()));
            if (allocated != null) {
                stat.allocated += allocated.intValue();
            }
        }

        for (DominanteStat stat : byId.values()) {
            if (stat.sessionCount > 0) {
                result.add(stat);
            }
        }
        return result;
    }

    private void applyDominanteRows(List<DominanteStat> stats) {
        for (int i = 0; i < dominanteRows.size(); i++) {
            DominanteOverviewRow row = dominanteRows.get(i);
            if (i < stats.size()) {
                DominanteStat stat = stats.get(i);
                int rate = stat.capacity <= 0 ? 0 : (int) Math.round((stat.allocated * 100.0) / stat.capacity);
                row.setData(stat.dominante.getCode(), stat.dominante.getName(), stat.sessionCount, stat.allocated,
                        stat.capacity, rate);
                row.setVisible(true);
            } else {
                row.setVisible(false);
            }
        }
    }

    private void hideDominanteRows() {
        for (DominanteOverviewRow row : dominanteRows) {
            row.setVisible(false);
        }
    }

    private int resolveDominanteId(List<Dominante> dominantes, String selectedName) {
        for (Dominante dominante : dominantes) {
            if (dominante.getName().equals(selectedName)) {
                return dominante.getId();
            }
        }
        return dominantes.isEmpty() ? 0 : dominantes.get(0).getId();
    }

    private int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int parseTimeToMinute(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) {
            return 540;
        }
        String[] p = hhmm.trim().split(":");
        if (p.length != 2) {
            return 540;
        }
        int h = parseInt(p[0], 9);
        int m = parseInt(p[1], 0);
        return (h * 60) + m;
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format(Locale.US, "%02d:%02d", Integer.valueOf(h), Integer.valueOf(m));
    }

    private String buildCode(String name) {
        if (name == null || name.isBlank()) {
            return "DOM";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                code.append(part.substring(0, 1).toUpperCase());
            }
            if (code.length() >= 3) {
                break;
            }
        }
        if (code.length() == 0) {
            code.append("DOM");
        }
        while (code.length() < 2) {
            code.append('X');
        }
        return code.toString();
    }

    private Color accentForCode(String code) {
        String key = code == null ? "--" : code;
        int hash = Math.abs(key.hashCode());
        Color[] colors = new Color[] {
                new Color(124, 92, 255),
                new Color(239, 68, 68),
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(245, 158, 11)
        };
        return colors[hash % colors.length];
    }

    private String statusLabel(String status) {
        if (status == null) {
            return "Statut: -";
        }
        if ("OPEN".equals(status)) {
            return "Inscriptions ouvertes";
        }
        if ("CLOSED".equals(status)) {
            return "Inscriptions fermees";
        }
        if ("PREPARATION".equals(status)) {
            return "Campagne en preparation";
        }
        return "Statut: " + status;
    }

    private String buttonTextForStatus(String status) {
        if ("OPEN".equals(status)) {
            return "Fermer les inscriptions";
        }
        if ("PREPARATION".equals(status)) {
            return "Ouvrir les inscriptions";
        }
        return "Creer campagne";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    // private String estimateEndDate(String startDate) {
    //     if (startDate == null || startDate.isBlank()) {
    //         return "-";
    //     }
    //     try {
    //         LocalDate start = LocalDate.parse(startDate, FR_DATE);
    //         return start.plusDays(10).format(FR_DATE);
    //     } catch (Exception ignored) {
    //         return "-";
    //     }
    // }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getLogin() == null ? "" : user.getLogin();
    }

    private void headerTitle(String text) {
        // PageHeader currently has fixed title text; recreating is unnecessary for now, use subtitle updates.
        // Keep this method as extension point for future title setter.
    }

    private void applyDarkTheme() {
        sidebar.setDarkMode(dominanteDarkMode);
        header.setDarkMode(dominanteDarkMode);
        
        BaseComp content = window.getContent();
        if (content != null) {
            content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        }
        
        campagneSection.setStyleManager(new style.StyleManager(PAGE_BG, 0, 100, 100, 0, 0, "absolute"));
        dominantesSection.setStyleManager(new style.StyleManager(PAGE_BG, 0, 100, 100, 0, 0, "absolute"));
        dominantesList.setStyleManager(new style.StyleManager(PAGE_BG, 0, 100, 100, 0, 0, "absolute"));
        sessionsSection.setStyleManager(new style.StyleManager(PAGE_BG, 0, 100, 100, 0, 0, "absolute"));
        dashboardSection.setStyleManager(new style.StyleManager(PAGE_BG, 0, 100, 100, 0, 0, "absolute"));
    }

    private void styleDominantesActionButtons() {
        nouvelleDominanteButton.setBackground(new Color(30, 93, 57));
        nouvelleDominanteButton.setForeground(new Color(233, 247, 238));
    }

    private void toggleGlobalTheme() {
        dominanteDarkMode = !dominanteDarkMode;
        if (dominanteDarkMode) {
            PAGE_BG = new Color(14, 18, 26);
            PANEL_BG = new Color(22, 28, 39);
            PANEL_BORDER = new Color(48, 60, 82);
            TEXT_MAIN = new Color(235, 241, 255);
            TEXT_MUTED = new Color(151, 166, 194);
        } else {
            PAGE_BG = new Color(243, 246, 252);
            PANEL_BG = Color.WHITE;
            PANEL_BORDER = new Color(226, 232, 240);
            TEXT_MAIN = new Color(15, 23, 42);
            TEXT_MUTED = new Color(100, 116, 139);
        }
        
        applyDarkTheme();
        
        // Backgrounds updates
        campaignCard.setBackground(PANEL_BG);
        campaignCard.setBorderColor(PANEL_BORDER);
        dominanteOverviewCard.setBackground(PANEL_BG);
        dominanteOverviewCard.setBorderColor(PANEL_BORDER);
        campaignSettingsCard.setBackground(PANEL_BG);
        campaignSettingsCard.setBorderColor(PANEL_BORDER);
        sessionsFilterCard.setBackground(PANEL_BG);
        sessionsFilterCard.setBorderColor(PANEL_BORDER);
        campaignFormCard.setBackground(PANEL_BG);
        campaignFormCard.setBorderColor(PANEL_BORDER);

        // Texts updates (Main texts)
        campaignTitle.setColor(TEXT_MAIN);
        campaignDates.setColor(TEXT_MUTED);
        dominanteOverviewTitle.setColor(TEXT_MAIN);
        dominanteOverviewSubtitle.setColor(TEXT_MUTED);
        campaignSettingsTitle.setColor(TEXT_MAIN);
        maxChoicesLabel.setColor(TEXT_MUTED);
        maxChoicesValue.setColor(TEXT_MAIN);
        startDateLabel.setColor(TEXT_MUTED);
        startDateValue.setColor(TEXT_MAIN);
        endDateLabel.setColor(TEXT_MUTED);
        endDateValue.setColor(TEXT_MAIN);
        sessionsTotalLabel.setColor(TEXT_MUTED);

        dominanteKpi.setDarkMode(dominanteDarkMode);
        sessionsKpi.setDarkMode(dominanteDarkMode);
        inscriptionsKpi.setDarkMode(dominanteDarkMode);
        fillRateKpi.setDarkMode(dominanteDarkMode);

        refreshDashboardView();
        refreshDominantesView();
        refreshSessionsView();
        refreshCampaignForm();
        window.requestRenderIfNeeded();
    }

    private void closeTopLayer() {
        window.closeTopLayer();
    }

    private void clearChildren(BaseComp parent) {
        ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList());
        for (BaseComp child : snapshot) {
            parent.removeChild(child);
        }
    }

}
