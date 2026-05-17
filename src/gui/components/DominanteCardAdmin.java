package gui.components;

import java.awt.Color;
import java.awt.Font;
import main.BaseWindow;

import components.Button;
import components.Label;

/**
 * A card component used in the admin panel to display and manage a "dominante"
 * (academic major or specialization). It shows key information such as the
 * dominante code, name, description, number of sessions, total capacity,
 * current enrollment count, and fill rate. Two action buttons allow editing
 * or deleting the dominante, with the delete action prompting a confirmation
 * dialog before proceeding. The card supports both light and dark colour
 * themes and dynamically adjusts its layout when its bounds change.
 */
public class DominanteCardAdmin extends SurfaceCard {
    private final SurfaceCard topAccent;
    private final SurfaceCard codeBadge;
    private final Label codeLabel;
    private final Label nameLabel;
    private final Label descLabel;
    private final Label sessionsLabel;
    private final Label capacityLabel;
    private final Label inscriptionsLabel;
    private final Label fillRateLabel;
    private final Button editButton;
    private final Button deleteButton;
    private boolean darkMode;

    /**
     * Constructs a new admin dominante card.
     *
     * @param onEdit   runnable invoked when the edit button is clicked
     * @param onDelete runnable invoked when the user confirms deletion through the
     *                 confirmation modal
     * @param window   the parent BaseWindow used to display the confirmation modal
     */
    public DominanteCardAdmin(Runnable onEdit, Runnable onDelete, BaseWindow window) {
        super(0, 0, 100, 100, Color.WHITE, new Color(226, 230, 238), 12);
        this.darkMode = true;

        this.topAccent = new SurfaceCard(0, 0, 100, 4, new Color(124, 92, 255), new Color(0, 0, 0, 0), 8);
        this.codeBadge = new SurfaceCard(0, 0, 34, 28, new Color(124, 92, 255), new Color(0, 0, 0, 0), 8);
        this.codeLabel = new Label("--", 0, 0, 34, 22);
        this.codeLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        this.codeLabel.setColor(Color.WHITE);

        this.nameLabel = new Label("Dominante", 0, 0, 220, 22);
        this.nameLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        this.nameLabel.setColor(new Color(30, 37, 52));

        this.descLabel = new Label("Description", 0, 0, 260, 34);
        this.descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.descLabel.setColor(new Color(113, 123, 139));

        this.sessionsLabel = new Label("Sessions 0", 0, 0, 120, 18);
        this.sessionsLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.capacityLabel = new Label("Capacite 0", 0, 0, 120, 18);
        this.capacityLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.inscriptionsLabel = new Label("Inscriptions 0", 0, 0, 140, 18);
        this.inscriptionsLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        this.fillRateLabel = new Label("0% rempli", 0, 0, 120, 18);
        this.fillRateLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        this.fillRateLabel.setColor(new Color(120, 130, 146));

        this.editButton = new Button("Editer", 0, 0, 74, 28, onEdit);
        this.editButton.setBackground(new Color(240, 243, 248));
        this.editButton.setForeground(new Color(67, 76, 91));

        // Bouton suppr avec confirmation
        this.deleteButton = new Button("Suppr", 0, 0, 74, 28, () -> {
            System.out.println("Deleting dominante with id: " + codeLabel.getText());
            ConfirmDeleteModal confirmModal = new ConfirmDeleteModal(window);
            confirmModal.setMessage("Êtes-vous sûr de vouloir supprimer cette dominante ? Cette action est irréversible.");
            confirmModal.setOnConfirm(onDelete);
            confirmModal.show();
        });
        this.deleteButton.setBackground(new Color(255, 240, 240));
        this.deleteButton.setForeground(new Color(196, 61, 61));

        addChild(topAccent);
        addChild(codeBadge);
        addChild(codeLabel);
        addChild(nameLabel);
        addChild(descLabel);
        addChild(sessionsLabel);
        addChild(capacityLabel);
        addChild(inscriptionsLabel);
        addChild(fillRateLabel);
        addChild(editButton);
        addChild(deleteButton);

        setDarkMode(true);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (topAccent == null || codeBadge == null || codeLabel == null || nameLabel == null || descLabel == null
                || sessionsLabel == null || capacityLabel == null || inscriptionsLabel == null || fillRateLabel == null
                || editButton == null || deleteButton == null) {
            return;
        }
        topAccent.setBounds(0, 0, width, 4);
        codeBadge.setBounds(16, 18, 34, 28);
        codeLabel.setBounds(24, 22, 28, 22);
        nameLabel.setBounds(16, 60, width - 32, 22);
        descLabel.setBounds(16, 84, width - 32, 34);
        sessionsLabel.setBounds(16, 126, width - 32, 18);
        capacityLabel.setBounds(16, 144, width - 32, 18);
        inscriptionsLabel.setBounds(16, 162, width - 32, 18);
        fillRateLabel.setBounds(16, height - 20, width - 32, 16);
        deleteButton.setBounds(width - 92, 16, 74, 28);
        editButton.setBounds(width - 174, 16, 74, 28);
    }

