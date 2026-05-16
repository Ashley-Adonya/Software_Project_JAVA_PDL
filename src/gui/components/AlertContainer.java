package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import components.Label;
import main.BaseComp;

public class AlertContainer extends SurfaceCard {
    private final BaseComp alertsList;
    private boolean darkMode = true;

    public AlertContainer() {
        super(0, 0, 100, 100, new Color(22, 28, 39), new Color(52, 63, 92), 10);
        this.alertsList = new BaseComp(null);
        addChild(alertsList);
    }

    public BaseComp getRoot() { return this; }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (dark) {
            setBackground(new Color(22, 28, 39));
            setBorderColor(new Color(52, 63, 92));
        } else {
            setBackground(Color.WHITE);
            setBorderColor(new Color(226, 230, 238));
        }
        invalidate();
    }

    public void setAlerts(List<AlertItem> alerts) {
        clearChildren(alertsList);
        if (alerts == null || alerts.isEmpty()) {
            Label empty = new Label("Aucune alerte", 0, 0, getWidth() - 16, 24);
            empty.setFont(new Font("Dialog", Font.PLAIN, 12));
            empty.setColor(darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139));
            alertsList.addChild(empty);
            return;
        }

        int y = 0;
        for (AlertItem alert : alerts) {
            SurfaceCard alertCard = new SurfaceCard(0, y, 100, 52,
                alert.type == AlertType.WARNING ? new Color(255, 245, 220) :
                alert.type == AlertType.ERROR ? new Color(255, 240, 240) :
                new Color(240, 248, 255),
                new Color(52, 63, 92), 6);

            Color iconColor = alert.type == AlertType.WARNING ? new Color(180, 120, 20) :
                              alert.type == AlertType.ERROR ? new Color(196, 61, 61) :
                              new Color(59, 130, 246);

            Label msgLabel = new Label(alert.message, 8, 6, 90, 20);
            msgLabel.setFont(new Font("Dialog", Font.BOLD, 11));
            msgLabel.setColor(darkMode ? new Color(235, 241, 255) : new Color(27, 39, 56));

            Label detailLabel = new Label(alert.detail == null ? "" : alert.detail, 8, 28, 90, 18);
            detailLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
            detailLabel.setColor(darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139));

            alertCard.addChild(msgLabel);
            alertCard.addChild(detailLabel);
            alertsList.addChild(alertCard);
            y += 58;
        }
        alertsList.setBounds(0, 0, 100, Math.max(1, y));
        invalidate();
    }

    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
    }

    private void clearChildren(BaseComp parent) {
        for (main.BaseComp c : new ArrayList<>(parent.getChildrenList())) {
            parent.removeChild(c);
        }
    }

    public static class AlertItem {
        public AlertType type;
        public String message;
        public String detail;

        public AlertItem(AlertType type, String message, String detail) {
            this.type = type;
            this.message = message;
            this.detail = detail;
        }
    }

    public static enum AlertType {
        INFO, WARNING, ERROR
    }
}