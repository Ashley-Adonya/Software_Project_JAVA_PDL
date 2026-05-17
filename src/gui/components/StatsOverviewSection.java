package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import main.BaseComp;
import components.Label;
import components.ScrollView;
import components.Button;
import model.User;
import service.StatisticsService;

/**
 * A comprehensive statistics overview section for the dashboard. It displays
 * four KPI cards (total sessions, complete sessions, average fill rate, and
 * unregistered students), a panel listing unregistered students, and a
 * student planning panel that shows a selected student's session schedule.
 * The section integrates with {@link StatisticsService} to fetch session
 * data and supports dark mode toggling.
 */
public class StatsOverviewSection extends BaseComp {
    private final Color PAGE_BG;
    private final Color PANEL_BG;
    private final Color PANEL_BORDER;
    private final Color TEXT_MAIN;
    private final Color TEXT_MUTED;

    private final KpiCard totalSessionsKpi;
    private final KpiCard completeSessionsKpi;
    private final KpiCard fillRateKpi;
    private final KpiCard unregisteredKpi;

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

    private Runnable onStudentSelected;
    private StatisticsService statisticsService;
    private int campaignId;

    /**
     * Constructs the statistics overview section with KPI cards, an
     * unregistered students panel, and a student planning panel.
     *
     * @param width       the initial width of this section
     * @param height      the initial height of this section
     * @param statsService the service used to fetch statistics and session data
     */
    public StatsOverviewSection(int width, int height, StatisticsService statsService) {
        super(null);
        
        this.PAGE_BG = new Color(14, 18, 26);
        this.PANEL_BG = new Color(22, 28, 39);
        this.PANEL_BORDER = new Color(48, 60, 82);
        this.TEXT_MAIN = new Color(235, 241, 255);
        this.TEXT_MUTED = new Color(151, 166, 194);
        
        this.statisticsService = statsService;
        
        setStyleManager(new style.StyleManager(PAGE_BG, 0, width, height, 0, 0, "absolute"));

        this.totalSessionsKpi = new KpiCard("Sessions totales", "0", "Nombre total", new Color(59, 130, 246));
        this.completeSessionsKpi = new KpiCard("Sessions completes", "0", "Plein", new Color(34, 197, 94));
        this.fillRateKpi = new KpiCard("Taux remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        this.unregisteredKpi = new KpiCard("Non inscrits", "0", "Etudiants", new Color(239, 68, 68));

        addChild(totalSessionsKpi);
        addChild(completeSessionsKpi);
        addChild(fillRateKpi);
        addChild(unregisteredKpi);

        this.unregisteredStudentsCard = new SurfaceCard(0, 0, 100, 100, PANEL_BG, PANEL_BORDER, 12);
        this.unregisteredStudentsTitle = new Label("Etudiants non inscrits", 0, 0, 200, 22);
        unregisteredStudentsTitle.setFont(new Font("Dialog", Font.BOLD, 16));
        unregisteredStudentsTitle.setColor(TEXT_MAIN);
        unregisteredStudentsCard.addChild(unregisteredStudentsTitle);

        this.unregisteredScroll = new ScrollView(0, 0, 100, 100);
        this.unregisteredList = unregisteredScroll.getContent();
        unregisteredStudentsCard.addChild(unregisteredScroll);
        addChild(unregisteredStudentsCard);

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
        addChild(studentPlanningCard);
    }

    /**
     * Sets the campaign ID used when fetching student session data.
     *
     * @param campaignId the identifier of the current campaign
     */
    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    /**
     * Registers a callback to be invoked when a student is selected from the
     * unregistered list.
     *
     * @param r the runnable to invoke on student selection; may be null
     */
    public void setOnStudentSelected(Runnable r) {
        this.onStudentSelected = r;
    }

    /**
     * Updates the four KPI cards with the provided summary statistics.
     *
     * @param stats the statistics summary containing total sessions, complete
     *              sessions, average fill rate, and unregistered student count
     */
    public void updateStats(StatisticsService.StatsSummary stats) {
        totalSessionsKpi.setValue(String.valueOf(stats.totalSessions));
        completeSessionsKpi.setValue(String.valueOf(stats.completeSessions));
        fillRateKpi.setValue(String.format("%.1f%%", stats.averageFillRate));
        unregisteredKpi.setValue(String.valueOf(stats.unregisteredStudents));
    }

    /**
     * Rebuilds the unregistered students list panel with the given students.
     * Each student is rendered as a clickable button that, when pressed,
     * selects that student and displays their session planning.
     *
     * @param students the list of unregistered students; may be empty
     */
    public void updateUnregisteredStudents(List<User> students) {
        clearChildren(unregisteredList);
        
        int y = 0;
        for (User student : students) {
            final User currentStudent = student;
            Button studentButton = new Button(
                    safe(student.getFullName()), 
                    0, y, unregisteredScroll.getWidth() - 8, 36,
                    () -> selectStudent(currentStudent));
            studentButton.setBackground(new Color(40, 50, 70));
            studentButton.setForeground(TEXT_MAIN);
            studentButton.setFont(new Font("Dialog", Font.PLAIN, 13));
            unregisteredList.addChild(studentButton);
            y += 40;
        }

        if (students.isEmpty()) {
            Label emptyLabel = new Label("Tous les etudiants sont inscrits", 0, 0, 200, 24);
            emptyLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
            emptyLabel.setColor(TEXT_MUTED);
            unregisteredList.addChild(emptyLabel);
        }

        unregisteredScroll.setContentHeight(Math.max(unregisteredScroll.getHeight(), y + 10));
    }

    private void selectStudent(User student) {
        selectedStudentLabel.setText("Planning de: " + safe(student.getFullName()));
        
        updateStudentPlanning(student);
        
        if (onStudentSelected != null) {
            onStudentSelected.run();
        }
    }

    private void updateStudentPlanning(User student) {
        if (student == null) {
            clearChildren(planningList);
            planningList.addChild(noStudentSelectedLabel);
            return;
        }

        StatisticsService.StudentWithSessions data = statisticsService.getStudentSessions(campaignId, student.getId());
        
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

    private void clearChildren(BaseComp parent) {
        if (parent == null) {
            return;
        }
        for (BaseComp child : new ArrayList<>(parent.getChildrenList())) {
            parent.removeChild(child);
        }
    }

    /**
     * Recalculates and applies the layout of all child components based on
     * the available main width. Places KPI cards in a row, then positions
     * the two panels (unregistered students and student planning) side by
     * side below them.
     *
     * @param mainW the available width for layout
     */
    public void layout(int mainW) {
        int gap = 12;
        int kpiW = (mainW - gap * 3) / 4;
        int kpiH = 100;
        int startY = 12;

        totalSessionsKpi.setBounds(0, startY, kpiW, kpiH);
        completeSessionsKpi.setBounds(kpiW + gap, startY, kpiW, kpiH);
        fillRateKpi.setBounds((kpiW + gap) * 2, startY, kpiW, kpiH);
        unregisteredKpi.setBounds((kpiW + gap) * 3, startY, kpiW, kpiH);

        int cardY = startY + kpiH + gap;
        int cardH = 280;
        int halfW = (mainW - gap) / 2;

        unregisteredStudentsCard.setBounds(0, cardY, halfW, cardH);
        unregisteredStudentsTitle.setBounds(16, 12, halfW - 32, 22);
        unregisteredScroll.setBounds(8, 40, halfW - 16, cardH - 48);

        studentPlanningCard.setBounds(halfW + gap, cardY, halfW, cardH);
        studentPlanningTitle.setBounds(16, 12, halfW - 32, 22);
        selectedStudentLabel.setBounds(16, 38, halfW - 32, 20);
        planningScroll.setBounds(8, 64, halfW - 16, cardH - 80);
    }

    /**
     * Toggles the visual theme between dark mode and light mode for all
     * child panels, cards, and labels.
     *
     * @param darkMode true to apply the dark theme, false for the light theme
     */
    public void setDarkMode(boolean darkMode) {
        Color panelBg = darkMode ? new Color(22, 28, 39) : Color.WHITE;
        Color borderColor = darkMode ? new Color(48, 60, 82) : new Color(226, 232, 240);
        Color textMain = darkMode ? new Color(235, 241, 255) : new Color(15, 23, 42);
        Color textMuted = darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139);
        
        unregisteredStudentsCard.setBackground(panelBg);
        unregisteredStudentsCard.setBorderColor(borderColor);
        studentPlanningCard.setBackground(panelBg);
        studentPlanningCard.setBorderColor(borderColor);
        
        unregisteredStudentsTitle.setColor(textMain);
        studentPlanningTitle.setColor(textMain);
        selectedStudentLabel.setColor(textMuted);
        noStudentSelectedLabel.setColor(textMuted);
        
        totalSessionsKpi.setDarkMode(darkMode);
        completeSessionsKpi.setDarkMode(darkMode);
        fillRateKpi.setDarkMode(darkMode);
        unregisteredKpi.setDarkMode(darkMode);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}