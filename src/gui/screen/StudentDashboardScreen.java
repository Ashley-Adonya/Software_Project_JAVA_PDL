package gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import components.Label;
import components.ScrollView;
import gui.components.PageHeader;
import gui.components.SearchField;
import gui.components.SessionOfferCard;
import gui.components.SidebarMenu;
import gui.components.SurfaceCard;
import gui.components.PrimaryButton;
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
import service.ServiceResult;

public class StudentDashboardScreen implements AppScreen {
    private static final Color PAGE_BG = new Color(14, 18, 26);
    private static final Color MUTED_TEXT = new Color(103, 113, 131);
    private static final Color DANGER = new Color(170, 52, 52);
    private static final Color SUCCESS = new Color(32, 140, 92);
    private static final Color ACTION_ADD_BG = new Color(127, 132, 146);
    private static final Color ACTION_REMOVE_BG = new Color(170, 74, 74);

    private enum Section {
        SEARCH,
        INSCRIPTIONS
    }

    private final BaseWindow window;
    private final User user;
    // private final Runnable onLogout;

    private final CampaignService campaignService;
    private final SessionService sessionService;
    private final ChoiceService choiceService;

    private final SidebarMenu sidebar;
    private final PageHeader header;
    private final PrimaryButton refreshButton;

    private final SearchField sessionSearchField;
    private final ScrollView offersScroll;
    private final BaseComp offersContent;
    private final SurfaceCard selectionsCard;
    private final Label selectionsTitle;
    private final Label selectionsCount;
    private final Label actionFeedback;
    private final Label selection1;
    private final Label selection2;
    private final Label selection3;

    private final PrimaryButton up1;
    private final PrimaryButton down1;
    private final PrimaryButton remove1;
    private final PrimaryButton up2;
    private final PrimaryButton down2;
    private final PrimaryButton remove2;
    private final PrimaryButton up3;
    private final PrimaryButton down3;
    private final PrimaryButton remove3;

    private final List<SessionOfferCard> offerCards;
    private final Map<Integer, SessionOfferCard> offerCardBySessionId;

    private List<SessionSlot> currentSessions;
    private List<Choice> currentChoices;
    private boolean savingChoices;

    private Campaign currentCampaign;
    private Section activeSection;

    public StudentDashboardScreen(BaseWindow window, User user, Runnable onLogout) {
        this.window = window;
        this.user = user;
        // this.onLogout = onLogout;

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
        this.refreshButton = new PrimaryButton("Actualiser", 0, 0, 110, 28, this::refreshCurrentData);
        this.refreshButton.setBackground(new Color(44, 54, 76));

        this.sessionSearchField = new SearchField(0, 0, 320, 32, "Filtrer par dominante ou session");
        this.sessionSearchField.setColors(new Color(28, 36, 50), new Color(48, 60, 82), new Color(82, 107, 255), new Color(239, 244, 252), new Color(132, 144, 168));
        this.sessionSearchField.setOnChange(this::rebuildOffersCards);

        this.offersScroll = new ScrollView(0, 0, 100, 100);
        this.offersContent = offersScroll.getContent();

        this.offerCards = new ArrayList<>();
        this.offerCardBySessionId = new HashMap<>();
        this.currentSessions = new ArrayList<>();
        this.currentChoices = new ArrayList<>();

        this.selectionsCard = new SurfaceCard(0, 0, 320, 170, Color.WHITE, new Color(226, 230, 238), 12);
        this.selectionsTitle = new Label("Mes selections", 0, 0, 200, 22);
        this.selectionsCount = new Label("0 / 0 choix", 0, 0, 160, 18);
        this.selectionsCount.setColor(MUTED_TEXT);
        this.actionFeedback = new Label("", 0, 0, 280, 18);
        this.actionFeedback.setColor(MUTED_TEXT);
        this.selection1 = new Label("", 0, 0, 280, 18);
        this.selection2 = new Label("", 0, 0, 280, 18);
        this.selection3 = new Label("", 0, 0, 280, 18);

        this.up1 = smallActionButton("^", () -> moveChoice(0, -1));
        this.down1 = smallActionButton("v", () -> moveChoice(0, +1));
        this.remove1 = smallActionButton("X", () -> removeChoiceAt(0));
        this.up2 = smallActionButton("^", () -> moveChoice(1, -1));
        this.down2 = smallActionButton("v", () -> moveChoice(1, +1));
        this.remove2 = smallActionButton("X", () -> removeChoiceAt(1));
        this.up3 = smallActionButton("^", () -> moveChoice(2, -1));
        this.down3 = smallActionButton("v", () -> moveChoice(2, +1));
        this.remove3 = smallActionButton("X", () -> removeChoiceAt(2));

        selectionsCard.addChild(selectionsTitle);
        selectionsCard.addChild(selectionsCount);
        selectionsCard.addChild(actionFeedback);
        selectionsCard.addChild(selection1);
        selectionsCard.addChild(selection2);
        selectionsCard.addChild(selection3);

        selectionsCard.addChild(up1);
        selectionsCard.addChild(down1);
        selectionsCard.addChild(remove1);
        selectionsCard.addChild(up2);
        selectionsCard.addChild(down2);
        selectionsCard.addChild(remove2);
        selectionsCard.addChild(up3);
        selectionsCard.addChild(down3);
        selectionsCard.addChild(remove3);
        this.activeSection = Section.SEARCH;
    }

