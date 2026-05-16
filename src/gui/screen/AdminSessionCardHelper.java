package gui.screen;

import java.awt.Color;

import components.Button;
import components.Label;
import components.ScrollView;
import components.FormModal;
import gui.components.SurfaceCard;
import model.SessionSlot;
import service.RegistrationService;
import service.ServiceResult;

/**
 * Aide à la construction des cartes de session dans la modale d'inscription.
 * Gère l'affichage des statuts (conflit, plein, disponible) et les boutons d'action.
 */
class AdminSessionCardHelper {
    private final AdminDashboardView view;

    AdminSessionCardHelper(AdminDashboardView view) { this.view = view; }

    SurfaceCard createSessionCard(SessionSlot s, RegistrationService.ConflictResult check, int slotAllocated,
                                   int y, int cardWidth, int campaignId, int studentId, String dominanteName,
                                   ScrollView sessionScroll, Label feedbackLabel, FormModal modal,
                                   Runnable refreshCallback) {
        SurfaceCard card = new SurfaceCard(0, y, cardWidth, 60,
            check.hasConflict ? new Color(255, 240, 240) : 
            check.sessionFull ? new Color(255, 245, 230) : new Color(240, 248, 255),
            new Color(226, 230, 238), 6);

        String statusText; Color statusColor;
        if (check.hasConflict) { statusText = "Conflit: " + check.conflictMessage; statusColor = new Color(196, 61, 61); }
        else if (check.sessionFull) { statusText = "Session complete (" + slotAllocated + "/" + s.getCapacity() + ")"; statusColor = new Color(180, 120, 20); }
        else { statusText = (s.getCapacity() - slotAllocated) + " places"; statusColor = new Color(34, 197, 94); }

        card.addChild(label(safe(s.getTitle()), 10, 8, 300, 18, 12, true, new Color(27, 39, 56)));
        card.addChild(label(safe(s.getSessionDate()) + " | " + fmt(s.getStartMinute()) + "-" + fmt(s.getEndMinute()) + " | " + safe(s.getRoom()), 10, 28, 400, 16, 11, false, new Color(100, 116, 139)));
        card.addChild(label(statusText, 10, 44, 300, 14, 10, false, statusColor));

        if (!check.hasConflict && !check.sessionFull) {
            Button regBtn = actionBtn("Inscrire", cardWidth - 100, 16, 76, 26, new Color(30, 93, 57), () -> {
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, s.getId(), false);
                updateFeedback(feedbackLabel, r, null);
                refreshCallback.run();
            });
            card.addChild(regBtn);
        } else if (check.sessionFull && check.alternatives != null && !check.alternatives.isEmpty()) {
            Button altBtn = actionBtn("Alternative", cardWidth - 110, 16, 90, 26, new Color(245, 158, 11), () -> {
                RegistrationService.AlternativeSession alt = check.alternatives.get(0);
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, alt.sessionId, false);
                updateFeedback(feedbackLabel, r, alt.title);
                refreshCallback.run();
            });
            card.addChild(altBtn);
        }
        return card;
    }

    private Button actionBtn(String text, int x, int y, int w, int h, Color bg, Runnable action) {
        Button b = new Button(text, x, y, w, h, action);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        return b;
    }

    private void updateFeedback(Label fb, ServiceResult r, String altTitle) {
        if (r.isSuccess()) {
            fb.setText(altTitle != null ? "Inscrit a: " + altTitle : "Inscrit avec succes!");
            fb.setColor(new Color(34, 197, 94));
        } else { fb.setText(r.getMessage()); fb.setColor(new Color(239, 68, 68)); }
    }

    private Label label(String t, int x, int y, int w, int h, int sz, boolean bd, Color c) {
        Label l = new Label(t, x, y, w, h);
        l.setFont(new java.awt.Font("Dialog", bd ? java.awt.Font.BOLD : java.awt.Font.PLAIN, sz));
        l.setColor(c); return l;
    }

    private String fmt(int min) { return String.format("%02d:%02d", min / 60, min % 60); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}