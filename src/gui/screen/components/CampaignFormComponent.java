package gui.screen.components;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;

import components.Label;
import gui.components.PrimaryButton;
import gui.components.ReusableLabeledInput;
import gui.components.SurfaceCard;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;
import service.AssignmentService;
import service.CampaignService;
import service.ServiceResult;
import model.User;

/**
 * Reusable form component for creating and managing campaigns in the admin dashboard.
 * Provides input fields for campaign metadata (name, registration dates, max choices),
 * status management buttons for transitioning between campaign lifecycle states,
 * and an automatic assignment trigger.
 * <p>
 * Key features:
 * - Create and edit campaign details (name, dates, max choices)
 * - Status transition buttons (PREPARATION, OPEN, CLOSED, VALIDATED, ARCHIVED)
 * - Automatic student-to-session assignment execution
 * - Visual status indicator with color-coded labels
 * - Async save operations with progress feedback
 * - Responsive layout adaptation to parent container width
 */
public class CampaignFormComponent {
    // private final BaseWindow window;
    private final BaseComp root;
    private final SurfaceCard formCard;
    private final SurfaceCard statusCard;
    private final ReusableLabeledInput campaignNameInput;
    private final ReusableLabeledInput registrationDateInput;
    private final ReusableLabeledInput maxChoicesInput;
    private final ReusableLabeledInput startDateInput;
    private final ReusableLabeledInput endDateInput;
    private final PrimaryButton saveCampaignButton;
    private final PrimaryButton preparationBtn;
    private final PrimaryButton openBtn;
    private final PrimaryButton closeBtn;
    private final PrimaryButton autoBtn;
    private final PrimaryButton validateBtn;
    private final PrimaryButton archiveBtn;
    private final Label statusValueLabel;
    private final Label feedbackLabel;

    private Consumer<Campaign> onSave = c -> {};
    private Campaign currentCampaign;
    private CampaignService campaignService;
    private AssignmentService assignmentService;

    /**
     * Constructs the campaign form component with the parent window and current user.
     * Creates two SurfaceCard sections: the form card with input fields and save button,
     * and the status card with state transition buttons and auto-assignment trigger.
     * All buttons are initially invisible and shown dynamically based on the campaign status.
     *
     * @param window      the main application window for rendering
     * @param currentUser the currently authenticated user, used to initialize the CampaignService
     *                    with appropriate permissions for status transitions
     */
    public CampaignFormComponent(BaseWindow window, User currentUser) {
        // this.window = window;
        this.campaignService = new CampaignService(currentUser);
        this.assignmentService = new AssignmentService();
        this.root = new BaseComp(null);

        // --- Formulaire de création/modification ---
        this.formCard = new SurfaceCard(0, 0, 100, 310,
            new java.awt.Color(22, 28, 39), new java.awt.Color(48, 60, 82), 12);

        this.campaignNameInput = new ReusableLabeledInput("Nom de la campagne", "", 16, 42, 380, 62);
        this.registrationDateInput = new ReusableLabeledInput("Date inscript (yyyy-MM-dd)", "", 16, 108, 200, 62);
        this.maxChoicesInput = new ReusableLabeledInput("Nombre max de choix", "5", 232, 108, 164, 62);
        this.startDateInput = new ReusableLabeledInput("Date de debut (yyyy-MM-dd)", "", 16, 174, 200, 62);
        this.endDateInput = new ReusableLabeledInput("Date de fin (yyyy-MM-dd)", "", 232, 174, 200, 62);

        this.saveCampaignButton = new PrimaryButton("Enregistrer", 16, 250, 130, 34, () -> {
            Campaign c = new Campaign();
            c.setName(campaignNameInput.getValue());
            c.setRegistrationDay(registrationDateInput.getValue());
            try { c.setMaxChoices(Integer.parseInt(maxChoicesInput.getValue())); }
            catch (Exception e) { c.setMaxChoices(0); }
            c.setStartDate(startDateInput.getValue());
            c.setEndDate(endDateInput.getValue());
            onSave.accept(c);
        });

        this.feedbackLabel = new Label("", 16, 288, 420, 16);
        this.feedbackLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.feedbackLabel.setColor(new Color(239, 68, 68));

        formCard.addChild(campaignNameInput);
        formCard.addChild(registrationDateInput);
        formCard.addChild(maxChoicesInput);
        formCard.addChild(startDateInput);
        formCard.addChild(endDateInput);
        formCard.addChild(saveCampaignButton);
        formCard.addChild(feedbackLabel);

        // --- Carte de gestion du statut et traitement auto ---
        this.statusCard = new SurfaceCard(0, 320, 100, 180,
            new java.awt.Color(22, 28, 39), new java.awt.Color(48, 60, 82), 12);

        Label statusTitleLabel = new Label("Statut de la campagne", 16, 14, 300, 18);
        statusTitleLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        statusTitleLabel.setColor(new Color(160, 175, 202));

        this.statusValueLabel = new Label("—", 16, 36, 300, 20);
        this.statusValueLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        this.statusValueLabel.setColor(new Color(239, 244, 252));

        // Boutons de transition de statut
        this.preparationBtn = statusBtn("Préparation", new Color(100, 100, 100), () -> changeStatus("PREPARATION"));
        this.openBtn = statusBtn("Ouvrir (OPEN)", new Color(34, 120, 60), () -> changeStatus("OPEN"));
        this.closeBtn = statusBtn("Fermer (CLOSED)", new Color(180, 100, 20), () -> changeStatus("CLOSED"));
        this.autoBtn = statusBtn("Lancer traitement auto", new Color(59, 100, 220), () -> runAutoAssignment());
        this.validateBtn = statusBtn("Valider (VALIDATED)", new Color(80, 60, 160), () -> changeStatus("VALIDATED"));
        this.archiveBtn = statusBtn("Archiver", new Color(80, 80, 80), () -> changeStatus("ARCHIVED"));

        statusCard.addChild(statusTitleLabel);
        statusCard.addChild(statusValueLabel);
        statusCard.addChild(preparationBtn);
        statusCard.addChild(openBtn);
        statusCard.addChild(closeBtn);
        statusCard.addChild(autoBtn);
        statusCard.addChild(validateBtn);
        statusCard.addChild(archiveBtn);

        root.addChild(formCard);
        root.addChild(statusCard);
    }

