/*
 * Decompiled with CFR 0.152.
 */
package components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.BaseComp;

public class Div
extends BaseComp {
    private Color background;
    private int radius;
    private float alpha;
    private Image backgroundImage;
    private float backgroundImageAlpha;

    public Div(int n, int n2, int n3, int n4, Color color, int n5) {
        super(null);
        this.setBounds(n, n2, n3, n4);
        this.background = color;
        this.radius = Math.max(0, n5);
        this.alpha = 1.0f;
        this.backgroundImageAlpha = 1.0f;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        Composite composite = graphics2D.getComposite();
        graphics2D.setComposite(AlphaComposite.getInstance(3, this.alpha));
        graphics2D.setColor(this.background);
        if (this.radius > 0) {
            graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.radius, this.radius);
        } else {
            graphics2D.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.backgroundImage != null) {
            graphics2D.setComposite(AlphaComposite.getInstance(3, this.backgroundImageAlpha));
            graphics2D.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), null);
        }
        graphics2D.setComposite(composite);
    }

    public void setBackground(Color color) {
        this.background = color;
    }

    public void setRadius(int n) {
        this.radius = Math.max(0, n);
    }

    public void setAlpha(float f) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, f));
    }

    public void setBackgroundImage(String string) {
        if (string == null || string.isBlank()) {
            this.backgroundImage = null;
            return;
        }
        try {
            this.backgroundImage = ImageIO.read(new File(string));
        }
        catch (IOException iOException) {
            this.backgroundImage = null;
        }
    }

    public void setBackgroundImageAlpha(float f) {
        this.backgroundImageAlpha = Math.max(0.0f, Math.min(1.0f, f));
    }
}

