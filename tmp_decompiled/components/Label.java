/*
 * Decompiled with CFR 0.152.
 */
package components;

import java.awt.Color;
import java.awt.Font;
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
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setColor(this.color);
        graphics2D.setFont(this.font);
        int n = Math.max(16, this.getHeight() / 2 + 5);
        graphics2D.drawString(this.text, 0, n);
    }

    public void setText(String string) {
        this.text = string;
    }

    public String getText() {
        return this.text;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

