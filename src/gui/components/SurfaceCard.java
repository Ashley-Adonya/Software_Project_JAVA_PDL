package gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import components.Div;

/**
 * A base card component that renders a rounded rectangle filled with a
 * background colour and outlined with a configurable border colour. It
 * extends {@link Div} and adds a custom border drawn through the graphics
 * pipeline. This component serves as the visual foundation for many card-like
 * components in the UI framework (e.g. KpiCard, AdminRegistrationCard).
 */
public class SurfaceCard extends Div {
    private Color borderColor;

    /**
     * Constructs a surface card with the given position, dimensions,
     * background, border colour, and corner radius.
     *
     * @param x           the x-coordinate of the card
     * @param y           the y-coordinate of the card
     * @param width       the width of the card
     * @param height      the height of the card
     * @param background  the fill colour of the card
     * @param borderColor the colour of the card's border stroke
     * @param radius      the corner radius for the rounded rectangle
     */
    public SurfaceCard(int x, int y, int width, int height, Color background, Color borderColor, int radius) {
        super(x, y, width, height, background, radius);
        this.borderColor = borderColor;
    }

    /**
     * Sets the border colour of the card and triggers a repaint.
     *
     * @param borderColor the new border colour
     */
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