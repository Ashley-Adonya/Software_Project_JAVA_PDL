package components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import event.UiEvent;
import main.BaseComp;

/**
 * A custom-painted clickable button component for the PDL application UI.
 * <p>
 * Renders a rounded rectangle with text using the application's own rendering
 * pipeline (via {@link #customGraphics(Graphics)}). Supports enabled/disabled
 * visual states, press animation (darker fill), configurable colors and fonts,
 * and a single click callback via {@link Runnable}. Pointer events are handled
 * internally through the {@link event.EventManager} registered in the
 * constructor.
 * </p>
 */
public class Button extends BaseComp {
    private Runnable onClick;
    private String text;
    private Color background = new Color(61, 126, 245);
    private Color foreground = Color.WHITE;
    private Color disabledBackground = new Color(165, 175, 190);
    private Color disabledForeground = new Color(245, 247, 250);
    private Font font = new Font("Dialog", Font.BOLD, 13);
    private boolean enabled = true;
    private boolean pressed = false;

    /**
     * Constructs a Button with the specified text, position, size and click
     * handler.
     * <p>
     * Registers POINTER_DOWN / POINTER_UP event listeners that control the
     * pressed visual state and invoke the {@code onClick} callback when a
     * full press-and-release occurs within the button bounds while enabled.
     * </p>
     *
     * @param text    the label displayed on the button (null-safe, defaults to
     *                empty string)
     * @param x       the x-coordinate of the button's top-left corner
     * @param y       the y-coordinate of the button's top-left corner
     * @param width   the button width in pixels
     * @param height  the button height in pixels
     * @param onClick the action to run when the button is clicked; may be null
     */
    public Button(String text, int x, int y, int width, int height, Runnable onClick) {
        super(null);
        this.text = text == null ? "" : text;
        this.onClick = onClick;
        setBounds(x, y, width, height);
        setCursor(java.awt.Cursor.HAND_CURSOR);

        getEventManager().register(UiEvent.Type.POINTER_DOWN, (component, event) -> {
            if (!enabled || event == null || event.getTarget() != this) {
                return;
            }
            pressed = true;
            invalidate();
        });

        getEventManager().register(UiEvent.Type.POINTER_UP, (component, event) -> {
            if (event == null) {
                pressed = false;
                invalidate();
                return;
            }
            boolean shouldClick = enabled && pressed && containsGlobalPoint(event.getX(), event.getY());
            pressed = false;
            if (shouldClick && onClick != null) {
                triggerClick();
            }
            invalidate();
        });
    }

    private void triggerClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    /**
     * Paints the button's visual representation.
     * <p>
     * Draws a filled rounded rectangle with an optional darker shade when
     * pressed, a subtle border, and centred text. Colours and fonts respect the
     * current enabled/disabled state.
     * </p>
     *
     * @param graphics the {@link Graphics} context supplied by the rendering
     *                 pipeline
     */
    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        Color fillColor = enabled ? background : disabledBackground;
        Color textColor = enabled ? foreground : disabledForeground;

        graphics2D.setColor(pressed && enabled ? fillColor.darker() : fillColor);
        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        graphics2D.setColor(new Color(0, 0, 0, enabled ? 24 : 12));
        graphics2D.drawRoundRect(0, 0, Math.max(1, getWidth() - 1), Math.max(1, getHeight() - 1), 10, 10);

        graphics2D.setFont(font);
        graphics2D.setColor(textColor);
        FontMetrics metrics = graphics2D.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textX = Math.max(8, (getWidth() - textWidth) / 2);
        int textY = Math.max(16, (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
        graphics2D.drawString(text, textX, textY);
    }

    /**
     * Updates the button's displayed text.
     *
     * @param text the new label (null-safe, defaults to empty string)
     */
    public void setText(String text) {
        this.text = text == null ? "" : text;
        invalidate();
    }

    /**
     * Replaces the click-action callback.
     *
     * @param onClick the new {@link Runnable} to execute on click; may be null
     */
    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    /**
     * Enables or disables the button.
     * <p>
     * When disabled the button uses muted colours, ignores pointer events, and
     * resets the pressed state.
     * </p>
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            pressed = false;
        }
        invalidate();
    }

    /**
     * Returns whether this button is currently enabled.
     *
     * @return {@code true} if the button is interactive
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the background fill colour used when the button is enabled.
     *
     * @param background the new background colour; if {@code null} the current
     *                   colour is kept
     */
    public void setBackground(Color background) {
        this.background = background == null ? this.background : background;
        invalidate();
    }

    /**
     * Sets the text colour used when the button is enabled.
     *
     * @param foreground the new foreground colour; if {@code null} the current
     *                   colour is kept
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground == null ? this.foreground : foreground;
        invalidate();
    }

    /**
     * Sets the font used to render the button's text.
     *
     * @param font the new font; if {@code null} the current font is kept
     */
    public void setFont(Font font) {
        this.font = font == null ? this.font : font;
        invalidate();
    }
}