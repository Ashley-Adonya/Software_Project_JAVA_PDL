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

/**
 * Section of the statistics panel that displays the distribution of students
 * across all sessions for a given campaign. Each session is rendered as a
 * clickable card showing its title, dominante, time slot, and fill rate.
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Arranges session cards in one or two columns depending on available width</li>
 *   <li>Highlights fully booked sessions with a distinct warm colour scheme</li>
 *   <li>Displays a textual fill-rate indicator (allocated / capacity + percentage)</li>
 *   <li>Emits a callback when a session card is clicked, enabling drill-down navigation</li>
 *   <li>Supports dark/light mode theming and window resize events</li>
 * </ul>
 */
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

        title = new Label("Répartition dans les Sessions", 20, 20, 300, 24);
        title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
        title.setColor(new Color(239, 244, 252));

        scroll = new ScrollView(0, 60, 100, 100);
        content = scroll.getContent();

        emptyLabel = new Label("Aucune session définie pour cette campagne.", 20, 10, 300, 24);
        emptyLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 13));
        emptyLabel.setColor(new Color(100, 116, 139));

        container.addChild(title); container.addChild(scroll);
    }

    /**
     * Returns the root UI component of this section, a {@code SurfaceCard}
     * containing the title and the scrollable session card grid.
     *
     * @return the root {@link SurfaceCard} instance
     */
    public SurfaceCard getRoot() { return container; }

    /**
     * Registers a callback invoked when a session card is clicked.
     *
     * @param cb a {@link Consumer} accepting the clicked {@link StatisticsService.SessionDetail}
     */
    public void setOnSelectSession(Consumer<StatisticsService.SessionDetail> cb) { this.onSelectSession = cb; }

    /**
     * Switches the section between dark and light colour themes.
     * Updates the container background, border colour, and title colour.
     *
     * @param dark {@code true} for dark mode, {@code false} for light mode
     */
    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
        container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        title.setColor(dark ? Color.WHITE : new Color(15, 23, 42));
        container.invalidate();
    }

    /**
     * Rebuilds the session card grid from the provided statistics summary.
     * Determines column count based on scroll width (2 columns above 700 px, 1 otherwise).
     * Clears any existing content and repopulates with new cards, recalculating
     * scroll height to fit all cards.
     *
     * @param s the {@link StatisticsService.StatsSummary} containing session detail data
     */
    public void refresh(StatisticsService.StatsSummary s) {
        clearChildren(content);
        if (s.sessionDetails == null || s.sessionDetails.isEmpty()) {
            content.addChild(emptyLabel); scroll.setContentHeight(60); return;
        }
        int y = 0;
        int scW = scroll.getWidth();
        // Deux colonnes si la place le permet
        int cols = scW > 700 ? 2 : 1; 
        int gap = 16;
        int cardW = (scW - (cols + 1) * gap) / cols;

        for (int i = 0; i < s.sessionDetails.size(); i++) {
            StatisticsService.SessionDetail detail = s.sessionDetails.get(i);
            
            SurfaceCard card = new SurfaceCard(0, 0, cardW, 86,
                detail.isFull ? new Color(255, 245, 220) : new Color(30, 40, 58),
                new Color(60, 72, 100), 8);
            card.setCursor(12);

            String name = safe(detail.sessionTitle);
            Label nameLabel = new Label(name, 16, 14, cardW - 130, 20);
            nameLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
            nameLabel.setColor(detail.isFull ? new Color(180, 120, 20) : (darkMode ? new Color(239, 244, 252) : new Color(15, 23, 42)));

            int fill = detail.allocated;
            int cap = detail.capacity;
            int pct = cap > 0 ? (fill * 100) / cap : 0;
            
            Label infoLabel = new Label(detail.dominanteName + "  |  " + detail.timeSlot, 16, 38, cardW - 100, 16);
            infoLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            infoLabel.setColor(darkMode ? new Color(132, 144, 168) : new Color(100, 116, 139));

            // Barre de pourçentage simulée typographique
            Label barLabel = new Label(fill + " / " + cap + " inscrits  (" + pct + "%)", 16, 60, 200, 18);
            barLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
            barLabel.setColor(detail.isFull ? new Color(180, 120, 20) : new Color(34, 197, 94));

            card.addChild(nameLabel); card.addChild(infoLabel); card.addChild(barLabel);
            card.getEventManager().register(UiEvent.Type.POINTER_UP, (c, e) -> onSelectSession.accept(detail));
            
            int col = i % cols;
            int row = i / cols;
            int posX = gap + col * (cardW + gap);
            int posY = 10 + row * (86 + gap);
            card.setBounds(posX, posY, cardW, 86);
            
            content.addChild(card);
            y = Math.max(y, posY + 86);
        }
        scroll.setContentHeight(y + 20);
    }

    /**
     * Adjusts the layout when the parent container is resized. Positions the
     * title at the top and expands the scrollable card area to fill the
     * remaining vertical space.
     *
     * @param w the new width in pixels
     * @param h the new height in pixels
     */
    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        scroll.setBounds(0, 56, w, h - 56);
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}