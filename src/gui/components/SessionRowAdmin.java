package gui.components;

import java.awt.Color;
import java.awt.Font;
import main.BaseWindow;

import components.Button;
import components.Label;

/**
 * A row component used in the admin panel to display and manage a single
 * session. It shows the session title, time and room information, and the
 * current fill rate. Three action buttons provide edit, manage, and delete
 * functionality, with the delete action guarded by a confirmation modal.
 * The component supports both light and dark colour themes.
 */
public class SessionRowAdmin extends SurfaceCard {
    private final SurfaceCard stripe;
    private final Label title;
    private final Label timeRoom;
    private final Label fill;
    private final Button editButton;
    private final Button manageButton;
    private final Button deleteButton;

    /**
     * Constructs a new session row for the admin panel.
     *
     * @param onEdit   runnable invoked when the edit button is clicked
     * @param onDelete runnable invoked when the user confirms deletion through the
     *                 confirmation modal
     * @param window   the parent BaseWindow used to display the confirmation modal
     */
    public SessionRowAdmin(Runnable onEdit, Runnable onDelete, BaseWindow window) {
        super(0, 0, 100, 66, Color.WHITE, new Color(231, 235, 242), 10);

        this.stripe = new SurfaceCard(0, 0, 4, 44, new Color(124, 92, 255), new Color(124, 92, 255), 4);
        this.title = new Label("Session", 0, 0, 220, 22);
        this.title.setFont(new Font("Dialog", Font.BOLD, 14));
        this.title.setColor(new Color(32, 40, 55));

        this.timeRoom = new Label("09:00 - 09:30  |  Amphi A", 0, 0, 260, 16);
        this.timeRoom.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.timeRoom.setColor(new Color(112, 122, 138));

        this.fill = new Label("0% rempli", 0, 0, 100, 16);
        this.fill.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.fill.setColor(new Color(112, 122, 138));

        this.editButton = new Button("Editer", 0, 0, 60, 26, onEdit);
        this.editButton.setBackground(new Color(240, 243, 248));
        this.editButton.setForeground(new Color(67, 76, 91));

        this.manageButton = new Button("Gerer", 0, 0, 60, 26, null);
        this.manageButton.setBackground(new Color(59, 130, 246));
        this.manageButton.setForeground(Color.WHITE);

        // Bouton suppr avec confirmation
        this.deleteButton = new Button("Suppr", 0, 0, 60, 26, () -> {
            ConfirmDeleteModal confirmModal = new ConfirmDeleteModal(window);
            confirmModal.setMessage("Êtes-vous sûr de vouloir supprimer cette session ? Cette action est irréversible.");
            confirmModal.setOnConfirm(onDelete);
            confirmModal.show();
        });
        this.deleteButton.setBackground(new Color(255, 240, 240));
        this.deleteButton.setForeground(new Color(196, 61, 61));

        addChild(stripe);
        addChild(title);
        addChild(timeRoom);
        addChild(fill);
        addChild(editButton);
        addChild(manageButton);
        addChild(deleteButton);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (stripe == null || title == null || timeRoom == null || fill == null || editButton == null || manageButton == null || deleteButton == null) {
            return;
        }
        stripe.setBounds(12, 11, 4, height - 22);
        title.setBounds(24, 12, width - 220, 22);
        timeRoom.setBounds(24, 34, width - 220, 16);
        fill.setBounds(width - 218, 12, 130, 16);
        editButton.setBounds(width - 210, 28, 56, 26);
        manageButton.setBounds(width - 148, 28, 56, 26);
        deleteButton.setBounds(width - 86, 28, 56, 26);
    }

    /**
     * Populates the row fields with the given session data.
     *
     * @param title       the session title
     * @param timeRoom    the time and room information string (e.g. "09:00-09:30 | Amphi A")
     * @param fillRate    the fill rate percentage to display
     * @param accentColor the colour used for the left stripe; if null a default
     *                    purple is used
     */
    public void setData(String title, String timeRoom, int fillRate, Color accentColor) {
        this.title.setText(title == null ? "Session" : title);
        this.timeRoom.setText(timeRoom == null ? "" : timeRoom);
        this.fill.setText(fillRate + "% rempli");
        Color useColor = accentColor == null ? new Color(124, 92, 255) : accentColor;
        stripe.setBackground(useColor);
        stripe.invalidate();
        invalidate();
    }

    /**
     * Sets the action to run when the "Gerer" (manage) button is clicked.
     *
     * @param action the runnable to invoke on manage; may be null to clear the
     *               handler
     */
    public void setOnManage(Runnable action) {
        if (manageButton != null) {
            manageButton.setOnClick(action);
        }
    }

    /**
     * Toggles the visual theme between dark mode and light mode for this row
     * and all its child components. Updates background, border, text, and
     * button colours accordingly.
     *
     * @param dark true to apply the dark theme, false for the light theme
     */
    public void setDarkMode(boolean dark) {
        if (dark) {
            setBackground(new Color(22, 28, 39));
            setBorderColor(new Color(48, 60, 82));
            title.setColor(new Color(235, 241, 255));
            timeRoom.setColor(new Color(151, 166, 194));
            fill.setColor(new Color(151, 166, 194));
            editButton.setBackground(new Color(40, 51, 73));
            editButton.setForeground(new Color(219, 230, 253));
            manageButton.setBackground(new Color(59, 130, 246));
            manageButton.setForeground(Color.WHITE);
            deleteButton.setBackground(new Color(78, 43, 47));
            deleteButton.setForeground(new Color(253, 209, 209));
        } else {
            setBackground(Color.WHITE);
            setBorderColor(new Color(231, 235, 242));
            title.setColor(new Color(32, 40, 55));
            timeRoom.setColor(new Color(112, 122, 138));
            fill.setColor(new Color(112, 122, 138));
            editButton.setBackground(new Color(240, 243, 248));
            editButton.setForeground(new Color(67, 76, 91));
            manageButton.setBackground(new Color(59, 130, 246));
            manageButton.setForeground(Color.WHITE);
            deleteButton.setBackground(new Color(255, 240, 240));
            deleteButton.setForeground(new Color(196, 61, 61));
        }
        invalidate();
    }
}
