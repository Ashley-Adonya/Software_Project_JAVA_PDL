package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;

/**
 * A KPI (Key Performance Indicator) card component that displays a metric
 * with a bold title, a large value, and a coloured subtitle. It is designed
 * for dashboard overviews and supports both dark and light colour themes.
 */
public class KpiCard extends SurfaceCard {
    private final Label title;
    private final Label value;
    private final Label subtitle;

    /**
     * Constructs a KPI card with the specified metric data.
     *
     * @param title    the label describing the metric (e.g. "Total sessions")
     * @param value    the initial numeric or string value (e.g. "42")
     * @param subtitle a short contextual subtitle rendered in the accent colour
     * @param accent   the colour used for the subtitle and visual accent
     */
    public KpiCard(String title, String value, String subtitle, Color accent) {
        super(0, 0, 100, 100, new Color(22, 28, 39), new Color(48, 60, 82), 12);

        this.title = new Label(title, 0, 0, 100, 20);
        this.title.setFont(new Font("Dialog", Font.BOLD, 13));
        this.title.setColor(new Color(151, 166, 194));

        this.value = new Label(value, 0, 0, 100, 34);
        this.value.setFont(new Font("Dialog", Font.BOLD, 30));
        this.value.setColor(new Color(235, 241, 255));

        this.subtitle = new Label(subtitle, 0, 0, 100, 18);
        this.subtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.subtitle.setColor(accent);

        addChild(this.title);
        addChild(this.value);
        addChild(this.subtitle);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (title == null || value == null || subtitle == null) {
            return;
        }
        title.setBounds(14, 14, width - 28, 20);
        value.setBounds(14, 44, width - 28, 34);
        subtitle.setBounds(14, height - 28, width - 28, 18);
    }

    /**
     * Updates the displayed value.
     *
     * @param text the new value text; null is replaced by "0"
     */
    public void setValue(String text) {
        value.setText(text == null ? "0" : text);
        value.invalidate();
    }

    /**
     * Updates the subtitle text.
     *
     * @param text the new subtitle; null is treated as an empty string
     */
    public void setSubtitle(String text) {
        subtitle.setText(text == null ? "" : text);
        subtitle.invalidate();
    }

    /**
     * Toggles the visual theme between dark mode and light mode for the card
     * background, border, title, and value colours.
     *
     * @param dark true to apply the dark theme, false for the light theme
     */
    public void setDarkMode(boolean dark) {
        if (dark) {
            setBackground(new Color(22, 28, 39));
            setBorderColor(new Color(48, 60, 82));
            title.setColor(new Color(151, 166, 194));
            value.setColor(new Color(235, 241, 255));
        } else {
            setBackground(Color.WHITE);
            setBorderColor(new Color(226, 232, 240));
            title.setColor(new Color(100, 116, 139));
            value.setColor(new Color(15, 23, 42));
        }
        invalidate();
    }
}
