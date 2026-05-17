package gui.screen.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import components.Button;
import components.Label;
import components.ScrollView;
import components.SelectInput;
import gui.components.SurfaceCard;
import gui.components.SessionRowAdmin;
import main.BaseComp;
import main.BaseWindow;
import model.Dominante;
import model.SessionSlot;
import service.ServiceResult;
import gui.screen.AdminDashboardView;
import service.DominanteService;
import service.SessionService;

/**
 * Composant de liste et gestion des sessions de présentation.
 * Affiche un tableau filtré des sessions par dominante et permet la création, édition,
 * et suppression de sessions avec rétroaction utilisateur.
 * 
 * Responsabilités :
 * - Rendu de la liste des sessions avec pagination/scroll
 * - Filtrage par dominante
 * - Callbacks pour création, édition, gestion et suppression
 * - Gestion du redimensionnement de la fenêtre
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class SessionListComponent {
    private final SessionService sessionService;
    private final DominanteService dominanteService;
    private final BaseWindow window;
    private final AdminDashboardView view;

    private final SurfaceCard backgroundCard;
    private final ScrollView sessionsScroll;
    private final BaseComp sessionsList;
    private final SelectInput dominanteFilter;
    private final Button createButton;

    private Consumer<SessionSlot> onEditSession = s -> {};
    private Consumer<SessionSlot> onManageSession = s -> {};
    private Runnable onCreateSession = () -> {};

    private int currentCampaignId = -1;
    private boolean darkMode = true;

    public SessionListComponent(BaseWindow window, SessionService sessionService, DominanteService dominanteService, AdminDashboardView view) {
        this.sessionService = sessionService;
        this.dominanteService = dominanteService;
        this.window = window;
        this.view = view;

        this.backgroundCard = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        this.sessionsScroll = new ScrollView(0, 0, 100, 100);
        this.sessionsList = sessionsScroll.getContent();
        this.dominanteFilter = new SelectInput(12, 28, 280, 30);

        this.createButton = new Button("+ Nouvelle session", 0, 0, 170, 32, () -> onCreateSession.run());
        backgroundCard.addChild(createButton);
        backgroundCard.addChild(dominanteFilter);
        backgroundCard.addChild(sessionsScroll);
    }

    public BaseComp getRoot() { return backgroundCard; }

    public void setOnEditSession(Consumer<SessionSlot> cb) { this.onEditSession = cb; }
    public void setOnManageSession(Consumer<SessionSlot> cb) { this.onManageSession = cb; }
    public void setOnCreateSession(Runnable cb) { this.onCreateSession = cb; }

    public void setActiveCampaignId(int campaignId) { this.currentCampaignId = campaignId; }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        applyDarkMode(dark);
    }

    private void applyDarkMode(boolean dark) {
        if (dark) {
            backgroundCard.setBackground(new Color(14, 18, 26));
            createButton.setBackground(new Color(30, 93, 57));
            createButton.setForeground(new Color(233, 247, 238));
        } else {
            backgroundCard.setBackground(Color.WHITE);
            createButton.setBackground(new Color(12, 16, 44));
            createButton.setForeground(Color.WHITE);
        }
        backgroundCard.invalidate();
    }

    public void refresh() {
        if (currentCampaignId <= 0) {
            clearChildren(sessionsList);
            Label empty = new Label("Aucune campagne active.", 0, 8, 200, 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            empty.setColor(darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139));
            sessionsList.addChild(empty);
            return;
        }

        List<Dominante> dominantes = dominanteService.listAll();
        List<String> options = new ArrayList<>();
        options.add("Toutes les dominantes");
        for (Dominante d : dominantes) options.add(d.getName());
        dominanteFilter.setOptions(options);

        List<SessionSlot> sessions = sessionService.listByCampaign(currentCampaignId);
        clearChildren(sessionsList);

        Map<Integer, Dominante> domById = new HashMap<>();
        for (Dominante d : dominantes) domById.put(d.getId(), d);

        String selected = dominanteFilter.getSelectedOption();
        int y = 0;
        for (SessionSlot s : sessions) {
            Dominante d = domById.get(s.getDominanteId());
            String dominanteName = d == null ? ("Dominante #" + s.getDominanteId()) : d.getName();
            if (!"Toutes les dominantes".equals(selected) && !selected.equals(dominanteName)) continue;

            SessionRowAdmin row = new SessionRowAdmin(() -> onEditSession.accept(s), () -> {
    ServiceResult result = sessionService.deleteSession(s.getId());
    if (result.isSuccess()) {
        refresh(); // Refresh the list after successful deletion
    } else {
        // Show error message - for now just print to console
        System.err.println("Error deleting session: " + result.getMessage());
    }
}, window);
            row.setDarkMode(darkMode);
            row.setBounds(0, y, sessionsScroll.getWidth() - 12, 66);
            int allocated = 0;
            int fillRate = 0;
            Color stripeColor = new Color(124, 92, 255);
            if (d != null && d.getColor() != null) {
                try { stripeColor = Color.decode(d.getColor().startsWith("#") ? d.getColor() : "#" + d.getColor()); } catch (Exception ex) { }
            }
            row.setData(dominanteName + " - " + safe(s.getTitle()), safe(s.getSessionDate()) + " | " + formatMinute(s.getStartMinute()) + "-" + formatMinute(s.getEndMinute()) + " | " + safe(s.getRoom()), fillRate, stripeColor);
            row.setOnManage(() -> onManageSession.accept(s));
            sessionsList.addChild(row);
            y += 76;
        }

        sessionsScroll.setContentHeight(Math.max(sessionsScroll.getHeight(), y + 10));
    }

    public void onResize(int width, int height) {
        int mainW = width;
        backgroundCard.setBounds(0, 0, mainW, height);
        createButton.setBounds(mainW - 188, 0, 178, 32);
        dominanteFilter.setBounds(12, 28, 280, 30);
        sessionsScroll.setBounds(0, 126, mainW, Math.max(220, height - 126));
    }

    private String formatMinute(int minute) {
        int h = minute / 60;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }

    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp c : snapshot) parent.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}
