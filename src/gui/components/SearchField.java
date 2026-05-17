package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import event.UiEvent;
import main.BaseComp;

/**
 * A lightweight text search input field with keyboard input handling and a
 * change callback. Designed for real-time list filtering without external
 * dependencies. The component renders a rounded rectangle with a configurable
 * border that highlights on focus, displays placeholder text when empty, and
 * draws a blinking caret when focused. It supports customisation of all
 * colours and fires a change notification on every text modification.
 */
public class SearchField extends BaseComp {
    private static final int PADDING_X = 12;
    private static final int BASELINE_OFFSET = 6;

    private String text = "";
    private String placeholder = "";
    private Color background = new Color(255, 255, 255);
    private Color border = new Color(200, 205, 211);
    private Color focusBorder = new Color(66, 133, 244);
    private Color textColor = new Color(40, 46, 54);
    private Color placeholderColor = new Color(140, 146, 156);
    private Font font = new Font("Dialog", Font.PLAIN, 14);
    private Runnable onChange = () -> {};

    /**
     * Constructs a search field with the given position, dimensions, and
     * placeholder text.
     *
     * @param x           the x-coordinate of the field
     * @param y           the y-coordinate of the field
     * @param width       the width of the field
     * @param height      the height of the field
     * @param placeholder the placeholder text displayed when the field is empty;
     *                    null is treated as an empty string
     */
    public SearchField(int x, int y, int width, int height, String placeholder) {
        super(null);
        this.placeholder = placeholder == null ? "" : placeholder;
        setBounds(x, y, width, height);
        setFocusable(true);
        setCursor(java.awt.Cursor.TEXT_CURSOR);
    }

    @Override
    public boolean onKeyPressed(int keyCode, char keyChar) {
        if (!isFocused()) {
            return false;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                fireChange();
            }
            invalidate();
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_DELETE) {
            if (!text.isEmpty()) {
                text = "";
                fireChange();
            }
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyTyped(char keyChar) {
        if (!isFocused()) {
            return false;
        }
        if (Character.isISOControl(keyChar)) {
            return false;
        }
        text = text + keyChar;
        fireChange();
        invalidate();
        return true;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        int radius = 10;

        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.setColor(isFocused() ? focusBorder : border);
        g2.setStroke(new java.awt.BasicStroke(isFocused() ? 2.0f : 1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int baseline = Math.max(18, (getHeight() / 2) + BASELINE_OFFSET);

        if (text.isEmpty()) {
            g2.setColor(placeholderColor);
            g2.drawString(placeholder, PADDING_X, baseline);
        } else {
            g2.setColor(textColor);
            g2.drawString(text, PADDING_X, baseline);
        }

        if (isFocused()) {
            int caretX = Math.min(getWidth() - 8, PADDING_X + fm.stringWidth(text) + 1);
            int top = Math.max(8, baseline - fm.getAscent());
            int bottom = Math.min(getHeight() - 8, top + fm.getHeight());
            g2.setColor(focusBorder);
            g2.drawLine(caretX, top, caretX, bottom);
        }
    }

    /**
     * Returns the current text content of the search field.
     *
     * @return the current text (never null)
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of the search field and fires the change callback.
     *
     * @param text the new text; null is treated as an empty string
     */
    public void setText(String text) {
        this.text = text == null ? "" : text;
        fireChange();
        invalidate();
    }

    /**
     * Registers a callback to be invoked whenever the text content changes.
     *
     * @param onChange the runnable to invoke on each change; null clears the
     *                 callback
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {} : onChange;
    }

    /**
     * Configures the colours of the search field. Any parameter that is null
     * will not be updated, allowing partial colour customisation.
     *
     * @param background       the background colour of the field
     * @param border           the border colour when the field is not focused
     * @param focusBorder      the border colour when the field is focused
     * @param textColor        the colour of the entered text
     * @param placeholderColor the colour of the placeholder text
     */
    public void setColors(Color background, Color border, Color focusBorder, Color textColor, Color placeholderColor) {
        if (background != null) {
            this.background = background;
        }
        if (border != null) {
            this.border = border;
        }
        if (focusBorder != null) {
            this.focusBorder = focusBorder;
        }
        if (textColor != null) {
            this.textColor = textColor;
        }
        if (placeholderColor != null) {
            this.placeholderColor = placeholderColor;
        }
        invalidate();
    }

    private void fireChange() {
        if (onChange != null) {
            onChange.run();
        }
    }

    /**
     * Returns the current text content. This is an alias for {@link #getText()}
     * provided for convenience.
     *
     * @return the current text (never null)
     */
    public String getCurrentText() {
        return text;
    }
}