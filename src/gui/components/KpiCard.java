package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;

public class KpiCard extends SurfaceCard {
    private final Label title;
    private final Label value;
    private final Label subtitle;

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

    public void setValue(String text) {
        value.setText(text == null ? "0" : text);
        value.invalidate();
    }

    public void setSubtitle(String text) {
        subtitle.setText(text == null ? "" : text);
        subtitle.invalidate();
    }

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
