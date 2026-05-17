package gui.screen.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import components.Button;
import components.Label;
import components.ScrollView;
import gui.components.DominanteCardAdmin;
import gui.components.SurfaceCard;
import main.BaseComp;
import main.BaseWindow;
import model.Dominante;
import service.DominanteService;
import service.ServiceResult;
import gui.screen.AdminDashboardView;

/**
 * Composant de liste et gestion des dominantes .
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
    private final BaseWindow window;
    private final AdminDashboardView view;
    private final SurfaceCard backgroundCard;
    private final ScrollView dominantesScroll;
    private final BaseComp dominantesList;
    private final Button createButton;
    private Consumer<Dominante> onEdit = d -> {};
    private Runnable onCreate = () -> {};
    private boolean darkMode = true;

public DominanteListComponent(BaseWindow window, DominanteService dominanteService, AdminDashboardView view) {
        this.dominanteService = dominanteService;
        this.window = window;
        this.view = view;
        this.backgroundCard = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        this.dominantesScroll = new ScrollView(0, 0, 100, 100);
        this.dominantesList = dominantesScroll.getContent();
        
        this.createButton = new Button("+ Nouvelle dominante", 0, 0, 210, 32, () -> onCreate.run());
        backgroundCard.addChild(createButton);
        backgroundCard.addChild(dominantesScroll);
    }

    public BaseComp getRoot() { return backgroundCard; }
    public void onEdit(Consumer<Dominante> cb) { this.onEdit = cb; }
    public void onCreate(Runnable cb) { this.onCreate = cb; }

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
            createButton.setBackground(new Color(240, 243, 248));
            createButton.setForeground(new Color(67, 76, 91));
        }
        backgroundCard.invalidate();
    }

    public void refresh() {
        List<Dominante> list = dominanteService.listAll();
        clearChildren(dominantesList);
        int idx = 0;
        int gap = 12;
        int containerWidth = Math.max(300, dominantesScroll.getWidth());
        int cardW = (containerWidth - gap) / 2;
        int cardH = 220;

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune dominante. Cliquez + Nouvelle dominante.", 0, 8, Math.max(260, dominantesScroll.getWidth() - 16), 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            empty.setColor(darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139));
            dominantesList.addChild(empty);
            dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), 64));
            return;
        }

        for (Dominante d : list) {
            final Dominante finalD = d;
            DominanteCardAdmin card = new DominanteCardAdmin(() -> onEdit.accept(finalD), () -> {
                // Show confirmation modal before deletion
                view.showConfirmDeleteModal("Êtes-vous sûr de vouloir supprimer cette dominante ? Cette action est irréversible.", () -> {
                    // Call the actual delete service
                    ServiceResult result = dominanteService.deleteById(finalD.getId());
                    if (result.isSuccess()) {
                        refresh(); // Refresh the list after successful deletion
                    } else {
                        // Show error message - for now just print to console
                        System.err.println("Error deleting dominante: " + result.getMessage());
                    }
                });
            }, window);
            card.setDarkMode(darkMode);
            Color accent = parseDominanteColor(d.getColor());
            int x = (idx % 2) * (cardW + gap);
            int y = (idx / 2) * (cardH + gap);
            card.setBounds(x, y, cardW, cardH);
            card.setData(d.getCode(), d.getName(), d.getDescription(), 0, 0, 0, 0, accent);
            dominantesList.addChild(card);
            idx++;
        }

        int rows = (int) Math.ceil(Math.max(1, idx) / 2.0);
        int contentHeight = rows * (cardH + gap) + 8;
        dominantesScroll.setContentHeight(Math.max(dominantesScroll.getHeight(), contentHeight));
        dominantesScroll.setContentWidth(Math.max(dominantesScroll.getWidth(), containerWidth));
    }

    public void onResize(int mainW, int mainH) {
        backgroundCard.setBounds(0, 0, mainW, mainH);
        createButton.setBounds(mainW - 220, 0, 220, 32);
        dominantesScroll.setBounds(0, 44, mainW, Math.max(220, mainH - 44));
    }

    private void clearChildren(BaseComp parent) { ArrayList<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (BaseComp c : snapshot) parent.removeChild(c); }
    private Color parseDominanteColor(String hex) {
        if (hex == null || hex.isBlank()) return new Color(124, 92, 255);
        try { return Color.decode(hex.startsWith("#") ? hex : "#" + hex); }
        catch (Exception e) { return new Color(124, 92, 255); }
    }
}
