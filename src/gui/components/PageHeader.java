package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;
import main.BaseComp;

public class PageHeader extends BaseComp {
    private final Label title;
    private final Label subtitle;

    public PageHeader(String title, String subtitle) {
        super(null);
        this.title = new Label(title, 0, 0, 100, 26);
        this.title.setFont(new Font("Dialog", Font.BOLD, 30));
        this.title.setColor(new Color(25, 32, 48));

        this.subtitle = new Label(subtitle, 0, 0, 100, 18);
        this.subtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.subtitle.setColor(new Color(113, 122, 137));

        addChild(this.title);
        addChild(this.subtitle);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (title == null || subtitle == null) {
            return;
        }
        title.setBounds(0, 0, width, 28);
        subtitle.setBounds(0, 30, width, 18);
    }

    public void setSubtitle(String text) {
        subtitle.setText(text == null ? "" : text);
        subtitle.invalidate();
    }

    public void setDarkMode(boolean dark) {
        if (dark) {
            title.setColor(new Color(235, 241, 255));
            subtitle.setColor(new Color(151, 166, 194));
        } else {
            title.setColor(new Color(25, 32, 48));
            subtitle.setColor(new Color(113, 122, 137));
        }
        invalidate();
    }
}
