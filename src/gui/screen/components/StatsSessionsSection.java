package gui.screen.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Consumer;
import main.BaseComp;
import service.StatisticsService;
import gui.components.SurfaceCard;
import components.Label;
import components.ScrollView;
import event.UiEvent;

public class StatsSessionsSection {
    private final SurfaceCard container;
    private final Label title;
    private final ScrollView scroll;
    private final BaseComp content;
    private final Label emptyLabel;
    private Consumer<StatisticsService.SessionDetail> onSelectSession = s -> {};
    private boolean darkMode = true;

    public StatsSessionsSection() {
        container = new SurfaceCard(0, 0, 100, 100, new Color(22, 28, 39), new Color(52, 63, 92), 12);

        title = new Label("Sessions", 16, 14, 220, 22);
        title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 15));
        title.setColor(new Color(239, 244, 252));

        scroll = new ScrollView(8, 44, 100, 100);
        content = scroll.getContent();

        emptyLabel = new Label("Aucune session definie", 8, 8, 200, 24);
        emptyLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        emptyLabel.setColor(new Color(100, 116, 139));

        container.addChild(title); container.addChild(scroll);
    }

    public SurfaceCard getRoot() { return container; }
    public void setOnSelectSession(Consumer<StatisticsService.SessionDetail> cb) { this.onSelectSession = cb; }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
        container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        container.invalidate();
    }

    public void refresh(StatisticsService.StatsSummary s) {
        clearChildren(content);
        if (s.sessionDetails == null || s.sessionDetails.isEmpty()) {
            content.addChild(emptyLabel); scroll.setContentHeight(40); return;
        }
        int y = 8;
        int scW = scroll.getWidth();
        int cardW = scW > 48 ? scW - 16 : 300;
        for (StatisticsService.SessionDetail detail : s.sessionDetails) {
            SurfaceCard card = new SurfaceCard(8, y, cardW, 72,
                detail.isFull ? new Color(255, 245, 220) : new Color(30, 40, 58),
                new Color(60, 72, 100), 8);
            card.setCursor(12);

            String name = safe(detail.sessionTitle);
            Label nameLabel = new Label(name, 14, 10, cardW - 100, 20);
            nameLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
            nameLabel.setColor(detail.isFull ? new Color(180, 120, 20) : new Color(239, 244, 252));

            int fill = detail.allocated;
            int cap = detail.capacity;
            int pct = cap > 0 ? (fill * 100) / cap : 0;
            Label infoLabel = new Label(detail.dominanteName + " | " + fill + "/" + cap + " places", 14, 36, 220, 16);
            infoLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            infoLabel.setColor(new Color(132, 144, 168));

            Label timeLabel = new Label(detail.timeSlot, cardW - 160, 10, 140, 16);
            timeLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
            timeLabel.setColor(new Color(132, 144, 168));

            Label barLabel = new Label(buildBar(pct), cardW - 150, 36, 130, 18);
            barLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
            barLabel.setColor(detail.isFull ? new Color(180, 120, 20) : new Color(34, 197, 94));

            card.addChild(nameLabel); card.addChild(infoLabel);
            card.addChild(timeLabel); card.addChild(barLabel);
            card.getEventManager().register(UiEvent.Type.POINTER_UP, (c, e) -> onSelectSession.accept(detail));
            content.addChild(card);
            y += 80;
        }
        scroll.setContentHeight(y + 8);
    }

    private String buildBar(int pct) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append(i < (pct / 6) ? "\u2588" : "\u2591");
        return sb.append(" ").append(pct).append("%").toString();
    }

    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        scroll.setBounds(8, 44, w - 16, h - 52);
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}