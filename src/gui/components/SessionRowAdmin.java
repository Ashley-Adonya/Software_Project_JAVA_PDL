package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Button;
import components.Label;

public class SessionRowAdmin extends SurfaceCard {
    private final SurfaceCard stripe;
    private final Label title;
    private final Label timeRoom;
    private final Label fill;
    private final Button editButton;
    private final Button manageButton;
    private final Button deleteButton;

    public SessionRowAdmin(Runnable onEdit, Runnable onDelete) {
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

        this.deleteButton = new Button("Suppr", 0, 0, 60, 26, onDelete);
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

    public void setData(String title, String timeRoom, int fillRate, Color accentColor) {
        this.title.setText(title == null ? "Session" : title);
        this.timeRoom.setText(timeRoom == null ? "" : timeRoom);
        this.fill.setText(fillRate + "% rempli");
        Color useColor = accentColor == null ? new Color(124, 92, 255) : accentColor;
        stripe.setBackground(useColor);
        stripe.invalidate();
        invalidate();
    }

    public void setOnManage(Runnable action) {
        if (manageButton != null) {
            manageButton.setOnClick(action);
        }
    }
}
