package gui.screen.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import main.BaseComp;
import gui.components.SurfaceCard;
import components.Button;
import components.Label;
import components.ScrollView;
import model.User;
import service.StatisticsService;

public class StudentDetailPanel {
    private final StatisticsService statsService;
    private final SurfaceCard container;
    private final SurfaceCard header;
    private final Button backBtn;
    private final Label studentName;
    private final Label studentInfo;
    private final ScrollView sessionsScroll;
    private final BaseComp sessionsList;
    private final Label emptyLabel;
    private Runnable onBack;
    private User currentStudent;
    private int campaignId;
    private boolean darkMode = true;

    public StudentDetailPanel(StatisticsService statsService) {
        this.statsService = statsService;
        container = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        header = new SurfaceCard(0, 0, 100, 80, new Color(18, 24, 35), new Color(52, 63, 92), 12);

        backBtn = new Button("< Retour", 12, 16, 90, 30, () -> { if (onBack != null) onBack.run(); });
        backBtn.setBackground(new Color(40, 50, 70));
        backBtn.setForeground(new Color(219, 230, 253));

        studentName = new Label("", 110, 14, 360, 22);
        studentName.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
        studentName.setColor(new Color(239, 244, 252));

        studentInfo = new Label("", 110, 40, 360, 18);
        studentInfo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        studentInfo.setColor(new Color(161, 175, 202));

        header.addChild(backBtn); header.addChild(studentName); header.addChild(studentInfo);

        Label sectionTitle = new Label("Sessions inscrites", 12, 12, 200, 18);
        sectionTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        sectionTitle.setColor(new Color(239, 244, 252));

        sessionsScroll = new ScrollView(0, 88, 100, 100);
        sessionsList = sessionsScroll.getContent();

        emptyLabel = new Label("Aucune session trouvee", 8, 8, 200, 24);
        emptyLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        emptyLabel.setColor(new Color(100, 116, 139));

        container.addChild(header); container.addChild(sectionTitle); container.addChild(sessionsScroll);
        sessionsList.addChild(emptyLabel);
    }

    public BaseComp getRoot() { return container; }
    public void setOnBack(Runnable r) { this.onBack = r; }

    public void loadStudent(User student, int campaignId) {
        this.currentStudent = student;
        this.campaignId = campaignId;
        String name = student.getFullName() != null && !student.getFullName().isBlank() ? student.getFullName() : student.getLogin();
        studentName.setText(name);
        studentInfo.setText("@" + student.getLogin() + " | Promo " + student.getPromo());
        refresh();
    }

    public void refresh() {
        clearChildren(sessionsList);
        if (currentStudent == null || campaignId <= 0) { sessionsList.addChild(emptyLabel); return; }
        List<StatisticsService.SessionDetail> sessions = statsService.getSessionsForStudent(campaignId, currentStudent.getId());
        if (sessions.isEmpty()) { sessionsList.addChild(emptyLabel); sessionsScroll.setContentHeight(40); return; }
        int y = 0;
        for (StatisticsService.SessionDetail s : sessions) {
            SurfaceCard card = new SurfaceCard(4, y, sessionsScroll.getWidth() - 12, 64,
                s.isFull ? new Color(255, 245, 220) : new Color(30, 40, 58),
                new Color(52, 63, 92), 8);
            Label title = new Label(safe(s.sessionTitle), 12, 8, card.getWidth() - 120, 20);
            title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
            title.setColor(s.isFull ? new Color(180, 120, 20) : new Color(239, 244, 252));
            int fill = s.allocated, cap = s.capacity;
            int pct = cap > 0 ? (fill * 100) / cap : 0;
            Label info = new Label(s.dominanteName + " | " + s.timeSlot, 12, 32, 200, 16);
            info.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            info.setColor(new Color(132, 144, 168));
            Label bar = new Label(buildBar(pct), card.getWidth() - 120, 32, 110, 16);
            bar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
            bar.setColor(s.isFull ? new Color(180, 120, 20) : new Color(34, 197, 94));
            card.addChild(title); card.addChild(info); card.addChild(bar);
            sessionsList.addChild(card);
            y += 72;
        }
        sessionsScroll.setContentHeight(Math.max(sessionsScroll.getHeight(), y + 8));
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
        sessionsScroll.setBounds(0, 88, w, h - 92);
    }

    private String buildBar(int pct) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 16; i++) sb.append(i < (pct / 6) ? "█" : "░");
        return sb.append("] ").append(pct).append("%").toString();
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}