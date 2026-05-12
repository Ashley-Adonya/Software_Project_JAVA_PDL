package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;

public class SessionOfferCard extends SurfaceCard {
    private final Label title;
    private final Label meta;
    private final Label room;
    private final Label capacity;
    private final PrimaryButton addButton;

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

    public void setData(String title, String meta, String room, String capacity) {
        this.title.setText(title == null ? "Session" : title);
        this.meta.setText(meta == null ? "" : meta);
        this.room.setText(room == null ? "" : room);
        this.capacity.setText(capacity == null ? "" : capacity);
        invalidate();
    }

    public void setActionText(String text) {
        this.addButton.setText(text == null ? "" : text);
        invalidate();
    }

    public void setActionBackground(Color bg) {
        if (bg == null) {
            return;
        }
        this.addButton.setBackground(bg);
        invalidate();
    }
}
