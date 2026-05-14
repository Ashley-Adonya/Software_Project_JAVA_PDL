package gui.screen.components;

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
import gui.components.SessionRowAdmin;
import main.BaseComp;
import main.BaseWindow;
import model.Dominante;
import model.SessionSlot;
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

    private final BaseComp root;
    private final ScrollView sessionsScroll;
    private final BaseComp sessionsList;
    private final SelectInput dominanteFilter;

    private Consumer<SessionSlot> onEditSession = s -> {};
    private Consumer<SessionSlot> onManageSession = s -> {};
    private Runnable onCreateSession = () -> {};

    private int currentCampaignId = -1;

    public SessionListComponent(BaseWindow window, SessionService sessionService, DominanteService dominanteService) {
        this.sessionService = sessionService;
        this.dominanteService = dominanteService;

        this.root = new BaseComp(null);
        this.sessionsScroll = new ScrollView(0, 0, 100, 100);
        this.sessionsList = sessionsScroll.getContent();
        this.dominanteFilter = new SelectInput(12, 28, 280, 30);

        Button createBtn = new Button("+ Nouvelle session", 0, 0, 170, 32, () -> onCreateSession.run());
        createBtn.setBackground(new java.awt.Color(12, 16, 44));
        root.addChild(createBtn);
        root.addChild(dominanteFilter);
        root.addChild(sessionsScroll);
    }

    public BaseComp getRoot() { return root; }

    public void setOnEditSession(Consumer<SessionSlot> cb) { this.onEditSession = cb; }
    public void setOnManageSession(Consumer<SessionSlot> cb) { this.onManageSession = cb; }
    public void setOnCreateSession(Runnable cb) { this.onCreateSession = cb; }

    public void setActiveCampaignId(int campaignId) { this.currentCampaignId = campaignId; }

    public void refresh() {
        if (currentCampaignId <= 0) {
            clearChildren(sessionsList);
            Label empty = new Label("Aucune campagne active.", 0, 8, 200, 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
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

            SessionRowAdmin row = new SessionRowAdmin(() -> onEditSession.accept(s), () -> sessionService.deleteSession(s.getId()));
            row.setBounds(0, y, sessionsScroll.getWidth() - 12, 66);
            row.setData(dominanteName + " - " + safe(s.getTitle()), " ", 0, new java.awt.Color(107,114,128));
            row.setOnManage(() -> onManageSession.accept(s));
            sessionsList.addChild(row);
            y += 76;
        }

        sessionsScroll.setContentHeight(Math.max(sessionsScroll.getHeight(), y + 10));
    }

    public void onResize(int width, int height) {
        int mainW = width;
        // basic layout
        // place create button and filter
        BaseComp create = root.getChildrenList().get(0);
        create.setBounds(mainW - 188, 0, 178, 32);
        dominanteFilter.setBounds(12, 28, 280, 30);
        sessionsScroll.setBounds(0, 126, mainW, Math.max(220, height - 126));
    }

    // util
    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp c : snapshot) parent.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}
