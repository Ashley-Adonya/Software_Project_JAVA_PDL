package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;

import components.Label;
import main.BaseComp;
import main.BaseWindow;
import gui.components.PrimaryButton;
import gui.components.SurfaceCard;

/**
 * Modal confirmation dialog for irreversible actions such as deleting records.
 * Displays a centered popup card over a semi-transparent background overlay,
 * with customizable title, message, confirm and cancel buttons.
 * <p>
 * This component uses the window's modal layer system (openModal/closeTopLayer)
 * to present itself above the normal content. It is typically used throughout
 * the admin dashboard to confirm destructive operations and prevent accidental data loss.
 * <p>
 * Key features:
 * - Customizable confirmation message
 * - Callback-based confirm and cancel actions
 * - Semi-transparent background overlay
 * - Centered card layout responsive to window dimensions
 * - Automatic layer cleanup on dismiss
 */
public class ConfirmDeleteModal extends BaseComp {
    private final BaseWindow window;
    private final SurfaceCard modalBackground;
    private final SurfaceCard modalCard;
    private final Label titleLabel;
    private final Label messageLabel;
    private final PrimaryButton cancelButton;
    private final PrimaryButton confirmButton;
    
    private Runnable onConfirm = () -> {};
    private Runnable onCancel = () -> {};

    /**
     * Constructs a confirmation modal dialog attached to the given window.
     * Creates a semi-transparent full-screen background overlay and a centered
     * modal card with a title, message, cancel button, and confirm button.
     * The modal is not shown until {@link #show()} is called.
     *
     * @param window the parent BaseWindow used for modal layer management (openModal/closeTopLayer)
     */
    public ConfirmDeleteModal(BaseWindow window) {
        System.out.println("Initializing ConfirmDeleteModal");
        super(null);
        this.window = window;
        
        // Fond semi-transparent qui couvre toute la fenêtre
        this.modalBackground = new SurfaceCard(0, 0, 100, 100, 
            new Color(0, 0, 0, 120), new Color(0, 0, 0, 0), 0);
        
        // Carte du modal
        this.modalCard = new SurfaceCard(0, 0, 400, 200, 
            new Color(22, 28, 39), new Color(52, 63, 92), 16);
        
        this.titleLabel = new Label("Confirmation", 20, 20, 360, 24);
        this.titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        this.titleLabel.setColor(new Color(239, 244, 252));
        
        this.messageLabel = new Label("Êtes-vous sûr de vouloir effectuer cette action ?", 20, 60, 360, 40);
        this.messageLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        this.messageLabel.setColor(new Color(200, 210, 230));
        
        this.cancelButton = new PrimaryButton("Annuler", 60, 130, 120, 36, () -> {
            if (onCancel != null) onCancel.run();
            window.closeTopLayer();
        });
        this.cancelButton.setBackground(new Color(80, 80, 80));
        
        this.confirmButton = new PrimaryButton("Confirmer", 220, 130, 120, 36, () -> {
            System.out.println("Confirm button clicked in ConfirmDeleteModal");
            onConfirm.run();
            window.closeTopLayer();
        });
        this.confirmButton.setBackground(new Color(239, 68, 68));
        System.out.println("ConfirmDeleteModal initialized");
        modalCard.addChild(titleLabel);
        modalCard.addChild(messageLabel);
        modalCard.addChild(cancelButton);
        modalCard.addChild(confirmButton);
        
        addChild(modalBackground);
        addChild(modalCard);
    }

    /**
     * Registers a callback to execute when the confirm button is clicked.
     * The callback runs before the modal is automatically dismissed.
     *
     * @param cb the runnable to execute on confirmation; may be null-safe
     */
    public void setOnConfirm(Runnable cb) { this.onConfirm = cb; }

    /**
     * Registers a callback to execute when the cancel button is clicked.
     * The callback runs before the modal is automatically dismissed.
     *
     * @param cb the runnable to execute on cancellation; may be null-safe
     */
    public void setOnCancel(Runnable cb) { this.onCancel = cb; }

    /**
     * Updates the message text displayed in the modal body to describe
     * the specific action being confirmed.
     *
     * @param message the new confirmation message; replaces the previous text
     */
    public void setMessage(String message) {
        this.messageLabel.setText(message);
    }
    
    /**
     * Displays the modal by sizing it to match the window content dimensions,
     * centering the modal card, and opening it as a top-layer overlay via
     * {@link BaseWindow#openModal(BaseComp)}.
     * The background overlay covers the entire content area with semi-transparent black.
     */
    public void show() {
        // Set our own bounds to match the window content
        setBounds(0, 0, window.getContent().getWidth(), window.getContent().getHeight());
        
        modalBackground.setBounds(0, 0, window.getContent().getWidth(), window.getContent().getHeight());
        int w = window.getContent().getWidth();
        int h = window.getContent().getHeight();
        modalCard.setBounds((w - 400) / 2, (h - 200) / 2, 400, 200);
        window.openModal(this);
    }
    
    /**
     * Hides the modal by closing the top window layer.
     * Equivalent to calling {@link BaseWindow#closeTopLayer()}.
     */
    public void hide() {
        window.closeTopLayer();
    }
}