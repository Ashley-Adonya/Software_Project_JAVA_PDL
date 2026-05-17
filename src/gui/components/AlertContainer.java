package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import components.Label;
import main.BaseComp;

/**
 * A container component that displays a vertical list of alerts, each
 * categorised by severity type (INFO, WARNING, ERROR). Alerts are rendered
 * as coloured cards with a message and an optional detail line. When the
 * alert list is empty a placeholder message is shown. The container
 * supports both dark and light colour themes and dynamically rebuilds its
 * child components whenever alerts are updated or the container is resized.
 */
public class AlertContainer extends SurfaceCard {
    private final BaseComp alertsList;
    private List<AlertItem> currentAlerts = new ArrayList<>();
    private boolean darkMode = true;

    public AlertContainer() {
        super(0, 0, 100, 100, new Color(22, 28, 39), new Color(52, 63, 92), 10);
        this.alertsList = new BaseComp(null);
        addChild(alertsList);
    }

    /**
     * Returns the root component of this alert container.
     *
     * @return this container instance
     */
    public BaseComp getRoot() { return this; }

    /**
     * Toggles the visual theme between dark mode and light mode.
     *
     * @param dark true to apply the dark theme, false for the light theme
     */
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

    /**
     * Replaces the current list of alerts with a new one and redraws the
     * container to reflect the change.
     *
     * @param alerts the new list of alerts to display; if null an empty list
     *               is used
     */
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

    /**
     * Re-layouts the container and redraws all alerts when the parent
     * component is resized.
     *
     * @param width  the new width of the container
     * @param height the new height of the container
     */
    public void onResize(int width, int height) {
        setBounds(0, 0, width, height);
        redrawAlerts(width, height);
    }

    private void clearChildren(BaseComp parent) {
        for (main.BaseComp c : new ArrayList<>(parent.getChildrenList())) {
            parent.removeChild(c);
        }
    }

    /**
     * Represents a single alert item with a type, a short message, and an
     * optional detail description.
     */
    public static class AlertItem {
        public AlertType type;
        public String message;
        public String detail;
        /**
         * Constructs an alert item with the given properties.
         *
         * @param type    the severity type of the alert
         * @param message the main alert message
         * @param detail  an optional detailed description (may be null or empty)
         */
        public AlertItem(AlertType type, String message, String detail) {
            this.type = type; this.message = message; this.detail = detail;
        }
    }
    /**
     * Severity levels for an alert.
     */
    public static enum AlertType { INFO, WARNING, ERROR }
}