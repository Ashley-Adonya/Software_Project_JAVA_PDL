package gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import main.BaseComp;

public class LogoBadge extends BaseComp {
    private static final Color BADGE_COLOR = new Color(120, 70, 255);
    private static final Color ICON_COLOR = Color.WHITE;

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
