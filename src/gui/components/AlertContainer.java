package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import components.Label;
import main.BaseComp;

public class AlertContainer extends SurfaceCard {
    private final BaseComp alertsList;
    private List<AlertItem> currentAlerts = new ArrayList<>();
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
        redrawAlerts(getWidth(), getHeight());
    }

    public void setAlerts(List<AlertItem> alerts) {
        this.currentAlerts = alerts != null ? alerts : new ArrayList<>();
        redrawAlerts(Math.max(getWidth(), 300), getHeight());
    }

    private void redrawAlerts(int width, int height) {
        clearChildren(alertsList);
        alertsList.setBounds(0, 0, width, height);

        if (currentAlerts.isEmpty()) {
            Label empty = new Label("Aucune alerte pour le moment.", 16, 16, width - 32, 24);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            empty.setColor(darkMode ? new Color(151, 166, 194) : new Color(100, 116, 139));
            alertsList.addChild(empty);
            return;
        }

        int y = 0;
        int cardWidth = Math.max(10, width);
        for (AlertItem alert : currentAlerts) {
            SurfaceCard alertCard = new SurfaceCard(0, y, cardWidth, 58,
                alert.type == AlertType.WARNING ? new Color(255, 245, 220) :
                alert.type == AlertType.ERROR ? new Color(255, 240, 240) :
                new Color(240, 248, 255),
                new Color(52, 63, 92), 8);

            Label msgLabel = new Label(alert.message, 16, 10, cardWidth - 32, 20);
            msgLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            msgLabel.setColor(darkMode ? new Color(22, 28, 39) : new Color(27, 39, 56)); // Toujours foncé pour la lisibilité sur fond clair d'alerte

            Label detailLabel = new Label(alert.detail == null ? "" : alert.detail, 16, 32, cardWidth - 32, 18);
            detailLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
            detailLabel.setColor(new Color(71, 85, 105));

            alertCard.addChild(msgLabel);
            alertCard.addChild(detailLabel);
            alertsList.addChild(alertCard);
            y += 66; // Espacement de 8px
        }
        invalidate();
    }

    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
        redrawAlerts(width, height);
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
            this.type = type; this.message = message; this.detail = detail;
        }
    }
    public static enum AlertType { INFO, WARNING, ERROR }
}