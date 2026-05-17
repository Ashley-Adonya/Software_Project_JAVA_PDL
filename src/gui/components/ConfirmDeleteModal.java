package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;
import java.util.function.Runnable;

import components.Label;
import main.BaseComp;
import main.BaseWindow;
import gui.components.PrimaryButton;
import gui.components.SurfaceCard;

/**
 * Modal de confirmation pour les actions irréversibles comme la suppression.
 */
public class ConfirmDeleteModal {
    private final BaseWindow window;
    private final BaseComp root;
    private final SurfaceCard modalBackground;
    private final SurfaceCard modalCard;
    private final Label titleLabel;
    private final Label messageLabel;
    private final PrimaryButton cancelButton;
    private final PrimaryButton confirmButton;
    
    private Runnable onConfirm = () -> {};
    private Runnable onCancel = () -> {};

    public ConfirmDeleteModal(BaseWindow window) {
        this.window = window;
        this.root = new BaseComp(null);
        
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
            hide();
        });
        this.cancelButton.setBackground(new Color(80, 80, 80));
        
        this.confirmButton = new PrimaryButton("Confirmer", 220, 130, 120, 36, () -> {
            if (onConfirm != null) onConfirm.run();
            hide();
        });
        this.confirmButton.setBackground(new Color(239, 68, 68));
        
        modalCard.addChild(titleLabel);
        modalCard.addChild(messageLabel);
        modalCard.addChild(cancelButton);
        modalCard.addChild(confirmButton);
        
        root.addChild(modalBackground);
        root.addChild(modalCard);
    }

    public BaseComp getRoot() { return root; }
    
    public void setOnConfirm(Runnable cb) { this.onConfirm = cb; }
    public void setOnCancel(Runnable cb) { this.onCancel = cb; }
    
    public void setMessage(String message) {
        this.messageLabel.setText(message);
    }
    
    public void show() {
        modalBackground.setBounds(0, 0, window.getContent().getWidth(), window.getContent().getHeight());
        int w = window.getContent().getWidth();
        int h = window.getContent().getHeight();
        modalCard.setBounds((w - 400) / 2, (h - 200) / 2, 400, 200);
        root.setVisible(true);
        window.requestRenderIfNeeded();
    }
    
    public void hide() {
        root.setVisible(false);
        window.requestRenderIfNeeded();
    }
}