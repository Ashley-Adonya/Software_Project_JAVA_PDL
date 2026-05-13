package components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import event.UiEvent;
import main.BaseComp;

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

    public void setText(String text) {
        this.text = text == null ? "" : text;
        invalidate();
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            pressed = false;
        }
        invalidate();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setBackground(Color background) {
        this.background = background == null ? this.background : background;
        invalidate();
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground == null ? this.foreground : foreground;
        invalidate();
    }

    public void setFont(Font font) {
        this.font = font == null ? this.font : font;
        invalidate();
    }
}