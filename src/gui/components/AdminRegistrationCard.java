package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Button;
import components.Label;

public class AdminRegistrationCard extends SurfaceCard {
    private final Color TEXT_MAIN;
    private final Color TEXT_MUTED;

    private final Label studentNameLabel;
    private final Label sessionInfoLabel;
    private final Label statusLabel;
    private final Button registerButton;
    private final Button removeButton;

    private Runnable onRegister;
    private Runnable onRemove;

    public AdminRegistrationCard(int width, int height, Color cardBg, Color borderColor) {
        super(0, 0, width, height, cardBg, borderColor, 12);
        this.TEXT_MAIN = new Color(235, 241, 255);
        this.TEXT_MUTED = new Color(151, 166, 194);

        this.studentNameLabel = new Label("", 12, 8, width - 100, 20);
        this.studentNameLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        this.studentNameLabel.setColor(TEXT_MAIN);

        this.sessionInfoLabel = new Label("", 12, 32, width - 100, 18);
        this.sessionInfoLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.sessionInfoLabel.setColor(TEXT_MUTED);

        this.statusLabel = new Label("", 12, 54, width - 100, 16);
        this.statusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));

        this.registerButton = new Button("Inscrire", width - 90, 8, 80, 24, () -> {
            if (onRegister != null) onRegister.run();
        });
        registerButton.setBackground(new Color(34, 197, 94));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Dialog", Font.BOLD, 11));

        this.removeButton = new Button("Suppr", width - 90, 36, 80, 24, () -> {
            if (onRemove != null) onRemove.run();
        });
        removeButton.setBackground(new Color(239, 68, 68));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFont(new Font("Dialog", Font.BOLD, 11));
        removeButton.setVisible(false);

        addChild(studentNameLabel);
        addChild(sessionInfoLabel);
        addChild(statusLabel);
        addChild(registerButton);
        addChild(removeButton);
    }

    public void setData(String studentName, String sessionInfo, boolean isRegistered, int allocated, int capacity) {
        studentNameLabel.setText(studentName);
        sessionInfoLabel.setText(sessionInfo);

        if (isRegistered) {
            statusLabel.setText("Inscrit (" + allocated + "/" + capacity + ")");
            statusLabel.setColor(new Color(34, 197, 94));
            registerButton.setVisible(false);
            removeButton.setVisible(true);
        } else {
            statusLabel.setText("Non inscrit | Places: " + (capacity - allocated) + "/" + capacity);
            if (allocated >= capacity) {
                statusLabel.setColor(new Color(239, 68, 68));
                registerButton.setEnabled(false);
            } else {
                statusLabel.setColor(TEXT_MUTED);
                registerButton.setEnabled(true);
            }
            registerButton.setVisible(true);
            removeButton.setVisible(false);
        }
    }

    public void setOnRegister(Runnable r) {
        this.onRegister = r;
    }

    public void setOnRemove(Runnable r) {
        this.onRemove = r;
    }
}