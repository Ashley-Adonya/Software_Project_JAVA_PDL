package gui.screen.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Consumer;
import main.BaseComp;
import model.User;
import service.StatisticsService;
import gui.components.SurfaceCard;
import components.Label;
import components.ScrollView;

public class StatsFilterSection {
    private final StatisticsService statsService;
    private final SurfaceCard container;
    private final gui.components.SearchField searchField;
    private final ScrollView resultsScroll;
    private final BaseComp resultsContent;
    private Consumer<StatisticsService.SessionDetail> onSelectSession = s -> {};
    private boolean darkMode = true;
    private int campaignId = -1;
    private String promo = "";

    public StatsFilterSection(StatisticsService statsService) {
        this.statsService = statsService;
        container = new SurfaceCard(0, 0, 100, 100, new Color(22, 28, 39), new Color(52, 63, 92), 12);

        Label title = new Label("Recherche", 16, 12, 200, 20);
        title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        title.setColor(new Color(239, 244, 252));

        searchField = new gui.components.SearchField(16, 40, 320, 32, "Rechercher un etudiant ou une session...");
        searchField.setColors(new Color(28, 36, 50), new Color(52, 63, 92), new Color(82, 107, 255), new Color(239, 244, 252), new Color(132, 144, 168));
        searchField.setOnChange(this::performSearch);

        resultsScroll = new ScrollView(16, 80, 100, 72);
        resultsContent = resultsScroll.getContent();

        Label hint = new Label("Tapez pour rechercher un etudiant ou une session", 8, 8, 280, 16);
        hint.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        hint.setColor(new Color(100, 116, 139));
        resultsContent.addChild(hint);

        container.addChild(title); container.addChild(searchField); container.addChild(resultsScroll);
    }

    public SurfaceCard getRoot() { return container; }

    public void refresh(int campaignId, String promo) {
        this.campaignId = campaignId;
        this.promo = promo;
    }

    private void performSearch() {
        String query = searchField.getCurrentText();
        if (query == null || query.isBlank()) {
            clearChildren(resultsContent);
            Label hint = new Label("Tapez pour rechercher...", 8, 8, 280, 16);
            hint.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            hint.setColor(new Color(100, 116, 139));
            resultsContent.addChild(hint);
            resultsScroll.setContentHeight(32);
            return;
        }
        String lower = query.toLowerCase();
        clearChildren(resultsContent);
        int y = 0;

        if (campaignId > 0) {
            StatisticsService.StatsSummary s = statsService.getStatsForCampaign(campaignId, promo);
            if (s.sessionDetails != null) {
                for (StatisticsService.SessionDetail detail : s.sessionDetails) {
                    if (detail.sessionTitle == null) continue;
                    if (detail.sessionTitle.toLowerCase().contains(lower) || detail.dominanteName.toLowerCase().contains(lower)) {
                        SurfaceCard card = resultCard(detail, y, resultsScroll.getWidth() - 32);
                        resultsContent.addChild(card);
                        y += 56;
                    }
                }
            }
            java.util.List<User> unreg = statsService.getUnregisteredStudents(campaignId, promo);
            java.util.List<User> reg = statsService.getRegisteredStudents(campaignId, promo);
            java.util.List<User> all = new java.util.ArrayList<>(reg); all.addAll(unreg);
            for (User u : all) {
                String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : "";
                String login = u.getLogin() != null ? u.getLogin() : "";
                if (name.toLowerCase().contains(lower) || login.toLowerCase().contains(lower)) {
                    SurfaceCard card = studentResultCard(u, y, resultsScroll.getWidth() - 32);
                    resultsContent.addChild(card);
                    y += 56;
                }
            }
        }

        if (y == 0) {
            Label no = new Label("Aucun resultat pour \"" + query + "\"", 8, 8, 280, 16);
            no.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            no.setColor(new Color(100, 116, 139));
            resultsContent.addChild(no);
            y = 32;
        }
        resultsScroll.setContentHeight(Math.max(resultsScroll.getHeight(), y + 8));
    }

    private SurfaceCard resultCard(StatisticsService.SessionDetail detail, int y, int w) {
        SurfaceCard card = new SurfaceCard(0, y, w, 48, new Color(35, 45, 65), new Color(52, 63, 92), 8);
        Label icon = new Label("[S]", 12, 12, 30, 22);
        icon.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        icon.setColor(new Color(59, 130, 246));
        Label titleLabel = new Label(detail.sessionTitle, 48, 8, 200, 18);
        titleLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        titleLabel.setColor(new Color(239, 244, 252));
        Label detailLabel = new Label(detail.dominanteName + " | " + detail.allocated + "/" + detail.capacity, 48, 28, 200, 14);
        detailLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        detailLabel.setColor(new Color(132, 144, 168));
        card.addChild(icon); card.addChild(titleLabel); card.addChild(detailLabel);
        return card;
    }

    private SurfaceCard studentResultCard(User u, int y, int w) {
        SurfaceCard card = new SurfaceCard(0, y, w, 48, new Color(35, 45, 65), new Color(52, 63, 92), 8);
        Label icon = new Label("[E]", 12, 12, 30, 22);
        icon.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        icon.setColor(new Color(34, 197, 94));
        String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getLogin();
        Label nameLabel = new Label(name, 48, 8, 200, 18);
        nameLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        nameLabel.setColor(new Color(239, 244, 252));
        Label loginLabel = new Label("@" + u.getLogin(), 48, 28, 200, 14);
        loginLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        loginLabel.setColor(new Color(132, 144, 168));
        card.addChild(icon); card.addChild(nameLabel); card.addChild(loginLabel);
        return card;
    }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
        container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
    }

    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        resultsScroll.setBounds(16, 80, w - 32, h - 88);
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
}