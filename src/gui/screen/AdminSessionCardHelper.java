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
 * Helper utility for building individual session cards within the student
 * registration modal.
 * <p>
 * Each card visually represents a session slot, displaying its title, date,
 * time range, room, and a colour-coded availability status:
 * <ul>
 *   <li><b>Red</b> &ndash; scheduling conflict</li>
 *   <li><b>Orange</b> &ndash; session full (with an optional alternative)</li>
 *   <li><b>Blue</b> &ndash; places available</li>
 * </ul>
 * Depending on the state, a "Register" or "Alternative" action button is
 * added to the card.
 * </p>
 */
class AdminSessionCardHelper {
    private final AdminDashboardView view;

    /**
     * Constructs a session card helper bound to the given dashboard view.
     *
     * @param view the parent {@link AdminDashboardView} used to access the
     *             registration service and window reference
     */
    AdminSessionCardHelper(AdminDashboardView view) { this.view = view; }

    /**
     * Builds a colour-coded session card with status information and action buttons.
     * <p>
     * The card content includes:
     * <ul>
     *   <li>Session title (bold)</li>
     *   <li>Date, time range, and room (secondary text)</li>
     *   <li>Status indicator (conflict, remaining places, or full)</li>
     *   <li>"Register" button when the slot is available and has free capacity</li>
     *   <li>"Alternative" button when the slot is full but an alternative session exists</li>
     * </ul>
     * The background colour changes according to the status: red for conflict,
     * orange for full, blue for available.
     * </p>
     *
     * @param s                the session slot to render
     * @param check            the conflict-check result for this student/session pair
     * @param slotAllocated    current number of registrations allocated to this session
     * @param y                vertical pixel position of the card within the parent scroll view
     * @param cardWidth        the card width in pixels
     * @param campaignId       the ID of the active campaign
     * @param studentId        the ID of the student being registered
     * @param dominanteName    the selected dominante name (used during list rebuild)
     * @param sessionScroll    the parent scroll view container
     * @param feedbackLabel    the label used to display registration feedback messages
     * @param modal            the parent modal dialog (for closing if needed)
     * @param refreshCallback  callback invoked after a successful registration to rebuild the session list
     * @return a fully constructed {@link SurfaceCard} component ready for display
     */
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
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, s.getId(), true);
                updateFeedback(feedbackLabel, r, null);
                refreshCallback.run();
            });
            card.addChild(regBtn);
        } else if (check.sessionFull && check.alternatives != null && !check.alternatives.isEmpty()) {
            Button altBtn = actionBtn("Alternative", cardWidth - 110, 16, 90, 26, new Color(245, 158, 11), () -> {
                RegistrationService.AlternativeSession alt = check.alternatives.get(0);
                ServiceResult r = view.getRegistrationService().registerStudent(campaignId, studentId, alt.sessionId, true);
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