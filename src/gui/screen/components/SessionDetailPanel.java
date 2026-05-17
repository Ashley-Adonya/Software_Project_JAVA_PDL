package gui.screen.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import main.BaseComp;
import gui.components.SurfaceCard;
import components.Button;
import components.Label;
import components.ScrollView;
import model.User;
import service.StatisticsService;

public class SessionDetailPanel {
    private final StatisticsService statsService;
    private final SurfaceCard container;
    private final SurfaceCard header;
    private final Button backBtn;
    private final Label sessionTitle;
    private final Label sessionInfo;
    private final ScrollView studentsScroll;
    private final BaseComp studentsList;
    private final Label emptyLabel;
    private Runnable onBack;
    private StatisticsService.SessionDetail currentSession;
    private int campaignId;
    private boolean darkMode = true;

    public SessionDetailPanel(StatisticsService statsService) {
        this.statsService = statsService;
        container = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        header = new SurfaceCard(0, 0, 100, 80, new Color(18, 24, 35), new Color(52, 63, 92), 12);

        backBtn = new Button("< Retour", 12, 16, 90, 30, () -> { if (onBack != null) onBack.run(); });
        backBtn.setBackground(new Color(40, 50, 70));
        backBtn.setForeground(new Color(219, 230, 253));

        sessionTitle = new Label("", 110, 14, 360, 22);
        sessionTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
        sessionTitle.setColor(new Color(239, 244, 252));

        sessionInfo = new Label("", 110, 40, 360, 18);
        sessionInfo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        sessionInfo.setColor(new Color(161, 175, 202));

        header.addChild(backBtn); header.addChild(sessionTitle); header.addChild(sessionInfo);

        Label sectionTitle = new Label("Etudiants inscrits", 12, 12, 200, 18);
        sectionTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        sectionTitle.setColor(new Color(239, 244, 252));

        studentsScroll = new ScrollView(0, 88, 100, 100);
        studentsList = studentsScroll.getContent();

        emptyLabel = new Label("Aucun etudiant inscrit", 8, 8, 200, 24);
        emptyLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        emptyLabel.setColor(new Color(100, 116, 139));

        container.addChild(header); container.addChild(sectionTitle); container.addChild(studentsScroll);
        studentsList.addChild(emptyLabel);
    }

    public BaseComp getRoot() { return container; }
    public void setOnBack(Runnable r) { this.onBack = r; }

    public void loadSession(StatisticsService.SessionDetail session, int campaignId) {
        this.currentSession = session;
        this.campaignId = campaignId;
        sessionTitle.setText(safe(session.sessionTitle));
        int pct = session.capacity > 0 ? (session.allocated * 100) / session.capacity : 0;
        sessionInfo.setText(session.dominanteName + " | " + session.timeSlot + " | " + session.allocated + "/" + session.capacity);
        refresh();
    }

    public void refresh() {
        clearChildren(studentsList);
        if (currentSession == null || campaignId <= 0) { studentsList.addChild(emptyLabel); return; }
        List<User> students = statsService.getStudentsInSession(campaignId, currentSession.sessionId);
        if (students.isEmpty()) { studentsList.addChild(emptyLabel); studentsScroll.setContentHeight(40); return; }
        int y = 0;
        for (User u : students) {
            String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getLogin();
            SurfaceCard card = new SurfaceCard(4, y, studentsScroll.getWidth() - 12, 48,
                new Color(30, 40, 58), new Color(52, 63, 92), 8);
            Label nameLabel = new Label(name, 12, 8, 260, 20);
            nameLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
            nameLabel.setColor(new Color(239, 244, 252));
            Label infoLabel = new Label("@" + u.getLogin() + " | " + u.getPromo(), 12, 30, 260, 14);
            infoLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
            infoLabel.setColor(new Color(132, 144, 168));
            card.addChild(nameLabel); card.addChild(infoLabel);
            studentsList.addChild(card);
            y += 56;
        }
        Label countLabel = new Label(students.size() + " etudiant(s)", 8, y + 8, 200, 16);
        countLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        countLabel.setColor(new Color(100, 116, 139));
        studentsList.addChild(countLabel);
        studentsScroll.setContentHeight(Math.max(studentsScroll.getHeight(), y + 40));
    }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(14, 18, 26) : Color.WHITE);
        header.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
        header.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
    }

    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        header.setBounds(0, 0, w, 80);
        studentsScroll.setBounds(0, 88, w, h - 92);
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}