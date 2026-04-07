package gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import components.Div;

public class SurfaceCard extends Div {
    private Color borderColor;

    public SurfaceCard(int x, int y, int width, int height, Color background, Color borderColor, int radius) {
        super(x, y, width, height, background, radius);
        this.borderColor = borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    @Override
    public void customGraphics(Graphics g) {
        super.customGraphics(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, Math.max(1, getWidth() - 1), Math.max(1, getHeight() - 1), 14, 14);
    }
}