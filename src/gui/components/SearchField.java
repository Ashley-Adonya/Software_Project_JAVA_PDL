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
 * Champ de recherche texte léger avec saisie clavier et callback de changement.
 * Conçu pour filtrer des listes en temps réel sans dépendre d'un composant externe.
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
    private Consumer<String> onChange = s -> {};

    public SearchField(int x, int y, int width, int height, String placeholder) {
        super(null);
        this.placeholder = placeholder == null ? "" : placeholder;
        setBounds(x, y, width, height);
        setFocusable(true);
        setCursor(java.awt.Cursor.TEXT_CURSOR);

        getEventManager().register(UiEvent.Type.POINTER_DOWN, (component, event) -> {
            if (event == null || event.getTarget() != this) {
                return;
            }
            requestFocus();
            event.stopPropagation();
            invalidate();
        });
    }

    @Override
    public boolean onKeyPressed(java.awt.event.KeyEvent e) {
        if (!isFocused() || e == null) {
            return false;
        }

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
            return true;
        }
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                fireChange();
            }
            invalidate();
            return true;
        }
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) {
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
    public boolean onKeyTyped(java.awt.event.KeyEvent e) {
        if (!isFocused() || e == null) {
            return false;
        }
        if (e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
            return false;
        }
        char keyChar = e.getKeyChar();
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        fireChange();
        invalidate();
    }

    public void setOnChange(Consumer<String> onChange) {
        this.onChange = onChange == null ? s -> {} : onChange;
    }

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
            onChange.accept(text);
        }
    }
}