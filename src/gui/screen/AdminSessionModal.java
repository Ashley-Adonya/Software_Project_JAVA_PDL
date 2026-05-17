package gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import components.Button;
import components.FormModal;
import components.Label;
import components.SelectInput;
import gui.components.ReusableLabeledInput;
import main.BaseComp;
import model.Dominante;
import model.SessionSlot;
import service.DominanteService;
import service.ServiceResult;
import service.SessionService;

/**
 * Gestion des modales de création et modification de sessions.
 * Regroupe les interfaces de création et d'édition dans une classe compacte
 * réutilisant la structure commune.
 */
public class AdminSessionModal {
    private final AdminDashboardView view;
    
    public AdminSessionModal(AdminDashboardView view) {
        this.view = view;
    }
    
    /**
     * Ouvre la modale de création d'une nouvelle session.
     */
    public void openCreateSessionModal() {
        openSessionForm(null);
    }
    
    /**
     * Ouvre la modale d'édition d'une session existante.
     * @param s La session à modifier
     */
    public void openEditSessionModal(SessionSlot s) {
        if (s != null) openSessionForm(s);
    }
    
    private void openSessionForm(SessionSlot existing) {
        boolean edit = existing != null;
        String title = edit ? "Modifier session" : "Nouvelle session";
        FormModal modal = new FormModal(520, 380, title, view.getWindow()::closeTopLayer);
        BaseComp body = modal.getBody();
        DominanteService domService = view.getDominanteService();
        SessionService sessService = view.getSessionService();
        
        int row1Y = 8;
        ReusableLabeledInput titleInput = new ReusableLabeledInput("Titre", 
            edit ? safe(existing.getTitle()) : "", 16, row1Y, 230, 54);
        ReusableLabeledInput dateInput = new ReusableLabeledInput("Date (yyyy-MM-dd)", 
            edit ? safe(existing.getSessionDate()) : "", 254, row1Y, 234, 54);
        
        int row2Y = 70;
        ReusableLabeledInput startInput = new ReusableLabeledInput("Debut (HH:mm)",
            edit ? minutesToHHmm(existing.getStartMinute()) : "08:30", 16, row2Y, 110, 54);
        ReusableLabeledInput endInput = new ReusableLabeledInput("Fin (HH:mm)",
            edit ? minutesToHHmm(existing.getEndMinute()) : "12:30", 132, row2Y, 110, 54);
        ReusableLabeledInput roomInput = new ReusableLabeledInput("Salle",
            edit ? safe(existing.getRoom()) : "", 248, row2Y, 240, 54);
        ReusableLabeledInput capacityInput = new ReusableLabeledInput("Capacite",
            edit ? String.valueOf(existing.getCapacity()) : "30", 16, row2Y + 62, 110, 54);
        
        int domY = 194;
        Label domLabel = labeledLabel("Dominante", 16, domY);
        SelectInput domSelect = new SelectInput(16, domY + 18, 472, 30);
        List<String> domOptions = new ArrayList<>();
        Dominante selectedDom = null;
        for (Dominante d : domService.listAll()) {
            domOptions.add(d.getName());
            if (edit && d.getId() == existing.getDominanteId()) selectedDom = d;
        }
        domSelect.setOptions(domOptions);
        if (selectedDom != null) domSelect.setSelectedOption(selectedDom.getName());
        
        int btnY = 296;
        Label feedback = feedbackLabel(16, btnY);
        
        Button cancelBtn = cancelButton(296, btnY, view.getWindow()::closeTopLayer);
        String btnText = edit ? "Enregistrer" : "Creer";
        Button actionBtn = new Button(btnText, 404, btnY, 100, 30, () -> {
            if (view.getActiveCampaign() == null || view.getActiveCampaign().getId() <= 0) {
                feedback.setText("Aucune campagne active."); return;
            }
            int domId = findDominanteId(domService.listAll(), domSelect.getSelectedOption());
            if (domId <= 0) { feedback.setText("Selectionnez une dominante."); return; }
            
            SessionSlot slot = edit ? existing : new SessionSlot();
            slot.setCampaignId(view.getActiveCampaign().getId());
            slot.setDominanteId(domId);
            slot.setTitle(titleInput.getValue());
            slot.setSessionDate(dateInput.getValue());
            try { slot.setStartMinute(parseHHmm(startInput.getValue())); } catch (Exception e) {
                feedback.setText("Format heure invalide (HH:mm attendu)"); return;
            }
            try { slot.setEndMinute(parseHHmm(endInput.getValue())); } catch (Exception e) {
                feedback.setText("Format heure invalide (HH:mm attendu)"); return;
            }
            slot.setRoom(roomInput.getValue());
            try { slot.setCapacity(Integer.parseInt(capacityInput.getValue())); } catch (Exception e) {}
            
            ServiceResult r = edit ? sessService.updateSession(slot) : sessService.createSession(slot);
            if (r.isSuccess()) { view.getWindow().closeTopLayer(); view.refreshSessions(); }
            else feedback.setText(r.getMessage());
        });
        actionBtn.setBackground(new Color(30, 93, 57));
        actionBtn.setForeground(new Color(233, 247, 238));
        
        body.addChild(titleInput); body.addChild(dateInput);
        body.addChild(startInput); body.addChild(endInput);
        body.addChild(roomInput); body.addChild(capacityInput);
        body.addChild(domLabel); body.addChild(domSelect);
        body.addChild(feedback); body.addChild(cancelBtn);
        body.addChild(actionBtn);
        view.getWindow().openModal(modal);
    }
    
    private int findDominanteId(List<Dominante> allDom, String selectedName) {
        for (Dominante d : allDom) if (d.getName().equals(selectedName)) return d.getId();
        return -1;
    }
    
    private Label labeledLabel(String text, int x, int y) {
        Label l = new Label(text, x, y, 120, 16);
        l.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        l.setColor(new Color(100, 110, 130));
        return l;
    }
    
    private Label feedbackLabel(int x, int y) {
        Label l = new Label("", x, y, 300, 16);
        l.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        l.setColor(new Color(239, 68, 68));
        return l;
    }
    
    private Button cancelButton(int x, int y, Runnable action) {
        Button b = new Button("Annuler", x, y, 100, 30, action);
        b.setBackground(new Color(40, 51, 73));
        b.setForeground(new Color(219, 230, 253));
        return b;
    }
    
    private String safe(String value) { return value == null || value.isBlank() ? "-" : value; }

    private String minutesToHHmm(int totalMinutes) {
        return String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

    private int parseHHmm(String value) {
        if (value == null || !value.contains(":")) throw new IllegalArgumentException();
        String[] parts = value.trim().split(":");
        int h = Integer.parseInt(parts[0].trim());
        int m = Integer.parseInt(parts[1].trim());
        return h * 60 + m;
    }
}