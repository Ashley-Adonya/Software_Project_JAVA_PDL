/*
 * Decompiled with CFR 0.152.
 */
package components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import main.BaseComp;

public class TextArea
extends BaseComp {
    private String text;

    public TextArea(String string, int n, int n2, int n3, int n4) {
        super(null);
        this.text = string;
        this.setBounds(n, n2, n3, n4);
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
        graphics2D.setColor(new Color(200, 205, 211));
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
        graphics2D.setColor(new Color(55, 60, 68));
        graphics2D.setFont(new Font("Dialog", 0, 13));
        String[] stringArray = this.text.split("\\n");
        int n = 22;
        for (String string : stringArray) {
            if (n > this.getHeight() - 8) break;
            graphics2D.drawString(string, 12, n);
            n += 17;
        }
    }

    public void setText(String string) {
        this.text = string;
    }
}

