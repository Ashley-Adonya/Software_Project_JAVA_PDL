package gui.screen;

import java.awt.Color;
import gui.components.ColorPicker;
import gui.components.ReusableLabeledInput;
import components.Button;
import components.FormModal;
import components.Label;
import main.BaseComp;
import model.Dominante;
import service.DominanteService;
import service.ServiceResult;

/**
 * Manages modal dialogs for creating and editing study domains (dominantes).
 * <p>
 * Provides a centralised form for capturing the code, name, description,
 * responsible person, and an associated colour for each dominante. The colour
 * is selected via a {@link gui.components.ColorPicker}. Persistence is handled
 * through {@link service.DominanteService}, and upon success the current dashboard
 * section is automatically refreshed to reflect the changes.
 * </p>
 */
public class AdminDominanteModal {
    private final AdminDashboardView view;
    
    /**
     * Constructs a dominante modal manager tied to the given dashboard view.
     *
     * @param view the parent {@link AdminDashboardView} used to access services,
     *             the window reference, and to trigger section refresh
     */
    public AdminDominanteModal(AdminDashboardView view) {
        this.view = view;
    }
    
    /**
     * Opens a modal dialog for creating a new dominante.
     * <p>
     * The form fields are initially empty except for the colour picker which
     * defaults to blue (#3B82F6). The new dominante is automatically marked as
     * active. Upon successful creation the active dashboard section is refreshed.
     * </p>
     */
    public void openCreateDominanteModal() {
        openDominanteForm(null);
    }
    
    /**
     * Opens a modal dialog for editing an existing dominante.
     * <p>
     * The form is pre-populated with the current dominante's values (code, name,
     * description, responsible name, and colour). Upon successful update the
     * active dashboard section is refreshed. If the provided dominante is
     * {@code null} the method does nothing.
     * </p>
     *
     * @param d the dominante to edit; may be {@code null} in which case no action is taken
     */
    public void openEditDominanteModal(Dominante d) {
        if (d != null) openDominanteForm(d);
    }
    
    private void openDominanteForm(Dominante existing) {
        boolean edit = existing != null;
        DominanteService domService = view.getDominanteService();
        String title = edit ? "Modifier dominante" : "Nouvelle dominante";
        FormModal modal = new FormModal(520, 440, title, view.getWindow()::closeTopLayer);
        BaseComp body = modal.getBody();
        
        int row1Y = 8;
        ReusableLabeledInput codeInput = new ReusableLabeledInput("Code (ex: IA)",
            edit ? existing.getCode() : "", 16, row1Y, 200, 52);
        ReusableLabeledInput nameInput = new ReusableLabeledInput("Nom",
            edit ? existing.getName() : "", 224, row1Y, 268, 52);
        
        int row2Y = 68;
        ReusableLabeledInput descInput = new ReusableLabeledInput("Description",
            edit ? safe(existing.getDescription()) : "", 16, row2Y, 476, 52);
        
        int row3Y = 128;
        ReusableLabeledInput respInput = new ReusableLabeledInput("Responsable",
            edit ? safe(existing.getResponsibleName()) : "", 16, row3Y, 320, 52);
        
        int colorY = 188;
        Label colorLabel = labeledLabel("Couleur", 16, colorY);
        Color initColor = edit && existing.getColor() != null ? parseColor(existing.getColor()) : new Color(59, 130, 246);
        String hexColor = edit && existing.getColor() != null ? existing.getColor() : "#3B82F6";
        ColorPicker colorPicker = new ColorPicker(16, colorY + 18, 200, 76, hexColor);
        
        int btnY = 324;
        Label feedback = feedbackLabel(16, btnY);
        
        Button cancelBtn = styledBtn("Annuler", 296, btnY, 100, 30,
            new Color(40, 51, 73), view.getWindow()::closeTopLayer);
        Button actionBtn = new Button(edit ? "Enregistrer" : "Creer", 404, btnY, 100, 30, () -> {
            String code = codeInput.getValue().trim().toUpperCase();
            String name = nameInput.getValue().trim();
            if (code.isEmpty() || name.isEmpty()) { feedback.setText("Code et nom obligatoires."); return; }
            
            Dominante dom = edit ? existing : new Dominante();
            dom.setCode(code);
            dom.setName(name);
            dom.setDescription(descInput.getValue().trim());
            dom.setResponsibleName(respInput.getValue().trim());
            Color c = colorPicker.getSelectedColor();
            dom.setColor(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
            if (!edit) dom.setActive(true);
            
            ServiceResult r = edit ? domService.update(dom) : domService.create(dom);
            if (r.isSuccess()) { view.getWindow().closeTopLayer(); view.refreshActiveSection(); }
            else feedback.setText(r.getMessage());
        });
        actionBtn.setBackground(new Color(30, 93, 57));
        actionBtn.setForeground(new Color(233, 247, 238));
        
        body.addChild(codeInput); body.addChild(nameInput);
        body.addChild(descInput); body.addChild(respInput);
        body.addChild(colorLabel); body.addChild(colorPicker);
        body.addChild(feedback); body.addChild(cancelBtn);
        body.addChild(actionBtn);
        view.getWindow().openModal(modal);
    }
    
    private Color parseColor(String hex) {
        try { return Color.decode(hex.startsWith("#") ? hex : "#" + hex); } 
        catch (Exception e) { return new Color(59, 130, 246); }
    }
    
    private Label labeledLabel(String text, int x, int y) {
        Label l = new Label(text, x, y, 120, 16);
        l.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        l.setColor(new Color(100, 110, 130));
        return l;
    }
    
    private Label feedbackLabel(int x, int y) {
        Label l = new Label("", x, y, 280, 16);
        l.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        l.setColor(new Color(239, 68, 68));
        return l;
    }
    
    private Button styledBtn(String text, int x, int y, int w, int h, Color bg, Runnable action) {
        Button b = new Button(text, x, y, w, h, action);
        b.setBackground(bg);
        b.setForeground(new Color(233, 247, 238));
        return b;
    }
    
    private String safe(String value) { return value == null || value.isBlank() ? "" : value; }
}