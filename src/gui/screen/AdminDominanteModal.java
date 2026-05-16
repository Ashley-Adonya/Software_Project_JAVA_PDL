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
 * Modale de création et édition des dominantes.
 * Interface centralisée pour gérer les domaines d'études.
 */
public class AdminDominanteModal {
    private final AdminDashboardView view;
    
    public AdminDominanteModal(AdminDashboardView view) {
        this.view = view;
    }
    
    /**
     * Ouvre la modale de création d'une nouvelle dominante.
     */
    public void openCreateDominanteModal() {
        openDominanteForm(null);
    }
    
    /**
     * Ouvre la modale d'édition d'une dominante existante.
     * @param d La dominante à modifier
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
        ColorPicker colorPicker = new ColorPicker(16, colorY + 18, 200, 76, initColor);
        
        int btnY = 324;
        Label feedback = feedbackLabel(16, btnY);
        
        Button cancelBtn = styledBtn("Annuler", 296, btnY, 100, 30,
            new Color(40, 51, 73), view.getWindow()::closeTopLayer);
        Button actionBtn = styledBtn(edit ? "Enregistrer" : "Creer", 404, btnY, 100, 30,
            new Color(30, 93, 57), null);
        
        actionBtn.setOnClick(() -> {
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