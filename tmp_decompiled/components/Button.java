/*
 * Decompiled with CFR 0.152.
 */
package components;

import event.UiEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import main.BaseComp;

public class Button
extends BaseComp {
    private final Runnable onClick;
    private String text;
    private Color background;
    private Color foreground;
    private int radius;
    private boolean pressed;

    public Button(String string, int n, int n2, int n3, int n4, Runnable runnable) {
        super(null);
        this.text = string;
        this.onClick = runnable;
        this.background = new Color(61, 126, 245);
        this.foreground = Color.WHITE;
        this.radius = 12;
        this.setCursor(12);
        this.setBounds(n, n2, n3, n4);
        this.getEventManager().register(UiEvent.Type.POINTER_DOWN, (baseComp, uiEvent) -> {
            if (uiEvent.getTarget() != this) {
                return;
            }
            this.pressed = true;
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            boolean bl = this.pressed && this.containsGlobalPoint(uiEvent.getX(), uiEvent.getY());
            this.pressed = false;
            if (bl && this.onClick != null) {
                this.onClick.run();
            }
            this.invalidate();
        });
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setColor(this.pressed ? this.background.darker() : this.background);
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.radius, this.radius);
        graphics2D.setColor(new Color(0, 0, 0, 30));
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, this.radius, this.radius);
        graphics2D.setColor(this.foreground);
        graphics2D.setFont(new Font("Dialog", 1, 13));
        int n = graphics2D.getFontMetrics().stringWidth(this.text);
        int n2 = Math.max(8, (this.getWidth() - n) / 2);
        int n3 = this.getHeight() / 2 + 5;
        graphics2D.drawString(this.text, n2, n3);
    }

    public void setText(String string) {
        this.text = string;
    }

    public void setBackground(Color color) {
        this.background = color;
    }

    public void setForeground(Color color) {
        this.foreground = color;
    }
}

