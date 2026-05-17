package gui.screen.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import main.BaseComp;
import gui.components.SurfaceCard;
import gui.components.SearchField;
import components.Label;
import components.ScrollView;
import model.User;
import service.StatisticsService;
import event.UiEvent;

/**
 * Section of the statistics panel that displays registered and unregistered
 * students side by side in two scrollable columns. Unregistered students can
 * be searched by name or login using an embedded search field.
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Left column: list of registered students with name and login</li>
 *   <li>Right column: unregistered (action-required) students with search filtering</li>
 *   <li>Each student card is clickable, emitting a callback for drill-down navigation</li>
 *   <li>Search field filters the unregistered list in real time as the user types</li>
 *   <li>Supports dark/light mode theming and window resize events</li>
 * </ul>
 */
public class StatsStudentsSection {
    private final SurfaceCard container;
    private final SearchField search;
    private final SurfaceCard regCard, unregCard;
    private final Label regTitle, unregTitle;
    private final ScrollView regScroll, unregScroll;
    private final BaseComp regList, unregList;
    private Consumer<User> onSelectStudent = u -> {};
    private List<User> registeredStudents = new ArrayList<>();
    private List<User> unregisteredStudents = new ArrayList<>();
    private boolean darkMode = true;

    public StatsStudentsSection() {
        container = new SurfaceCard(0, 0, 100, 100, new Color(22, 28, 39), new Color(0,0,0,0), 0); // Container Invisible

        regCard = new SurfaceCard(0, 0, 100, 100, new Color(18, 24, 35), new Color(52, 63, 92), 12);
        regTitle = new Label("Étudiants Inscrits", 20, 16, 200, 22);
        regTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 15));
        regTitle.setColor(new Color(239, 244, 252));
        regScroll = new ScrollView(12, 50, 100, 100);
        regList = regScroll.getContent();
        regCard.addChild(regTitle); regCard.addChild(regScroll);

        unregCard = new SurfaceCard(0, 0, 100, 100, new Color(18, 24, 35), new Color(52, 63, 92), 12);
        unregTitle = new Label("Action Requise (Non inscrits)", 20, 16, 250, 22);
        unregTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 15));
        unregTitle.setColor(new Color(239, 68, 68));
        
        search = new SearchField(20, 48, 200, 36, "Chercher nom / login...");
        search.setColors(new Color(28, 36, 50), new Color(52, 63, 92), new Color(82, 107, 255), new Color(239, 244, 252), new Color(132, 144, 168));
        search.setOnChange(this::filterStudents);
        
        unregScroll = new ScrollView(12, 94, 100, 100);
        unregList = unregScroll.getContent();
        unregCard.addChild(unregTitle); unregCard.addChild(search); unregCard.addChild(unregScroll);

        container.addChild(regCard); container.addChild(unregCard);
    }

    /**
     * Returns the root UI component of this section, an invisible container
     * {@code SurfaceCard} holding the two side-by-side column cards.
     *
     * @return the root {@link BaseComp} instance
     */
    public BaseComp getRoot() { return container; }

    /**
     * Registers a callback invoked when a student card (registered or unregistered) is clicked.
     *
     * @param cb a {@link Consumer} accepting the clicked {@link User}
     */
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }

    /**
     * Switches the section between dark and light colour themes.
     * Updates the container background and both column cards' backgrounds and border colours.
     *
     * @param dark {@code true} for dark mode, {@code false} for light mode
     */
    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(14, 18, 26) : Color.WHITE);
        regCard.setBackground(dark ? new Color(22, 28, 39) : new Color(248, 250, 252));
        unregCard.setBackground(dark ? new Color(22, 28, 39) : new Color(248, 250, 252));
        Color bd = dark ? new Color(52, 63, 92) : new Color(226, 230, 238);
        regCard.setBorderColor(bd); unregCard.setBorderColor(bd);
        regTitle.setColor(dark ? Color.WHITE : new Color(15, 23, 42));
    }

    /**
     * Updates the section with fresh data from the statistics service.
     * Stores the registered and unregistered student lists and re-renders both columns.
     *
     * @param s     the {@link StatisticsService.StatsSummary} (may be used for additional context)
     * @param reg   the list of registered {@link User}s for this campaign
     * @param unreg the list of unregistered {@link User}s who still need to register
     */
    public void update(StatisticsService.StatsSummary s, List<User> reg, List<User> unreg) {
        registeredStudents = new ArrayList<>(reg);
        unregisteredStudents = new ArrayList<>(unreg);
        renderList(regList, regScroll, registeredStudents, true);
        renderList(unregList, unregScroll, unregisteredStudents, false);
    }

    private SurfaceCard studentCard(User u, int w, boolean isUnreg) {
        SurfaceCard card = new SurfaceCard(0, 0, w, 56, new Color(30, 40, 58), new Color(52, 63, 92), 8);
        card.setCursor(12);
        String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getLogin();
        Label ln = new Label(name, 16, 10, w - 30, 20);
        ln.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        ln.setColor(darkMode ? new Color(239, 244, 252) : new Color(15, 23, 42));
        Label li = new Label("@" + u.getLogin(), 16, 32, w - 30, 16);
        li.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        li.setColor(darkMode ? new Color(132, 144, 168) : new Color(100, 116, 139));
        
        card.addChild(ln); card.addChild(li);
        card.getEventManager().register(UiEvent.Type.POINTER_UP, (c, e) -> onSelectStudent.accept(u));
        return card;
    }

    private void renderList(BaseComp list, ScrollView parentScroll, List<User> users, boolean registered) {
        clearChildren(list);
        if (users.isEmpty()) {
            Label l = new Label("Aucun étudiant", 16, 16, 200, 20);
            l.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 13));
            l.setColor(new Color(100, 116, 139));
            list.addChild(l);
            parentScroll.setContentHeight(80);
            return;
        }
        int w = list.getWidth() - 16;
        int y = 0;
        for (User u : users) {
            SurfaceCard card = studentCard(u, w, !registered);
            card.setBounds(8, y, w, 56);
            list.addChild(card);
            y += 66; // Espace entre les cartes
        }
        parentScroll.setContentHeight(Math.max(parentScroll.getHeight(), y + 20));
    }

    private void filterStudents() {
        String q = search.getCurrentText() == null ? "" : search.getCurrentText().toLowerCase();
        List<User> filtered = new ArrayList<>();
        for (User u : unregisteredStudents) {
            String n = (u.getFullName() != null ? u.getFullName() : "").toLowerCase();
            String l = (u.getLogin() != null ? u.getLogin() : "").toLowerCase();
            if (n.contains(q) || l.contains(q)) filtered.add(u);
        }
        renderList(unregList, unregScroll, filtered, false);
    }

    /**
     * Adjusts the layout when the parent container is resized. Splits available
     * width into two equal columns with a gap, positions the two column cards,
     * and re-renders both student lists.
     *
     * @param w the new width in pixels
     * @param h the new height in pixels
     */
    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        int gap = 24;
        int halfW = (w - gap) / 2;
        
        regCard.setBounds(0, 0, halfW, h);
        unregCard.setBounds(halfW + gap, 0, halfW, h);
        
        regScroll.setBounds(0, 50, halfW, h - 60);
        regList.setBounds(0, 0, halfW, Math.max(h - 60, 200));

        search.setBounds(16, 48, halfW - 32, 36);
        unregScroll.setBounds(0, 94, halfW, h - 104);
        unregList.setBounds(0, 0, halfW, Math.max(h - 104, 200));
        
        renderList(regList, regScroll, registeredStudents, true);
        filterStudents();
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
}