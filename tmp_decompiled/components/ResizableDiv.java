/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Div;
import event.UiEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ResizableDiv
extends Div {
    private static final int HANDLE_SIZE = 14;
    private int minWidth = 80;
    private int minHeight = 60;
    private boolean resizing = false;
    private boolean hoverHandle = false;
    private int startMouseX;
    private int startMouseY;
    private int startWidth;
    private int startHeight;

    public ResizableDiv(int n, int n2, int n3, int n4, Color color, int n5) {
        super(n, n2, n3, n4, color, n5);
        this.setCursor(0);
        this.registerResizeEvents();
    }

    public void setMinSize(int n, int n2) {
        this.minWidth = Math.max(20, n);
        this.minHeight = Math.max(20, n2);
    }

    public boolean isResizing() {
        return this.resizing;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        super.customGraphics(graphics);
        Graphics2D graphics2D = (Graphics2D)graphics;
        int n = Math.max(0, this.getWidth() - 14);
        int n2 = Math.max(0, this.getHeight() - 14);
        graphics2D.setColor(new Color(30, 41, 59, this.hoverHandle || this.resizing ? 220 : 145));
        graphics2D.drawLine(n + 3, n2 + 14 - 4, n + 14 - 4, n2 + 3);
        graphics2D.drawLine(n + 6, n2 + 14 - 4, n + 14 - 4, n2 + 6);
        graphics2D.drawLine(n + 9, n2 + 14 - 4, n + 14 - 4, n2 + 9);
        if (this.hoverHandle || this.resizing) {
            graphics2D.setColor(new Color(59, 130, 246, 120));
            graphics2D.fillRoundRect(n, n2, 14, 14, 6, 6);
        }
    }

    private void registerResizeEvents() {
        this.getEventManager().register(UiEvent.Type.POINTER_DOWN, (baseComp, uiEvent) -> {
            int n;
            if (uiEvent == null) {
                return;
            }
            int n2 = this.toLocalX(uiEvent.getX());
            if (!this.isInHandle(n2, n = this.toLocalY(uiEvent.getY()))) {
                return;
            }
            this.resizing = true;
            this.startMouseX = uiEvent.getX();
            this.startMouseY = uiEvent.getY();
            this.startWidth = this.getWidth();
            this.startHeight = this.getHeight();
            this.setCursor(5);
            if (uiEvent.getWindow() != null) {
                uiEvent.getWindow().capturePointer(this);
            }
            uiEvent.stopPropagation();
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_MOVE, (baseComp, uiEvent) -> {
            if (uiEvent == null) {
                return;
            }
            int n = this.toLocalX(uiEvent.getX());
            int n2 = this.toLocalY(uiEvent.getY());
            if (this.resizing) {
                int n3 = uiEvent.getX() - this.startMouseX;
                int n4 = uiEvent.getY() - this.startMouseY;
                int n5 = Math.max(this.minWidth, this.startWidth + n3);
                int n6 = Math.max(this.minHeight, this.startHeight + n4);
                this.setBounds(this.getX(), this.getY(), n5, n6);
                uiEvent.stopPropagation();
                return;
            }
            boolean bl = this.isInHandle(n, n2);
            if (bl != this.hoverHandle) {
                this.hoverHandle = bl;
                this.setCursor(this.hoverHandle ? 5 : 0);
                this.invalidate();
            }
        });
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (!this.resizing) {
                return;
            }
            this.resizing = false;
            if (uiEvent != null && uiEvent.getWindow() != null) {
                uiEvent.getWindow().releasePointer(this);
            }
            this.setCursor(this.hoverHandle ? 5 : 0);
            if (uiEvent != null) {
                uiEvent.stopPropagation();
            }
            this.invalidate();
        });
    }

    private boolean isInHandle(int n, int n2) {
        return n >= this.getWidth() - 14 && n2 >= this.getHeight() - 14 && n < this.getWidth() && n2 < this.getHeight();
    }
}

