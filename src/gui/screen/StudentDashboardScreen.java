package gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.Label;
import gui.components.PageHeader;
import gui.components.SessionOfferCard;
import gui.components.SidebarMenu;
import gui.components.SurfaceCard;
import gui.navigation.AppScreen;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import model.Choice;
import model.SessionSlot;
import model.User;
import service.CampaignService;
import service.ChoiceService;
import service.SessionService;

public class StudentDashboardScreen implements AppScreen {
    private static final Color PAGE_BG = new Color(14, 18, 26);

    private enum Section {
        SEARCH,
        INSCRIPTIONS
    }

    private final BaseWindow window;
    private final User user;
    private final Runnable onLogout;

    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final ChoiceService choiceService;

    private final SidebarMenu sidebar;
    private final PageHeader header;
    private final SurfaceCard selectionsCard;
    private final Label selectionsTitle;
    private final Label selectionsCount;
    private final Label selection1;
    private final Label selection2;
    private final Label selection3;

    private final List<SessionOfferCard> offerCards;

    private Campaign currentCampaign;
    private Section activeSection;

    public StudentDashboardScreen(BaseWindow window, User user, Runnable onLogout) {
        this.window = window;
        this.user = user;
        this.onLogout = onLogout;

        this.campaignService = new CampaignService();
        this.sessionService = new SessionService();
        this.choiceService = new ChoiceService();

        List<SidebarMenu.Item> items = new ArrayList<>();
        items.add(new SidebarMenu.Item("search", "Rechercher"));
        items.add(new SidebarMenu.Item("inscriptions", "Mes inscriptions"));
        this.sidebar = new SidebarMenu("Etudiant", resolveDisplayName(user), items, "search", this::onSidebarSelect, onLogout, () -> {});

        this.header = new PageHeader("Sessions disponibles", "Choisissez vos dominantes preferees");
        this.header.setDarkMode(true);
        this.sidebar.setDarkMode(true);

        this.offerCards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            offerCards.add(new SessionOfferCard("Session", "", "", "", () -> System.out.println("Ajout choix")));
        }

        this.selectionsCard = new SurfaceCard(0, 0, 320, 170, Color.WHITE, new Color(226, 230, 238), 12);
        this.selectionsTitle = new Label("Mes selections", 0, 0, 200, 22);
        this.selectionsCount = new Label("0 / 0 choix", 0, 0, 160, 18);
        this.selection1 = new Label("", 0, 0, 280, 18);
        this.selection2 = new Label("", 0, 0, 280, 18);
        this.selection3 = new Label("", 0, 0, 280, 18);

        selectionsCard.addChild(selectionsTitle);
        selectionsCard.addChild(selectionsCount);
        selectionsCard.addChild(selection1);
        selectionsCard.addChild(selection2);
        selectionsCard.addChild(selection3);

        this.activeSection = Section.SEARCH;
    }

    @Override
    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);

        content.addChild(sidebar);
        content.addChild(header);
        for (SessionOfferCard offer : offerCards) {
            content.addChild(offer);
        }
        content.addChild(selectionsCard);

        refreshData();
        renderSection();
        onResize();
    }

    @Override
    public void onResize() {
        BaseComp content = window.getContent();
        int w = content.getWidth();
        int h = content.getHeight();

        int sideW = Math.max(200, Math.min(240, w / 4));
        sidebar.setBounds(0, 0, sideW, h);

        int mainX = sideW + 18;
        int mainW = Math.max(320, w - mainX - 18);

        header.setBounds(mainX, 18, mainW, 52);

        int cardY = 84;
        if (activeSection == Section.SEARCH) {
            for (SessionOfferCard offer : offerCards) {
                offer.setBounds(mainX, cardY, mainW, 92);
                cardY += 106;
            }
        }

        int selectionY = activeSection == Section.SEARCH ? cardY : 84;
        selectionsCard.setBounds(mainX, selectionY, mainW, 160);
        selectionsTitle.setBounds(16, 14, mainW - 32, 24);
        selectionsCount.setBounds(16, 38, mainW - 32, 18);
        selection1.setBounds(16, 70, mainW - 32, 18);
        selection2.setBounds(16, 94, mainW - 32, 18);
        selection3.setBounds(16, 118, mainW - 32, 18);

        window.invalidateAll();
        window.requestRenderIfNeeded();
    }

    private void onSidebarSelect(String key) {
        if ("inscriptions".equals(key)) {
            activeSection = Section.INSCRIPTIONS;
        } else {
            activeSection = Section.SEARCH;
        }
        renderSection();
        onResize();
    }

    private void renderSection() {
        boolean searchMode = activeSection == Section.SEARCH;
        for (SessionOfferCard offer : offerCards) {
            offer.setVisible(searchMode);
        }
        selectionsCard.setVisible(true);
        if (searchMode) {
            header.setSubtitle("Choisissez vos dominantes preferees");
            selectionsTitle.setText("Mes selections");
        } else {
            header.setSubtitle("Retrouvez vos choix et attributions");
            selectionsTitle.setText("Mes inscriptions");
        }
    }

    private void refreshData() {
        currentCampaign = resolveOpenCampaignForStudent();
        if (currentCampaign == null) {
            header.setSubtitle("Aucune campagne ouverte pour votre promo");
            for (SessionOfferCard offer : offerCards) {
                offer.setData("Aucune session", "", "", "");
            }
            selectionsCount.setText("0 / 0 choix");
            selection1.setText("");
            selection2.setText("");
            selection3.setText("");
            return;
        }

        List<SessionSlot> sessions = sessionService.listByCampaign(currentCampaign.getId());
        for (int i = 0; i < offerCards.size(); i++) {
            if (i < sessions.size()) {
                SessionSlot s = sessions.get(i);
                offerCards.get(i).setData(
                        safe(s.getTitle()),
                        safe(s.getSessionDate()) + " - " + formatMinute(s.getStartMinute()) + " / " + formatMinute(s.getEndMinute()),
                        "Salle " + safe(s.getRoom()),
                        s.getCapacity() + " places");
            } else {
                offerCards.get(i).setData("Session", "", "", "");
            }
        }

        List<Choice> choices = choiceService.getStudentChoices(currentCampaign.getId(), user.getId());
        Map<Integer, String> bySession = new HashMap<>();
        for (SessionSlot session : sessions) {
            bySession.put(Integer.valueOf(session.getId()), session.getTitle());
        }

        selectionsCount.setText(choices.size() + " / " + currentCampaign.getMaxChoices() + " choix");
        selection1.setText(choiceLine(choices, 0, bySession));
        selection2.setText(choiceLine(choices, 1, bySession));
        selection3.setText(choiceLine(choices, 2, bySession));
    }

    private Campaign resolveOpenCampaignForStudent() {
        if (user.getPromo() == null || user.getPromo().isBlank()) {
            return null;
        }
        List<Campaign> campaigns = campaignService.getCampaignsByPromo(user.getPromo());
        for (Campaign c : campaigns) {
            if ("OPEN".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }

    private String choiceLine(List<Choice> choices, int index, Map<Integer, String> bySession) {
        if (index >= choices.size()) {
            return "";
        }
        Choice c = choices.get(index);
        String name = bySession.getOrDefault(Integer.valueOf(c.getSessionId()), "Session #" + c.getSessionId());
        return c.getRankOrder() + ". " + name;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", Integer.valueOf(h), Integer.valueOf(m));
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getLogin() == null ? "" : user.getLogin();
    }

    private void clearChildren(BaseComp parent) {
        List<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList());
        for (BaseComp child : snapshot) {
            parent.removeChild(child);
        }
    }
}
