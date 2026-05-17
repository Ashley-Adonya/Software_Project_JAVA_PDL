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
        container = new SurfaceCard(0, 0, 100, 100, new Color(22, 28, 39), new Color(52, 63, 92), 12);

        regCard = new SurfaceCard(0, 0, 100, 100, new Color(18, 24, 35), new Color(52, 63, 92), 10);
        regTitle = new Label("Inscrits", 12, 10, 140, 18);
        regTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        regTitle.setColor(new Color(239, 244, 252));
        regScroll = new ScrollView(8, 36, 100, 100);
        regList = regScroll.getContent();
        regCard.addChild(regTitle); regCard.addChild(regScroll);

        unregCard = new SurfaceCard(0, 0, 100, 100, new Color(18, 24, 35), new Color(52, 63, 92), 10);
        unregTitle = new Label("Non Inscrits", 12, 10, 140, 18);
        unregTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        unregTitle.setColor(new Color(239, 68, 68));
        search = new SearchField(12, 38, 200, 28, "Rechercher...");
        search.setColors(new Color(28, 36, 50), new Color(52, 63, 92), new Color(82, 107, 255), new Color(239, 244, 252), new Color(132, 144, 168));
        search.setOnChange(this::filterStudents);
        unregScroll = new ScrollView(8, 74, 100, 100);
        unregList = unregScroll.getContent();
        unregCard.addChild(unregTitle); unregCard.addChild(search); unregCard.addChild(unregScroll);

        container.addChild(regCard); container.addChild(unregCard);
    }

    public BaseComp getRoot() { return container; }
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }

    public void setDarkMode(boolean dark) {
        darkMode = dark;
        container.setBackground(dark ? new Color(22, 28, 39) : Color.WHITE);
        container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        regCard.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
        unregCard.setBackground(dark ? new Color(18, 24, 35) : Color.WHITE);
        container.invalidate();
    }

    public void update(StatisticsService.StatsSummary s, List<User> reg, List<User> unreg) {
        registeredStudents = new ArrayList<>(reg);
        unregisteredStudents = new ArrayList<>(unreg);
        renderList(regList, registeredStudents, true);
        renderList(unregList, unregisteredStudents, false);
    }

    private SurfaceCard studentCard(User u, int w) {
        SurfaceCard card = new SurfaceCard(0, 0, w, 36, new Color(40, 50, 70), new Color(60, 72, 100), 6);
        card.setCursor(12);
        String name = u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getLogin();
        Label ln = new Label(name, 10, 4, w - 70, 16);
        ln.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        ln.setColor(new Color(239, 244, 252));
        Label li = new Label("@" + u.getLogin(), 10, 20, w - 70, 12);
        li.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
        li.setColor(new Color(132, 144, 168));
        card.addChild(ln); card.addChild(li);
        card.getEventManager().register(UiEvent.Type.POINTER_UP, (c, e) -> onSelectStudent.accept(u));
        return card;
    }

    private void renderList(BaseComp list, List<User> users, boolean registered) {
        clearChildren(list);
        if (users.isEmpty()) {
            Label l = new Label("Aucun", 8, 8, 120, 20);
            l.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            l.setColor(new Color(100, 116, 139));
            list.addChild(l);
            return;
        }
        int w = list.getWidth() - 10;
        if (w < 50) w = 200;
        int y = 0;
        for (User u : users) {
            SurfaceCard card = studentCard(u, w);
            card.setBounds(4, y, w, 36);
            list.addChild(card);
            y += 40;
        }
    }

    private void filterStudents() {
        String q = search.getCurrentText() == null ? "" : search.getCurrentText().toLowerCase();
        List<User> filtered = new ArrayList<>();
        for (User u : unregisteredStudents) {
            String n = (u.getFullName() != null ? u.getFullName() : "").toLowerCase();
            String l = (u.getLogin() != null ? u.getLogin() : "").toLowerCase();
            if (n.contains(q) || l.contains(q)) filtered.add(u);
        }
        renderList(unregList, filtered, false);
    }

    public void onResize(int w, int h) {
        container.setBounds(0, 0, w, h);
        int half = (w - 16) / 2;
        int cardH = h - 16;
        regCard.setBounds(0, 8, half, cardH);
        unregCard.setBounds(half + 16, 8, w - half - 16, cardH);
        regScroll.setBounds(8, 36, half - 16, cardH - 44);
        unregScroll.setBounds(8, 74, w - half - 24, cardH - 82);
        regList.setBounds(0, 0, half - 24, Math.max(cardH - 44, 200));
        unregList.setBounds(0, 0, w - half - 32, Math.max(cardH - 82, 200));
    }

    private void clearChildren(BaseComp p) { for (main.BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
}