    /**
     * Returns the root container component that holds both the form card and status card.
     * This root should be added to the parent layout for the form to be visible.
     *
     * @return the root BaseComp containing all form and status UI elements
     */
    public BaseComp getRoot() { return root; }

    /**
     * Registers a callback to be invoked when the save button is clicked.
     * The callback receives a Campaign object populated with the current form field values.
     *
     * @param cb the consumer to call with the assembled Campaign on save; may be null-safe default
     */
    public void onSave(Consumer<Campaign> cb) { this.onSave = cb; }

    /**
     * Populates the form fields and status display from the given campaign.
     * If the campaign is null, all fields are cleared and a message indicating
     * no active campaign is shown. Status buttons are updated to reflect
     * allowed transitions from the campaign's current state.
     *
     * @param campaign the campaign to load data from, or null to reset the form
     */
    public void refreshFrom(Campaign campaign) {
        this.currentCampaign = campaign;
        if (campaign == null) {
            campaignNameInput.setValue("");
            registrationDateInput.setValue("");
            maxChoicesInput.setValue("5");
            startDateInput.setValue("");
            endDateInput.setValue("");
            feedbackLabel.setText("Aucune campagne active.");
            statusValueLabel.setText("—");
            updateButtonVisibility(null);
            return;
        }
        campaignNameInput.setValue(campaign.getName());
        registrationDateInput.setValue(campaign.getRegistrationDay());
        maxChoicesInput.setValue(String.valueOf(campaign.getMaxChoices()));
        startDateInput.setValue(campaign.getStartDate());
        endDateInput.setValue(campaign.getEndDate());
        feedbackLabel.setText("");
        updateStatusLabel(campaign.getStatus());
        updateButtonVisibility(campaign.getStatus());
    }

    /**
     * Sets the feedback message displayed below the save button.
     * Typically used to show validation errors or success confirmations.
     *
     * @param msg the message text to display; null or empty string clears the feedback label
     */
    public void setFeedback(String msg) {
        feedbackLabel.setText(msg == null ? "" : msg);
    }

    private void changeStatus(String newStatus) {
        if (currentCampaign == null) {
            feedbackLabel.setText("Aucune campagne active.");
            return;
        }
        ServiceResult result = campaignService.changeStatus(currentCampaign.getId(), newStatus);
        if (result.isSuccess()) {
            currentCampaign.setStatus(newStatus);
            updateStatusLabel(newStatus);
            updateButtonVisibility(newStatus);
            feedbackLabel.setColor(new Color(34, 197, 94));
            feedbackLabel.setText("Statut mis a jour : " + newStatus);
        } else {
            feedbackLabel.setColor(new Color(239, 68, 68));
            feedbackLabel.setText("Echec mise a jour statut: " + result.getMessage());
        }
    }

