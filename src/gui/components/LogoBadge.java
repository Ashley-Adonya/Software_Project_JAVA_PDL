package gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import main.BaseComp;

/**
 * A lightweight component that renders a small logo badge consisting of a
 * rounded purple rectangle with a white graduation-cap-style icon. It is
 * intended for use in headers, sidebars, and other branding areas where a
 * compact visual identifier is needed.
 */
public class LogoBadge extends BaseComp {
    private static final Color BADGE_COLOR = new Color(120, 70, 255);
    private static final Color ICON_COLOR = Color.WHITE;

    /**
     * Constructs a logo badge at the given position with the specified size.
     *
     * @param x    the x-coordinate of the badge
     * @param y    the y-coordinate of the badge
     * @param size the width and height of the badge (square)
     */
    public LogoBadge(int x, int y, int size) {
        super(null);
        setBounds(x, y, size, size);
    }

    @Override
    public void customGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();

        g2.setColor(BADGE_COLOR);
        g2.fillRoundRect(0, 0, w, h, 16, 16);

        int centerX = w / 2;
        int top = Math.max(8, h / 4);

        int[] capX = { centerX - 16, centerX, centerX + 16, centerX };
        int[] capY = { top + 4, top - 4, top + 4, top + 12 };
        g2.setColor(ICON_COLOR);
        g2.fillPolygon(capX, capY, 4);

        g2.fillRect(centerX - 9, top + 12, 18, 5);
        g2.fillRect(centerX + 8, top + 10, 2, 14);
        g2.fillOval(centerX + 6, top + 24, 6, 6);
    }
}
