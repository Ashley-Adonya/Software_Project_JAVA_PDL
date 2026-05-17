package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;
import main.BaseComp;

/**
 * A page-level header component that displays a title and an optional
 * subtitle. The title is rendered in a large bold font while the subtitle
 * appears below in a smaller muted style. The component supports both
 * dark and light colour themes and automatically repositions its child
 * labels when resized.
 */
public class PageHeader extends BaseComp {
    private final Label title;
    private final Label subtitle;

    /**
     * Constructs a page header with the specified title and subtitle.
     *
     * @param title    the main header text
     * @param subtitle the secondary descriptive text, may be empty or null
     */
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

    /**
     * Updates the subtitle text displayed below the title.
     *
     * @param text the new subtitle text; null is treated as an empty string
     */
    public void setSubtitle(String text) {
        subtitle.setText(text == null ? "" : text);
        subtitle.invalidate();
    }

    /**
     * Updates the title text.
     *
     * @param text the new title text; null is treated as an empty string
     */
    public void setTitle(String text) {
        title.setText(text == null ? "" : text);
        title.invalidate();
    }

    /**
     * Toggles the visual theme between dark mode and light mode for the
     * title and subtitle colours.
     *
     * @param dark true to apply the dark theme, false for the light theme
     */
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