    private void runAutoAssignment() {
        if (currentCampaign == null) {
            feedbackLabel.setText("Aucune campagne active.");
            return;
        }
        if (!"CLOSED".equals(currentCampaign.getStatus())) {
            feedbackLabel.setColor(new Color(239, 68, 68));
            feedbackLabel.setText("La campagne doit etre en statut CLOSED avant le traitement.");
            return;
        }
        feedbackLabel.setColor(new Color(160, 175, 202));
        feedbackLabel.setText("Traitement en cours...");
        autoBtn.setEnabled(false);

        int campaignId = currentCampaign.getId();
        new Thread(() -> {
            ServiceResult result = assignmentService.runAutoAssignment(campaignId);
            javax.swing.SwingUtilities.invokeLater(() -> {
                autoBtn.setEnabled(true);
                if (result.isSuccess()) {
                    currentCampaign.setStatus("PROCESSING");
                    updateStatusLabel("PROCESSING");
                    updateButtonVisibility("PROCESSING");
                    feedbackLabel.setColor(new Color(34, 197, 94));
                    feedbackLabel.setText(result.getMessage());
                } else {
                    feedbackLabel.setColor(new Color(239, 68, 68));
                    feedbackLabel.setText(result.getMessage());
                }
            });
        }, "auto-assignment").start();
    }

    private void updateStatusLabel(String status) {
        statusValueLabel.setText("Statut : " + (status == null ? "—" : status));
        Color c;
        switch (status == null ? "" : status) {
            case "OPEN"       -> c = new Color(34, 197, 94);
            case "CLOSED"     -> c = new Color(245, 158, 11);
            case "PROCESSING" -> c = new Color(59, 130, 246);
            case "VALIDATED"  -> c = new Color(168, 85, 247);
            case "ARCHIVED"   -> c = new Color(100, 116, 139);
            default           -> c = new Color(239, 244, 252);
        }
        statusValueLabel.setColor(c);
        statusValueLabel.invalidate();
    }

    private void updateButtonVisibility(String status) {
        // Reset all buttons to invisible first
        preparationBtn.setVisible(false);
        openBtn.setVisible(false);
        closeBtn.setVisible(false);
        autoBtn.setVisible(false);
        validateBtn.setVisible(false);
        archiveBtn.setVisible(false);

        if (status == null) return;
        
        // Show buttons based on allowed transitions from current status
        if (campaignService.isAllowedTransition(status, "PREPARATION")) {
            preparationBtn.setVisible(true);
        }
        if (campaignService.isAllowedTransition(status, "OPEN")) {
            openBtn.setVisible(true);
        }
        if (campaignService.isAllowedTransition(status, "CLOSED")) {
            closeBtn.setVisible(true);
        }
        if (campaignService.isAllowedTransition(status, "VALIDATED")) {
            validateBtn.setVisible(true);
        }
        if (campaignService.isAllowedTransition(status, "ARCHIVED")) {
            archiveBtn.setVisible(true);
        }
        // Auto assignment only available from CLOSED
        if ("CLOSED".equals(status)) {
            autoBtn.setVisible(true);
        }
    }

    private PrimaryButton statusBtn(String text, Color bg, Runnable action) {
        PrimaryButton b = new PrimaryButton(text, 0, 0, 200, 30, action);
        b.setBackground(bg);
        b.setForeground(new Color(239, 244, 252));
        b.setVisible(false);
        return b;
    }

    /**
     * Updates the layout of all form and status components based on the available width.
     * Recalculates bounds for input fields, buttons, and cards to ensure
     * proper horizontal scaling within the parent container.
     *
     * @param mainW the available width in pixels for the component layout;
     *              used to scale input fields and button positions proportionally
     */
    public void onResize(int mainW) {
        root.setBounds(0, 0, mainW, 520);
        formCard.setBounds(0, 0, mainW, 310);
        campaignNameInput.setBounds(16, 42, mainW - 32, 62);
        registrationDateInput.setBounds(16, 108, 220, 62);
        maxChoicesInput.setBounds(252, 108, 180, 62);
        startDateInput.setBounds(16, 174, 220, 62);
        endDateInput.setBounds(252, 174, 180, 62);
        saveCampaignButton.setBounds(16, 250, 130, 34);
        feedbackLabel.setBounds(16, 288, mainW - 32, 16);

        statusCard.setBounds(0, 320, mainW, 190);
        int bx = 16;
        int by = 64;
        int bwidth = 200;
        int bheight = 30;
        int spacing = 10;
        
        preparationBtn.setBounds(bx, by, bwidth, bheight);
        openBtn.setBounds(bx, by + (bheight + spacing), bwidth, bheight);
        closeBtn.setBounds(bx, by + 2 * (bheight + spacing), bwidth, bheight);
        autoBtn.setBounds(bx, by + 3 * (bheight + spacing), bwidth, bheight);
        validateBtn.setBounds(bx, by + 4 * (bheight + spacing), bwidth, bheight);
        archiveBtn.setBounds(bx, by + 5 * (bheight + spacing), bwidth, bheight);
    }
}