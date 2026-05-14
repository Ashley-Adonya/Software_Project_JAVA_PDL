package gui.screen.components;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import components.Button;
import components.Label;
import components.ScrollView;
import gui.components.DominanteCardAdmin;
import main.BaseComp;
import main.BaseWindow;
import model.Dominante;
import service.DominanteService;

/**
 * Composant de liste et gestion des dominantes (domaines d'étude).
 * Affiche les dominantes existantes sous forme de cartes et permet création et édition.
 * 
 * Responsabilités :
 * - Rendu de la grille de dominantes en 2 colonnes
 * - Callbacks pour création et édition de dominantes
 * - Gestion de la pagination/scroll
 * - Gestion du redimensionnement
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class DominanteListComponent {
    private final DominanteService dominanteService;
    private final BaseComp root;
    private final ScrollView dominantesScroll;
    private final BaseComp dominantesList;
    private Consumer<Dominante> onEdit = d -> {};
    private Runnable onCreate = () -> {};

    public DominanteListComponent(BaseWindow window, DominanteService dominanteService) {
        this.dominanteService = dominanteService;
        this.root = new BaseComp(null);
        this.dominantesScroll = new ScrollView(0, 0, 100, 100);
        this.dominantesList = dominantesScroll.getContent();

        Button create = new Button("+ Nouvelle dominante", 0, 0, 210, 32, () -> onCreate.run());
        create.setBackground(new java.awt.Color(12, 16, 44));
        root.addChild(create);
        root.addChild(dominantesScroll);
    }

    public BaseComp getRoot() { return root; }
    public void onEdit(Consumer<Dominante> cb) { this.onEdit = cb; }
    public void onCreate(Runnable cb) { this.onCreate = cb; }

    public void refresh() {
        List<Dominante> list = dominanteService.listAll();
        clearChildren(dominantesList);
        int idx = 0;
        int gap = 12;
        int containerWidth = Math.max(300, dominantesScroll.getWidth());
        int cardW = (containerWidth - gap) / 2;
        int cardH = 220;

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune dominante. Utilisez le bouton + Nouvelle dominante.", 0, 8, Math.max(260, dominantesScroll.getWidth() - 16), 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            dominantesList.addChild(empty);
            dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), 64));
            return;
        }

        for (Dominante d : list) {
            DominanteCardAdmin card = new DominanteCardAdmin(() -> onEdit.accept(d), () -> {});
            int x = (idx % 2) * (cardW + gap);
            int y = (idx / 2) * (cardH + gap);
            card.setBounds(x, y, cardW, cardH);
            card.setData(d.getCode(), d.getName(), d.getDescription(), 0, 0, 0, 0, java.awt.Color.GRAY);
            dominantesList.addChild(card);
            idx++;
        }

        int rows = (int) Math.ceil(Math.max(1, idx) / 2.0);
        int contentHeight = rows * (cardH + gap) + 8;
        dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), contentHeight));
        dominantesScroll.setContentWidth(Math.max(dominantesScroll.getWidth(), containerWidth));
    }

    public void onResize(int mainW, int mainH) {
        BaseComp create = root.getChildrenList().get(0);
        create.setBounds(mainW - 220, 0, 220, 32);
        dominantesScroll.setBounds(0, 44, mainW, Math.max(220, mainH - 44));
    }

    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp c : snapshot) parent.removeChild(c); }
}
