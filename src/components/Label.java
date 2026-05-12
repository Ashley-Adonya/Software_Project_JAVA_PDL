package components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import main.BaseComp;

public class Label
extends BaseComp {
    private String text;
    private Font font;
    private Color color;

    public Label(String string, int n, int n2, int n3, int n4) {
        super(null);
        this.text = string;
        this.font = new Font("Dialog", 0, 14);
        this.color = new Color(42, 46, 52);
        this.setBounds(n, n2, n3, n4);
    }

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

    public void setText(String string) {
        this.text = string;
        this.invalidate();
    }

    public String getText() {
        return this.text;
    }

    public void setFont(Font font) {
        this.font = font;
        this.invalidate();
    }

    public void setColor(Color color) {
        this.color = color;
        this.invalidate();
    }
}

