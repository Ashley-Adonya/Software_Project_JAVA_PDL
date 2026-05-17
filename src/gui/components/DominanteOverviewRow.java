package gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import components.Label;
import main.BaseComp;

/**
 * A compact row component that displays an overview of a dominante (academic
 * major / specialization). It shows a coloured code badge, the dominante
 * name, the number of scheduled sessions, the allocated/capacity counts, and
 * the fill rate as a percentage. The badge colour is deterministically derived
 * from the dominante code using a hash function, giving each dominante a
 * consistent colour across the UI.
 */
public class DominanteOverviewRow extends SurfaceCard {
    private final BaseComp codeBadgeBg;
    private final Label codeBadge;
    private final Label nameLabel;
    private final Label sessionsLabel;
    private final Label loadLabel;
    private final Label rateLabel;
    private Color badgeColor;

    /**
     * Constructs a dominante overview row with default placeholder values.
     */
    public DominanteOverviewRow() {
        super(0, 0, 100, 64, Color.WHITE, new Color(232, 236, 242), 10);

        this.badgeColor = new Color(124, 92, 255);
        this.codeBadgeBg = new BaseComp(null) {
            @Override
            public void customGraphics(Graphics g) {
                g.setColor(badgeColor);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
        };

        this.codeBadge = new Label("--", 0, 0, 42, 24);
        this.codeBadge.setFont(new Font("Dialog", Font.BOLD, 12));
        this.codeBadge.setColor(Color.WHITE);

        this.nameLabel = new Label("Dominante", 0, 0, 220, 22);
        this.nameLabel.setFont(new Font("Dialog", Font.BOLD, 15));
        this.nameLabel.setColor(new Color(35, 43, 58));

        this.sessionsLabel = new Label("0 sessions", 0, 0, 220, 16);
        this.sessionsLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.sessionsLabel.setColor(new Color(120, 130, 146));

        this.loadLabel = new Label("0 / 0", 0, 0, 120, 18);
        this.loadLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        this.loadLabel.setColor(new Color(35, 43, 58));

        this.rateLabel = new Label("0% rempli", 0, 0, 120, 16);
        this.rateLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.rateLabel.setColor(new Color(120, 130, 146));

        addChild(codeBadgeBg);
        addChild(codeBadge);
        addChild(nameLabel);
        addChild(sessionsLabel);
        addChild(loadLabel);
        addChild(rateLabel);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (codeBadgeBg == null || codeBadge == null || nameLabel == null || sessionsLabel == null || loadLabel == null
                || rateLabel == null) {
            return;
        }
        codeBadgeBg.setBounds(12, 16, 44, 28);
        codeBadge.setBounds(14, 18, 40, 24);
        nameLabel.setBounds(64, 12, width - 220, 22);
        sessionsLabel.setBounds(64, 34, width - 220, 16);
        loadLabel.setBounds(width - 120, 14, 100, 18);
        rateLabel.setBounds(width - 120, 34, 100, 16);
    }

    /**
     * Populates the row with the given dominante data. The code is truncated
     * to at most three uppercase characters and used to derive the badge
     * colour.
     *
     * @param code            the dominante code (used for badge text and colour)
     * @param name            the display name of the dominante
     * @param sessionCount    the number of scheduled sessions
     * @param allocated       the number of students currently allocated
     * @param capacity        the maximum student capacity
     * @param fillRatePercent the fill rate as a percentage (0-100)
     */
    public void setData(String code, String name, int sessionCount, int allocated, int capacity, int fillRatePercent) {
        String safeCode = code == null || code.isBlank() ? "--" : code.trim().toUpperCase();
        String safeName = name == null || name.isBlank() ? "Dominante" : name;

        codeBadge.setText(safeCode.length() > 3 ? safeCode.substring(0, 3) : safeCode);
        badgeColor = colorForCode(safeCode);

        nameLabel.setText(safeName);
        sessionsLabel.setText(sessionCount + " sessions programmees");
        loadLabel.setText(allocated + " / " + capacity);
        rateLabel.setText(fillRatePercent + "% rempli");

        invalidate();
    }

    private Color colorForCode(String code) {
        int hash = Math.abs(code.hashCode());
        Color[] palette = new Color[] {
                new Color(124, 92, 255),
                new Color(239, 68, 68),
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(245, 158, 11)
        };
        return palette[hash % palette.length];
    }
}
