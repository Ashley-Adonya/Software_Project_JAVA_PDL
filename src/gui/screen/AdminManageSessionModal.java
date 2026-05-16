package gui.screen;

import java.awt.Color;

import components.Button;
import components.FormModal;
import components.Label;
import components.ScrollView;
import gui.components.ReusableLabeledInput;
import gui.components.SurfaceCard;
import main.BaseComp;
import model.Dominante;
import model.SessionSlot;
import service.RegistrationService;

/**
 * Modale de gestion d'une session (capacité, étudiants inscrits).
 * Permet à l'admin de modifier la capacité et de visualiser les inscrits.
 */
public class AdminManageSessionModal {
    private final AdminDashboardView view;
    
    public AdminManageSessionModal(AdminDashboardView view) {
        this.view = view;
    }
    
    /**
     * Ouvre la modale de gestion pour une session donnée.
     * @param s       La session à gérer
     * @param allocated Nombre d'étudiants actuellement inscrits
     */
    public void openManageSessionModal(SessionSlot s, int allocated) {
        if (s == null) return;
        FormModal modal = new FormModal(620, 420, "Gerer session", view.getWindow()::closeTopLayer);
        BaseComp body = modal.getBody();
        Dominante d = view.getDominanteService().findById(s.getDominanteId());
        String domName = d != null ? d.getName() : "Dominante #" + s.getDominanteId();
        
        Label sessionTitle = new Label(domName + " - " + safe(s.getTitle()), 16, 10, 580, 22);
        sessionTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
        sessionTitle.setColor(new Color(27, 39, 56));
        
        Label details = new Label(safe(s.getSessionDate()) + " | " + formatMinute(s.getStartMinute()) + "-" 
            + formatMinute(s.getEndMinute()) + " | " + safe(s.getRoom()) + " | " 
            + allocated + "/" + s.getCapacity() + " places", 16, 36, 580, 16);
        details.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        details.setColor(new Color(100, 116, 139));
        
        int capY = 58;
        Label feedbackLabel = new Label("", 244, capY + 18, 200, 16);
        feedbackLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedbackLabel.setColor(new Color(239, 68, 68));
        
        ReusableLabeledInput capInput = new ReusableLabeledInput("Modifier capacite",
            String.valueOf(s.getCapacity()), 16, capY, 160, 52);
        Button updateBtn = new Button("OK", 184, capY + 14, 50, 28, () -> {
            try {
                int newCap = Integer.parseInt(capInput.getValue());
                boolean ok = view.getSessionService().updateCapacity(s.getId(), newCap);
                if (ok) { 
                    s.setCapacity(newCap); capInput.setValue(String.valueOf(newCap));
                    feedbackLabel.setText("Capacite mise a jour.");
                    feedbackLabel.setColor(new Color(34, 197, 94));
                    view.refreshActiveSection();
                } else {
                    feedbackLabel.setText("Modification interdite.");
                    feedbackLabel.setColor(new Color(239, 68, 68));
                }
            } catch (Exception ex) {
                feedbackLabel.setText("Capacite invalide.");
                feedbackLabel.setColor(new Color(239, 68, 68));
            }
        });
        updateBtn.setBackground(new Color(59, 130, 246));
        updateBtn.setForeground(Color.WHITE);
        
        Label studentsTitle = new Label("Etudiants inscrits (" + allocated + ")", 16, 120, 200, 16);
        studentsTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        studentsTitle.setColor(new Color(27, 39, 56));
        
        ScrollView scroll = new ScrollView(8, 142, 596, 220);
        Button closeBtn = styledButton("Fermer", 252, 372, 110, 30, new Color(40, 51, 73), 
            view.getWindow()::closeTopLayer);
        
        body.addChild(sessionTitle); body.addChild(details);
        body.addChild(capInput); body.addChild(updateBtn);
        body.addChild(feedbackLabel); body.addChild(studentsTitle);
        body.addChild(scroll); body.addChild(closeBtn);
        view.getWindow().openModal(modal);
    }
    
    private Button styledButton(String text, int x, int y, int w, int h, Color bg, Runnable action) {
        Button b = new Button(text, x, y, w, h, action);
        b.setBackground(bg);
        b.setForeground(new Color(219, 230, 253));
        return b;
    }
    
    private String formatMinute(int min) {
        return String.format("%02d:%02d", min / 60, min % 60);
    }
    
    private String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}