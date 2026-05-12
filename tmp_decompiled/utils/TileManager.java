/*
 * Decompiled with CFR 0.152.
 */
package utils;

import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class TileManager {
    private final JFrame frame;
    private Rectangle restoreBounds;
    private boolean maximized;

    public TileManager(JFrame jFrame) {
        this.frame = jFrame;
        this.maximized = false;
    }

    public void toggleMaximizeRestore() {
        if (this.maximized) {
            this.restore();
            return;
        }
        this.maximize();
    }

    public void maximize() {
        this.saveRestoreBounds();
        Rectangle rectangle = this.getWorkArea();
        this.frame.setBounds(rectangle);
        this.maximized = true;
    }

    public void restore() {
        if (this.restoreBounds != null) {
            this.frame.setBounds(this.restoreBounds);
        }
        this.maximized = false;
    }

    public void tileLeft() {
        this.saveRestoreBounds();
        Rectangle rectangle = this.getWorkArea();
        this.frame.setBounds(rectangle.x, rectangle.y, rectangle.width / 2, rectangle.height);
        this.maximized = false;
    }

    public void tileRight() {
        this.saveRestoreBounds();
        Rectangle rectangle = this.getWorkArea();
        int n = rectangle.width / 2;
        this.frame.setBounds(rectangle.x + n, rectangle.y, n, rectangle.height);
        this.maximized = false;
    }

    private void saveRestoreBounds() {
        if (!this.maximized) {
            this.restoreBounds = this.frame.getBounds();
        }
    }

    private Rectangle getWorkArea() {
        GraphicsConfiguration graphicsConfiguration = this.frame.getGraphicsConfiguration();
        Rectangle rectangle = graphicsConfiguration.getBounds();
        Insets insets = this.frame.getToolkit().getScreenInsets(graphicsConfiguration);
        return new Rectangle(rectangle.x + insets.left, rectangle.y + insets.top, rectangle.width - insets.left - insets.right, rectangle.height - insets.top - insets.bottom);
    }
}

