package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;

/**
 * A card component that displays a session offering with its title, meta
 * information, room, and capacity. An "Ajouter" (add) button allows the
 * user to trigger an action (e.g. adding the session to a selection). The
 * button text and background colour can be changed dynamically.
 */
public class SessionOfferCard extends SurfaceCard {
    private final Label title;
    private final Label meta;
    private final Label room;
    private final Label capacity;
    private final PrimaryButton addButton;

    /**
     * Constructs a session offer card with the given information.
     *
     * @param title    the session title
     * @param meta     additional meta information (e.g. time, date)
     * @param room     the room or location of the session
     * @param capacity the capacity description (e.g. "30 places")
     * @param onAdd    runnable invoked when the add button is clicked
     */
    public SessionOfferCard(String title, String meta, String room, String capacity, Runnable onAdd) {
        super(0, 0, 100, 90, Color.WHITE, new Color(226, 230, 238), 12);

        this.title = new Label(title, 0, 0, 100, 22);
        this.title.setFont(new Font("Dialog", Font.BOLD, 18));
        this.title.setColor(new Color(26, 33, 46));

        this.meta = new Label(meta, 0, 0, 100, 16);
        this.meta.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.meta.setColor(new Color(103, 113, 131));

        this.room = new Label(room, 0, 0, 100, 16);
        this.room.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.room.setColor(new Color(103, 113, 131));

        this.capacity = new Label(capacity, 0, 0, 100, 16);
        this.capacity.setFont(new Font("Dialog", Font.BOLD, 12));
        this.capacity.setColor(new Color(95, 103, 121));

        this.addButton = new PrimaryButton("Ajouter", 0, 0, 96, 28, onAdd);
        this.addButton.setBackground(new Color(127, 132, 146));

        addChild(this.title);
        addChild(this.meta);
        addChild(this.room);
        addChild(this.capacity);
        addChild(this.addButton);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (title == null || meta == null || room == null || capacity == null || addButton == null) {
            return;
        }
        title.setBounds(16, 10, width - 150, 22);
        meta.setBounds(16, 34, width - 32, 16);
        room.setBounds(16, 52, width - 32, 16);
        capacity.setBounds(width - 170, 52, 100, 16);
        addButton.setBounds(width - 110, 28, 92, 32);
    }

    /**
     * Updates the displayed session data.
     *
     * @param title    the new session title; null is replaced by "Session"
     * @param meta     the new meta information; null is treated as empty
     * @param room     the new room information; null is treated as empty
     * @param capacity the new capacity text; null is treated as empty
     */
    public void setData(String title, String meta, String room, String capacity) {
        this.title.setText(title == null ? "Session" : title);
        this.meta.setText(meta == null ? "" : meta);
        this.room.setText(room == null ? "" : room);
        this.capacity.setText(capacity == null ? "" : capacity);
        invalidate();
    }

    /**
     * Changes the text displayed on the action button.
     *
     * @param text the new button text; null is treated as an empty string
     */
    public void setActionText(String text) {
        this.addButton.setText(text == null ? "" : text);
        invalidate();
    }

    /**
     * Changes the background colour of the action button.
     *
     * @param bg the new background colour; if null the method does nothing
     */
    public void setActionBackground(Color bg) {
        if (bg == null) {
            return;
        }
        this.addButton.setBackground(bg);
        invalidate();
    }
}