    /**
     * Populates the card fields with the given dominante data.
     * The displayed code is trimmed to at most three uppercase characters.
     * If any string argument is null or blank a sensible fallback is shown.
     *
     * @param code         the dominante code (will be truncated to 3 characters)
     * @param name         the dominante display name
     * @param desc         a short description of the dominante
     * @param sessions     the number of sessions associated with this dominante
     * @param capacity     the total student capacity across sessions
     * @param inscriptions the number of students currently enrolled
     * @param fillRate     the fill rate as a percentage (0-100)
     * @param accentColor  the colour used for the top accent bar and code badge;
     *                     if null a default purple is used
     */
    public void setData(String code, String name, String desc, int sessions, int capacity, int inscriptions, int fillRate,
            Color accentColor) {
        String safeCode = code == null || code.isBlank() ? "--" : code.trim().toUpperCase();
        codeLabel.setText(safeCode.length() > 3 ? safeCode.substring(0, 3) : safeCode);
        nameLabel.setText(name == null || name.isBlank() ? "Dominante" : name);
        descLabel.setText(desc == null || desc.isBlank() ? "-" : desc);
        sessionsLabel.setText("Sessions " + sessions);
        capacityLabel.setText("Capacite totale " + capacity);
        inscriptionsLabel.setText("Inscriptions " + inscriptions);
        fillRateLabel.setText(fillRate + "% rempli");

        Color useColor = accentColor == null ? new Color(124, 92, 255) : accentColor;
        topAccent.setBackground(useColor);
        codeBadge.setBackground(useColor);
        topAccent.invalidate();
        codeBadge.invalidate();
        invalidate();
    }

    /**
     * Toggles the visual theme between dark mode and light mode for this card
     * and all its child components. Updates background, border, text, and button
     * colours accordingly.
     *
     * @param dark true to apply the dark theme, false for the light theme
     */
    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (darkMode) {
            setBackground(new Color(22, 28, 39));
            setBorderColor(new Color(48, 60, 82));
            nameLabel.setColor(new Color(235, 241, 255));
            descLabel.setColor(new Color(151, 166, 194));
            sessionsLabel.setColor(new Color(214, 223, 243));
            capacityLabel.setColor(new Color(214, 223, 243));
            inscriptionsLabel.setColor(new Color(214, 223, 243));
            fillRateLabel.setColor(new Color(173, 189, 220));
            editButton.setBackground(new Color(40, 51, 73));
            editButton.setForeground(new Color(219, 230, 253));
            deleteButton.setBackground(new Color(78, 43, 47));
            deleteButton.setForeground(new Color(253, 209, 209));
        } else {
            setBackground(Color.WHITE);
            setBorderColor(new Color(226, 230, 238));
            nameLabel.setColor(new Color(30, 37, 52));
            descLabel.setColor(new Color(113, 123, 139));
            sessionsLabel.setColor(new Color(76, 87, 104));
            capacityLabel.setColor(new Color(76, 87, 104));
            inscriptionsLabel.setColor(new Color(76, 87, 104));
            fillRateLabel.setColor(new Color(120, 130, 146));
            editButton.setBackground(new Color(240, 243, 248));
            editButton.setForeground(new Color(67, 76, 91));
            deleteButton.setBackground(new Color(255, 240, 240));
            deleteButton.setForeground(new Color(196, 61, 61));
        }
        invalidate();
    }
}
