package components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import main.BaseComp;

/**
 * A static text label component for the PDL application UI.
 * <p>
 * Renders a single line of text with configurable font and colour using the
 * custom rendering pipeline. When the text exceeds the available width it is
 * automatically trimmed and appended with an ellipsis ("..."). The label is
 * non-interactive and serves purely as a visual element.
 * </p>
 */
public class Label
extends BaseComp {
    private String text;
    private Font font;
    private Color color;

    /**
     * Constructs a Label with the given text, position and size.
     * <p>
     * The default font is Dialog plain 14 px and the default colour is a dark
     * grey ({@code #2A2E34}).
     * </p>
     *
     * @param text   the initial label text
     * @param x      the x-coordinate of the label's top-left corner
     * @param y      the y-coordinate of the label's top-left corner
     * @param width  the label width in pixels
     * @param height the label height in pixels
     */
    public Label(String string, int n, int n2, int n3, int n4) {
        super(null);
        this.text = string;
        this.font = new Font("Dialog", 0, 14);
        this.color = new Color(42, 46, 52);
        this.setBounds(n, n2, n3, n4);
    }

    /**
     * Paints the label text.
     * <p>
     * If the text is wider than the component's width (minus a 1 px padding on
     * each side) it is truncated and an ellipsis is appended. Empty or
     * {@code null} text results in a no-op.
     * </p>
     *
     * @param graphics the {@link Graphics} context supplied by the rendering
     *                 pipeline
     */
    @Override
    public void customGraphics(Graphics graphics) {
        if (this.text == null || this.text.isEmpty()) return;
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setColor(this.color);
        graphics2D.setFont(this.font);
        int n = Math.max(16, this.getHeight() / 2 + (this.font.getSize() / 3));
        
        FontMetrics fm = graphics2D.getFontMetrics();
        int maxWidth = this.getWidth() - 2;
        String display = this.text;
        
        if (fm.stringWidth(display) > maxWidth && maxWidth > 10) {
            String ellipsis = "...";
            int limit = maxWidth - fm.stringWidth(ellipsis);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < display.length(); i++) {
                String candidate = builder.toString() + display.charAt(i);
                if (fm.stringWidth(candidate) > limit) {
                    break;
                }
                builder.append(display.charAt(i));
            }
            display = builder.toString() + ellipsis;
        }

        graphics2D.drawString(display, 0, n);
    }

    /**
     * Updates the label's displayed text.
     *
     * @param string the new text
     */
    public void setText(String string) {
        this.text = string;
        this.invalidate();
    }

    /**
     * Returns the current label text.
     *
     * @return the text string
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the font used to render the label.
     *
     * @param font the new font
     */
    public void setFont(Font font) {
        this.font = font;
        this.invalidate();
    }

    /**
     * Sets the text colour.
     *
     * @param color the new colour
     */
    public void setColor(Color color) {
        this.color = color;
        this.invalidate();
    }
}

