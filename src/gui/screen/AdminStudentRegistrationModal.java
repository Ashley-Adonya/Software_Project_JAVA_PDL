package gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import components.Button;
import components.FormModal;
import components.Label;
import components.ScrollView;
import components.SelectInput;
import gui.components.SurfaceCard;
import main.BaseComp;
import model.Dominante;
import model.SessionSlot;
import model.User;
import service.RegistrationService;
import service.ServiceResult;

/**
 * Modale d'inscription d'un étudiant par l'administrateur.
 * Gère la sélection de dominante, l'affichage des sessions disponibles,
 * la détection des conflits et la proposition d'alternatives.
 */
public class AdminStudentRegistrationModal {
    private final AdminDashboardView view;
    
    public AdminStudentRegistrationModal(AdminDashboardView view) {
        this.view = view;
    }
    
    /**
     * Ouvre la modale d'inscription pour un étudiant donné.
     * @param student L'étudiant à inscrire
     */
    public void openManageStudentModal(User student) {
        if (student == null || view.getActiveCampaign() == null) return;
        int campaignId = view.getActiveCampaign().getId();
        int studentId = student.getId();
        FormModal modal = new FormModal(640, 500, "Inscrire - " + safe(student.getFullName()), view.getWindow()::closeTopLayer);
        BaseComp body = modal.getBody();
        
        Label studentLabel = new Label("Etudiant: " + safe(student.getFullName()) + " (" + safe(student.getLogin()) + ")", 16, 10, 600, 20);
        studentLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 13));
        studentLabel.setColor(new Color(27, 39, 56));
        
        Label domLabel = new Label("Selectionnez une dominante", 16, 42, 200, 16);
        domLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        domLabel.setColor(new Color(100, 110, 130));
        
        SelectInput domSelect = new SelectInput(16, 60, 280, 30);
        List<String> domOptions = new ArrayList<>();
        for (Dominante d : view.getDominanteService().listAll()) domOptions.add(d.getName());
        domSelect.setOptions(domOptions);
        
        int sessionY = 100;
        Label sessionLabel = new Label("Sessions disponibles", 16, sessionY, 200, 16);
        sessionLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
        sessionLabel.setColor(new Color(100, 110, 130));
        
        ScrollView sessionScroll = new ScrollView(8, sessionY + 18, 616, 220);
        Label feedbackLabel = new Label("", 16, 348, 400, 16);
        feedbackLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        feedbackLabel.setColor(new Color(239, 68, 68));
        
        Button closeBtn = new Button("Fermer", 252, 430, 110, 30, view.getWindow()::closeTopLayer);
        closeBtn.setBackground(new Color(40, 51, 73));
        closeBtn.setForeground(new Color(219, 230, 253));
        
        domSelect.setOnChange(selected -> refreshSessionList(campaignId, studentId, selected, sessionScroll, feedbackLabel, modal));
        
        body.addChild(studentLabel); body.addChild(domLabel); body.addChild(domSelect);
        body.addChild(sessionLabel); body.addChild(sessionScroll);
        body.addChild(feedbackLabel); body.addChild(closeBtn);
        view.getWindow().openModal(modal);
    }
    
    private void refreshSessionList(int campaignId, int studentId, String dominanteName, 
                                     ScrollView sessionScroll, Label feedbackLabel, FormModal modal) {
        BaseComp list = sessionScroll.getContent();
        clearChildren(list);
        Dominante selectedDom = findDominante(dominanteName);
        if (selectedDom == null) return;
        
        List<SessionSlot> sessions = view.getSessionService().listByCampaign(campaignId);
        int y = 0;
        
        for (SessionSlot s : sessions) {
            if (s.getDominanteId() != selectedDom.getId()) continue;
            
            RegistrationService.ConflictResult check = view.getRegistrationService().checkRegistration(campaignId, studentId, s.getId());
            int slotAllocated = view.getRegistrationDAO().countAllocatedBySession(campaignId, s.getId());
            
            SurfaceCard sessionCard = createSessionCard(s, check, slotAllocated, y, sessionScroll.getWidth() - 16,
                campaignId, studentId, dominanteName, sessionScroll, feedbackLabel, modal);
            list.addChild(sessionCard);
            y += 66;
        }
        
        if (y == 0) {
            Label noSessions = new Label("Aucune session pour cette dominante", 0, 0, 300, 24);
            noSessions.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            noSessions.setColor(new Color(100, 116, 139));
            list.addChild(noSessions);
            y = 24;
        }
        sessionScroll.setContentHeight(Math.max(sessionScroll.getHeight(), y + 10));
    }
    
    private SurfaceCard createSessionCard(SessionSlot s, RegistrationService.ConflictResult check, int slotAllocated,
                                          int y, int cardWidth, int campaignId, int studentId, String dominanteName,
                                          ScrollView sessionScroll, Label feedbackLabel, FormModal modal) {
        SurfaceCard card = new SurfaceCard(0, y, cardWidth, 60,
            check.hasConflict ? new Color(255, 240, 240) : (check.sessionFull ? new Color(255, 245, 230) : new Color(240, 248, 255)),
            new Color(226, 230, 238), 6);
        
        String statusText;
        java.awt.Color statusColor;
        if (check.hasConflict) {
            statusText = "Conflit: " + check.conflictMessage;
            statusColor = new Color(196, 61, 61);
        } else if (check.sessionFull) {
            statusText = "Session complete (" + slotAllocated + "/" + s.getCapacity() + ")";
            statusColor = new Color(180, 120, 20);
        } else {
            statusText = (s.getCapacity() - slotAllocated) + " places";
            statusColor = new Color(34, 197, 94);
        }
        
        Label sTitle = new Label(safe(s.getTitle()), 10, 8, 300, 18);
        sTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        sTitle.setColor(new Color(27, 39, 56));
        
        Label sInfo = new Label(safe(s.getSessionDate()) + " | " + formatMinute(s.getStartMinute()) + "-" + formatMinute(s.getEndMinute()) + " | " + safe(s.getRoom()), 10, 28, 400, 16);
        sInfo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
        sInfo.setColor(new Color(100, 116, 139));
        
        Label sStatus = new Label(statusText, 10, 44, 300, 14);
        sStatus.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
        sStatus.setColor(statusColor);
        
        card.addChild(sTitle); card.addChild(sInfo); card.addChild(sStatus);
        
        if (!check.hasConflict && !check.sessionFull) {
            Button regBtn = new Button("Inscrire", cardWidth - 100, 16, 76, 26, () -> {
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, s.getId(), false);
                if (r.isSuccess()) {
                    feedbackLabel.setText("Inscrit avec succes!");
                    feedbackLabel.setColor(new Color(34, 197, 94));
                    refreshSessionList(campaignId, studentId, dominanteName, sessionScroll, feedbackLabel, modal);
                } else {
                    feedbackLabel.setText(r.getMessage());
                    feedbackLabel.setColor(new Color(239, 68, 68));
                }
            });
            regBtn.setBackground(new Color(30, 93, 57));
            regBtn.setForeground(new Color(233, 247, 238));
            card.addChild(regBtn);
        } else if (check.sessionFull && check.alternatives != null && !check.alternatives.isEmpty()) {
            Button altBtn = new Button("Alternative", cardWidth - 110, 16, 90, 26, () -> {
                RegistrationService.AlternativeSession alt = check.alternatives.get(0);
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, alt.sessionId, false);
                if (r.isSuccess()) {
                    feedbackLabel.setText("Inscrit a: " + alt.title);
                    feedbackLabel.setColor(new Color(34, 197, 94));
                    refreshSessionList(campaignId, studentId, dominanteName, sessionScroll, feedbackLabel, modal);
                } else {
                    feedbackLabel.setText(r.getMessage());
                    feedbackLabel.setColor(new Color(239, 68, 68));
                }
            });
            altBtn.setBackground(new Color(245, 158, 11));
            altBtn.setForeground(Color.WHITE);
            card.addChild(altBtn);
        }
        return card;
    }
    
    private Dominante findDominante(String name) {
        for (Dominante d : view.getDominanteService().listAll()) if (d.getName().equals(name)) return d;
        return null;
    }
    
    private void clearChildren(BaseComp parent) {
        for (BaseComp c : new ArrayList<>(parent.getChildrenList())) parent.removeChild(c);
    }
    
    private String formatMinute(int min) {
        return String.format("%02d:%02d", min / 60, min % 60);
    }
    
    private String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}