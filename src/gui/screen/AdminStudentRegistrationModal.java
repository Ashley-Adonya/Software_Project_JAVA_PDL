package gui.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import components.Button;
import components.FormModal;
import components.Label;
import components.ScrollView;
import components.SelectInput;
import main.BaseComp;
import model.Dominante;
import model.SessionSlot;
import model.User;
import service.RegistrationService;

/**
 * Modal dialog for administrator-driven student registration.
 * <p>
 * Allows an admin to select a student, choose a study domain (dominante),
 * view all available session slots for that domain, detect scheduling
 * conflicts, and register the student with a single click. When a session
 * is full, the dialog can propose alternative sessions. Each session card
 * is dynamically built by {@link AdminSessionCardHelper} and shows live
 * availability status and action buttons.
 * </p>
 */
public class AdminStudentRegistrationModal {
    private final AdminDashboardView view;
    private final AdminSessionCardHelper cardHelper;

    /**
     * Constructs a student registration modal manager.
     *
     * @param view the parent {@link AdminDashboardView} used to access services,
     *             DAOs, the window reference, and session data
     */
    public AdminStudentRegistrationModal(AdminDashboardView view) {
        this.view = view;
        this.cardHelper = new AdminSessionCardHelper(view);
    }

    /**
     * Opens a modal dialog to manage registration for the given student.
     * <p>
     * The dialog displays the student's full name and login, a dropdown to
     * select a dominante, and a scrollable list of sessions that updates
     * dynamically when the selected dominante changes. Each session card
     * indicates conflict status, remaining places, or fullness. The admin
     * can register the student directly or choose an alternative session
     * when the preferred one is full.
     * </p>
     *
     * @param student the student to register; if {@code null} or if no active
     *                campaign exists, the method returns without action
     */
    public void openManageStudentModal(User student) {
        if (student == null || view.getActiveCampaign() == null) return;
        int campaignId = view.getActiveCampaign().getId();
        int studentId = student.getId();
        FormModal modal = new FormModal(640, 500, "Inscrire - " + safe(student.getFullName()), view.getWindow()::closeTopLayer);
        BaseComp body = modal.getBody();

        Label studentLabel = styledLabel("Etudiant: " + safe(student.getFullName()) + " (" + safe(student.getLogin()) + ")", 16, 10, 600, 20, 13, true, new Color(27, 39, 56));
        Label domLabel = styledLabel("Selectionnez une dominante", 16, 42, 200, 16, 11, true, new Color(100, 110, 130));
        SelectInput domSelect = new SelectInput(16, 60, 280, 30);
        List<String> domOptions = new ArrayList<>();
        for (Dominante d : view.getDominanteService().listAll()) domOptions.add(d.getName());
        domSelect.setOptions(domOptions);

        Label sessionLabel = styledLabel("Sessions disponibles", 16, 100, 200, 16, 11, true, new Color(100, 110, 130));
        ScrollView sessionScroll = new ScrollView(8, 118, 616, 220);
        Label feedbackLabel = styledLabel("", 16, 348, 400, 16, 11, false, new Color(239, 68, 68));
        Button closeBtn = new Button("Fermer", 252, 430, 110, 30, view.getWindow()::closeTopLayer);
        closeBtn.setBackground(new Color(40, 51, 73)); closeBtn.setForeground(new Color(219, 230, 253));

        domSelect.setOnChange(selected -> refreshSessionList(campaignId, studentId, selected, sessionScroll, feedbackLabel, modal));
        body.addChild(studentLabel); body.addChild(domLabel); body.addChild(domSelect);
        body.addChild(sessionLabel); body.addChild(sessionScroll);
        body.addChild(feedbackLabel); body.addChild(closeBtn);
        view.getWindow().openModal(modal);
    }

    private void refreshSessionList(int campaignId, int studentId, String dominanteName,
                                     ScrollView sessionScroll, Label feedbackLabel, FormModal modal) {
        BaseComp list = sessionScroll.getContent(); clearChildren(list);
        Dominante selectedDom = findDominante(dominanteName);
        if (selectedDom == null) return;
        List<SessionSlot> sessions = view.getSessionService().listByCampaign(campaignId);
        int y = 0;
        for (SessionSlot s : sessions) {
            if (s.getDominanteId() != selectedDom.getId()) continue;
            RegistrationService.ConflictResult check = view.getRegistrationService().checkRegistration(campaignId, studentId, s.getId());
            int slotAllocated = view.getRegistrationDAO().countAllocatedBySession(campaignId, s.getId());
            list.addChild(cardHelper.createSessionCard(s, check, slotAllocated, y, sessionScroll.getWidth() - 16,
                campaignId, studentId, dominanteName, sessionScroll, feedbackLabel, modal,
                () -> refreshSessionList(campaignId, studentId, dominanteName, sessionScroll, feedbackLabel, modal)));
            y += 66;
        }
        if (y == 0) {
            list.addChild(styledLabel("Aucune session pour cette dominante", 0, 0, 300, 24, 12, false, new Color(100, 116, 139)));
            y = 24;
        }
        sessionScroll.setContentHeight(Math.max(sessionScroll.getHeight(), y + 10));
    }

    private Dominante findDominante(String name) {
        for (Dominante d : view.getDominanteService().listAll()) if (d.getName().equals(name)) return d;
        return null;
    }

    private Label styledLabel(String t, int x, int y, int w, int h, int sz, boolean bd, Color c) {
        Label l = new Label(t, x, y, w, h);
        l.setFont(new java.awt.Font("Dialog", bd ? java.awt.Font.BOLD : java.awt.Font.PLAIN, sz));
        l.setColor(c);
        return l;
    }

    private void clearChildren(BaseComp p) { for (BaseComp c : new ArrayList<>(p.getChildrenList())) p.removeChild(c); }
    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}