    @Override
    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));
        clearChildren(content);

        content.addChild(sidebar);
        content.addChild(header);
        content.addChild(refreshButton);
        content.addChild(sessionSearchField);
        content.addChild(offersScroll);
        content.addChild(selectionsCard;

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
        refreshButton.setBounds(mainX + mainW - 110, 24, 110, 28);

        int topY = 84;
        int bottomMargin = 18;
        int gap = 14;
        int selectionH = 160;

        if (activeSection == Section.SEARCH) {
            int searchY = topY;
            int searchH = 32;
            int selectionY = h - bottomMargin - selectionH;
            int scrollY = searchY + searchH + gap;
            int scrollH = Math.max(200, selectionY - scrollY - gap);
            if (scrollH < 140) {
                scrollH = Math.max(140, h - scrollY - bottomMargin - selectionH);
                selectionY = scrollY + scrollH + gap;
            }

            sessionSearchField.setVisible(true);
            sessionSearchField.setBounds(mainX, searchY, Math.min(420, mainW), searchH);
            offersScroll.setVisible(true);
            offersScroll.setBounds(mainX, scrollY, mainW, scrollH);
            relayoutOffersList(mainW);

            selectionsCard.setBounds(mainX, selectionY, mainW, selectionH);
        } else {
            sessionSearchField.setVisible(false);
            offersScroll.setVisible(false);
            selectionsCard.setBounds(mainX, topY, mainW, 160);
        }
        selectionsTitle.setBounds(16, 14, mainW - 32, 24);
        selectionsCount.setBounds(16, 38, mainW - 32, 18);
        actionFeedback.setBounds(16, 56, mainW - 32, 18);

        int btnW = 26;
        int btnH = 20;
        int btnGap = 6;
        int controlsW = btnW * 3 + btnGap * 2;
        int labelW = Math.max(120, mainW - 32 - controlsW - 8);
        int rightX = mainW - 16;

        selection1.setBounds(16, 78, labelW, 18);
        selection2.setBounds(16, 102, labelW, 18);
        selection3.setBounds(16, 126, labelW, 18);

        layoutRowControls(0, rightX, 76, btnW, btnH, btnGap);
        layoutRowControls(1, rightX, 100, btnW, btnH, btnGap);
        layoutRowControls(2, rightX, 124, btnW, btnH, btnGap);

        updateChoiceControlsVisibility();

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
        sessionSearchField.setVisible(searchMode);
        selectionsCard.setVisible(true);
        setEditingControlsVisible(searchMode);
        if (searchMode) {
            header.setSubtitle("Choisissez vos dominantes preferees");
            selectionsTitle.setText("Mes selections");
        } else {
            header.setSubtitle("Retrouvez vos choix et attributions");
            selectionsTitle.setText("Mes inscriptions");
        }
    }

    private void setEditingControlsVisible(boolean visible) {
        actionFeedback.setVisible(visible);
        if (!visible) {
            up1.setVisible(false);
            down1.setVisible(false);
            remove1.setVisible(false);
            up2.setVisible(false);
            down2.setVisible(false);
            remove2.setVisible(false);
            up3.setVisible(false);
            down3.setVisible(false);
            remove3.setVisible(false);
            return;
        }
        updateChoiceControlsVisibility();
    }

    private void refreshCurrentData() {
        refreshData();
        renderSection();
        onResize();
    }
    private void refreshData() {
        currentCampaign = resolveOpenCampaignForStudent();
        if (currentCampaign == null) {
            header.setSubtitle("Aucune campagne ouverte pour votre promo");
            clearChildren(offersContent);
            offerCards.clear();
            offerCardBySessionId.clear();
            currentSessions = new ArrayList<>();
            currentChoices = new ArrayList<>();
            selectionsCount.setText("0 / 0 choix");
            actionFeedback.setText("");
            selection1.setText("");
            selection2.setText("");
            selection3.setText("");
            updateChoiceControlsVisibility();
            return;
        }

        List<SessionSlot> sessions = sessionService.listByCampaign(currentCampaign.getId());
        this.currentSessions = sessions == null ? new ArrayList<>() : sessions;

        rebuildOffersCards();

        List<Choice> choices = choiceService.getStudentChoices(currentCampaign.getId(), user.getId());
        this.currentChoices = choices == null ? new ArrayList<>() : choices;
        Map<Integer, String> bySession = new HashMap<>();
        for (SessionSlot session : currentSessions) {
            bySession.put(Integer.valueOf(session.getId()), session.getTitle());
        }

        selectionsCount.setText(currentChoices.size() + " / " + currentCampaign.getMaxChoices() + " choix");
        selection1.setText(choiceLine(currentChoices, 0, bySession));
        selection2.setText(choiceLine(currentChoices, 1, bySession));
        selection3.setText(choiceLine(currentChoices, 2, bySession));

        updateOfferCardActions();
        updateChoiceControlsVisibility();
    }

    private void rebuildOffersCards() {
        clearChildren(offersContent);
        offerCards.clear();
        offerCardBySessionId.clear();

        if (currentSessions == null || currentSessions.isEmpty()) {
            SessionOfferCard empty = new SessionOfferCard("Aucune session", "", "", "", () -> {});
            offerCards.add(empty);
            offersContent.addChild(empty);
            return;
        }

        String query = sessionSearchField.getText() == null ? "" : sessionSearchField.getText().trim().toLowerCase();
        Map<Integer, String> dominantNames = resolveDominantNames();

        for (SessionSlot s : currentSessions) {
            String dominantName = dominantNames.getOrDefault(Integer.valueOf(s.getDominanteId()), "Dominante #" + s.getDominanteId());
            String haystack = (safe(s.getTitle()) + " " + safe(s.getRoom()) + " " + dominantName).toLowerCase();
            if (!query.isEmpty() && !haystack.contains(query)) {
                continue;
            }
            SessionOfferCard card = new SessionOfferCard(
                    safe(s.getTitle()),
                    dominantName + " • " + safe(s.getSessionDate()) + " - " + formatMinute(s.getStartMinute()) + " / " + formatMinute(s.getEndMinute()),
                    "Salle " + safe(s.getRoom()),
                    s.getCapacity() + " places",
                    () -> onToggleChoice(s));
            card.setActionBackground(ACTION_ADD_BG);
            offerCards.add(card);
            offerCardBySessionId.put(Integer.valueOf(s.getId()), card);
            offersContent.addChild(card);
        }

        if (offerCards.isEmpty()) {
            SessionOfferCard empty = new SessionOfferCard("Aucun résultat", "Affinez votre recherche", "", "", () -> {});
            empty.setActionBackground(ACTION_ADD_BG);
            offerCards.add(empty);
            offersContent.addChild(empty);
        }
    }

    private void relayoutOffersList(int mainW) {
        int y = 0;
        int gap = 12;
        int cardH = 92;
        int cardW = Math.max(280, offersScroll.getWidth());

        for (SessionOfferCard offer : offerCards) {
            offer.setBounds(0, y, cardW, cardH);
            y += cardH + gap;
        }

        offersScroll.setContentHeight(Math.max(offersScroll.getHeight(), y));
        offersScroll.setContentWidth(Math.max(offersScroll.getWidth(), mainW));
    }

    private void onToggleChoice(SessionSlot session) {
        if (session == null || currentCampaign == null) {
            return;
        }
        if (savingChoices) {
            return;
        }

        List<Integer> ids = getChoiceSessionIdsInOrder();
        int sessionId = session.getId();
        boolean alreadySelected = ids.contains(Integer.valueOf(sessionId));
        if (alreadySelected) {
            ids.remove(Integer.valueOf(sessionId));
            saveChoicesAsync(ids);
            return;
        }

        if (ids.size() >= currentCampaign.getMaxChoices()) {
            setFeedback(DANGER, "Maximum de choix atteint");
            return;
        }
        ids.add(Integer.valueOf(sessionId));
        saveChoicesAsync(ids);
    }

    private void removeChoiceAt(int index) {
        if (currentCampaign == null || savingChoices) {
            return;
        }
        List<Integer> ids = getChoiceSessionIdsInOrder();
        if (index < 0 || index >= ids.size()) {
            return;
        }
        ids.remove(index);
        saveChoicesAsync(ids);
    }

    private void moveChoice(int index, int delta) {
        if (currentCampaign == null || savingChoices) {
            return;
        }
        List<Integer> ids = getChoiceSessionIdsInOrder();
        int j = index + delta;
        if (index < 0 || index >= ids.size() || j < 0 || j >= ids.size()) {
            return;
        }
        Integer tmp = ids.get(index);
        ids.set(index, ids.get(j));
        ids.set(j, tmp);
        saveChoicesAsync(ids);
    }

    private void saveChoicesAsync(List<Integer> sessionIdsInOrder) {
        if (currentCampaign == null) {
            return;
        }
        if (savingChoices) {
            return;
        }
        savingChoices = true;
        setFeedback(MUTED_TEXT, "Enregistrement...");

        final int campaignId = currentCampaign.getId();
        final int studentId = user.getId();

        final List<Choice> newChoices = new ArrayList<>();
        if (sessionIdsInOrder != null) {
            for (int i = 0; i < sessionIdsInOrder.size(); i++) {
                int sessionId = sessionIdsInOrder.get(i).intValue();
                newChoices.add(new Choice(0, campaignId, studentId, sessionId, i + 1));
            }
        }

        new Thread(() -> {
            ServiceResult result;
            try {
                result = choiceService.replaceStudentChoices(campaignId, studentId, newChoices);
            } catch (Exception e) {
                result = ServiceResult.fail("Erreur: " + e.getMessage());
            }

            List<Choice> refreshed;
            try {
                refreshed = choiceService.getStudentChoices(campaignId, studentId);
            } catch (Exception e) {
                refreshed = new ArrayList<>();
            }

            final ServiceResult finalResult = result;
            final List<Choice> finalChoices = refreshed == null ? new ArrayList<>() : refreshed;
            SwingUtilities.invokeLater(() -> {
                savingChoices = false;
                currentChoices = finalChoices;
                selectionsCount.setText(currentChoices.size() + " / " + currentCampaign.getMaxChoices() + " choix");

                Map<Integer, String> bySession = new HashMap<>();
                for (SessionSlot s : currentSessions) {
                    bySession.put(Integer.valueOf(s.getId()), s.getTitle());
                }
                selection1.setText(choiceLine(currentChoices, 0, bySession));
                selection2.setText(choiceLine(currentChoices, 1, bySession));
                selection3.setText(choiceLine(currentChoices, 2, bySession));
                updateOfferCardActions();
                updateChoiceControlsVisibility();

                if (finalResult != null && finalResult.isSuccess()) {
                    setFeedback(SUCCESS, finalResult.getMessage());
                } else {
                    setFeedback(DANGER, finalResult == null ? "Erreur" : finalResult.getMessage());
                }

                window.invalidateAll();
                window.requestRenderIfNeeded();
            });
        }).start();
    }

    private void updateOfferCardActions() {
        Set<Integer> selected = new HashSet<>();
        for (Choice c : currentChoices) {
            selected.add(Integer.valueOf(c.getSessionId()));
        }
        for (SessionSlot s : currentSessions) {
            SessionOfferCard card = offerCardBySessionId.get(Integer.valueOf(s.getId()));
            if (card == null) {
                continue;
            }
            boolean isSelected = selected.contains(Integer.valueOf(s.getId()));
            if (isSelected) {
                card.setActionText("Retirer");
                card.setActionBackground(ACTION_REMOVE_BG);
            } else {
                card.setActionText("Ajouter");
                card.setActionBackground(ACTION_ADD_BG);
            }
        }
    }

    private Map<Integer, String> resolveDominantNames() {
        Map<Integer, String> dominantNames = new HashMap<>();
        List<model.Dominante> dominantes = new service.DominanteService().listAll();
        for (model.Dominante dominante : dominantes) {
            dominantNames.put(Integer.valueOf(dominante.getId()), safe(dominante.getName()));
        }
        return dominantNames;
    }

    private List<Integer> getChoiceSessionIdsInOrder() {
        List<Integer> ids = new ArrayList<>();
        for (Choice c : currentChoices) {
            ids.add(Integer.valueOf(c.getSessionId()));
        }
        return ids;
    }

    private void setFeedback(Color color, String text) {
        if (color != null) {
            actionFeedback.setColor(color);
        }
        actionFeedback.setText(text == null ? "" : text);
    }

    private PrimaryButton smallActionButton(String text, Runnable onClick) {
        PrimaryButton b = new PrimaryButton(text, 0, 0, 26, 20, () -> {
            if (savingChoices) {
                return;
            }
            if (onClick != null) {
                onClick.run();
            }
        });
        b.setBackground(new Color(127, 132, 146));
        return b;
    }

    private void layoutRowControls(int rowIndex, int rightX, int y, int btnW, int btnH, int btnGap) {
        PrimaryButton up;
        PrimaryButton down;
        PrimaryButton remove;
        if (rowIndex == 0) {
            up = up1;
            down = down1;
            remove = remove1;
        } else if (rowIndex == 1) {
            up = up2;
            down = down2;
            remove = remove2;
        } else {
            up = up3;
            down = down3;
            remove = remove3;
        }

        int xRemove = rightX - btnW;
        int xDown = xRemove - btnGap - btnW;
        int xUp = xDown - btnGap - btnW;
        up.setBounds(xUp, y, btnW, btnH);
        down.setBounds(xDown, y, btnW, btnH);
        remove.setBounds(xRemove, y, btnW, btnH);
    }

    private void updateChoiceControlsVisibility() {
        int n = currentChoices == null ? 0 : currentChoices.size();

        setRowControlsVisible(0, n);
        setRowControlsVisible(1, n);
        setRowControlsVisible(2, n);
    }

    private void setRowControlsVisible(int rowIndex, int totalChoices) {
        PrimaryButton up;
        PrimaryButton down;
        PrimaryButton remove;
        if (rowIndex == 0) {
            up = up1;
            down = down1;
            remove = remove1;
        } else if (rowIndex == 1) {
            up = up2;
            down = down2;
            remove = remove2;
        } else {
            up = up3;
            down = down3;
            remove = remove3;
        }

        boolean hasRow = rowIndex < totalChoices;
        remove.setVisible(hasRow);
        up.setVisible(hasRow && rowIndex > 0);
        down.setVisible(hasRow && rowIndex < totalChoices - 1);
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
