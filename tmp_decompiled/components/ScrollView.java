/*
 * Decompiled with CFR 0.152.
 */
package components;

import event.UiEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Timer;
import main.BaseComp;

public class ScrollView
extends BaseComp {
    private static final double BASE_PIXELS_PER_UNIT = 48.0;
    private static final long AXIS_LATCH_TIMEOUT_NANOS = ScrollView.readLatchTimeoutNanos("ui.scroll.latchMs", 220L);
    private static final double SCROLL_X_FACTOR = ScrollView.readFactor("ui.scroll.xFactor", 1.0);
    private static final double SCROLL_Y_FACTOR = ScrollView.readFactor("ui.scroll.yFactor", 1.0);
    private final BaseComp content;
    private int scrollY;
    private int scrollX;
    private int contentWidth;
    private int contentHeight;
    private boolean draggingVerticalThumb;
    private boolean draggingHorizontalThumb;
    private int dragStartPointerX;
    private int dragStartPointerY;
    private int dragStartScrollX;
    private int dragStartScrollY;
    private double wheelAccumulatorY;
    private double wheelAccumulatorX;
    private ScrollAxis latchedAxis;
    private long lastWheelEventNanos;
    private boolean scrollbarsVisible;
    private final Timer hideScrollbarsTimer;

    public ScrollView(int n, int n2, int n3, int n4) {
        super(null);
        this.setBounds(n, n2, n3, n4);
        this.content = new BaseComp(null);
        this.content.setBounds(0, 0, n3, n4);
        this.addChild(this.content);
        this.latchedAxis = ScrollAxis.NONE;
        this.lastWheelEventNanos = 0L;
        this.scrollbarsVisible = false;
        this.hideScrollbarsTimer = new Timer(850, actionEvent -> {
            if (this.draggingVerticalThumb || this.draggingHorizontalThumb) {
                return;
            }
            this.scrollbarsVisible = false;
            this.invalidate();
        });
        this.hideScrollbarsTimer.setRepeats(false);
        this.getEventManager().register(UiEvent.Type.WHEEL, (baseComp, uiEvent) -> {
            boolean bl;
            double d = uiEvent.getWheelRotation();
            if (d == 0.0) {
                return;
            }
            this.requestShowScrollbars();
            boolean bl2 = this.canScrollHorizontally();
            boolean bl3 = this.canScrollVertically();
            ScrollAxis scrollAxis = this.chooseLatchedAxis(uiEvent.isShiftDown(), bl2, bl3);
            if (scrollAxis == ScrollAxis.HORIZONTAL) {
                this.wheelAccumulatorX += d * (48.0 * SCROLL_X_FACTOR);
                int n = this.consumeWheelAccumulatorX();
                if (n == 0) {
                    return;
                }
                int n2 = this.scrollX;
                this.setScrollX(this.scrollX + n);
                bl = n2 != this.scrollX;
            } else {
                this.wheelAccumulatorY += d * (48.0 * SCROLL_Y_FACTOR);
                int n = this.consumeWheelAccumulatorY();
                if (n == 0) {
                    return;
                }
                int n3 = this.scrollY;
                this.setScrollY(this.scrollY + n);
                boolean bl4 = bl = n3 != this.scrollY;
            }
            if (bl) {
                this.invalidate();
                uiEvent.stopPropagation();
                this.requestShowScrollbars();
            }
        });
        this.getEventManager().register(UiEvent.Type.POINTER_DOWN, (baseComp, uiEvent) -> {
            int n = this.toLocalX(uiEvent.getX());
            int n2 = this.toLocalY(uiEvent.getY());
            Rectangle rectangle = this.getVerticalThumbBounds();
            Rectangle rectangle2 = this.getHorizontalThumbBounds();
            if (rectangle != null && rectangle.contains(n, n2)) {
                this.draggingVerticalThumb = true;
                this.dragStartPointerY = n2;
                this.dragStartScrollY = this.scrollY;
                uiEvent.stopPropagation();
                this.requestShowScrollbars();
                this.invalidate();
                return;
            }
            if (rectangle2 != null && rectangle2.contains(n, n2)) {
                this.draggingHorizontalThumb = true;
                this.dragStartPointerX = n;
                this.dragStartScrollX = this.scrollX;
                uiEvent.stopPropagation();
                this.requestShowScrollbars();
                this.invalidate();
            }
        });
        this.getEventManager().register(UiEvent.Type.POINTER_MOVE, (baseComp, uiEvent) -> {
            int n;
            int n2;
            int n3;
            int n4;
            if (!this.draggingVerticalThumb && !this.draggingHorizontalThumb) {
                return;
            }
            if (this.draggingVerticalThumb) {
                n4 = this.getMaxScrollY();
                n3 = this.getHeight() - this.getVerticalThumbSize();
                if (n4 > 0 && n3 > 0) {
                    n2 = this.toLocalY(uiEvent.getY()) - this.dragStartPointerY;
                    n = (int)Math.round((double)n2 / (double)n3 * (double)n4);
                    this.setScrollY(this.dragStartScrollY + n);
                }
            }
            if (this.draggingHorizontalThumb) {
                n4 = this.getMaxScrollX();
                n3 = this.getWidth() - this.getHorizontalThumbSize();
                if (n4 > 0 && n3 > 0) {
                    n2 = this.toLocalX(uiEvent.getX()) - this.dragStartPointerX;
                    n = (int)Math.round((double)n2 / (double)n3 * (double)n4);
                    this.setScrollX(this.dragStartScrollX + n);
                }
            }
            uiEvent.stopPropagation();
            this.requestShowScrollbars();
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (!this.draggingVerticalThumb && !this.draggingHorizontalThumb) {
                return;
            }
            this.draggingVerticalThumb = false;
            this.draggingHorizontalThumb = false;
            uiEvent.stopPropagation();
            this.requestShowScrollbars();
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_MOVE, (baseComp, uiEvent) -> {
            int n = this.toLocalX(uiEvent.getX());
            int n2 = this.toLocalY(uiEvent.getY());
            if (n >= 0 && n < this.getWidth() && n2 >= 0 && n2 < this.getHeight() && (this.canScrollHorizontally() || this.canScrollVertically())) {
                this.requestShowScrollbars();
            }
        });
    }

    public BaseComp getContent() {
        return this.content;
    }

    public void setContentHeight(int n) {
        this.contentHeight = Math.max(this.getHeight(), n);
        this.syncContentPosition();
    }

    public void setContentWidth(int n) {
        this.contentWidth = Math.max(this.getWidth(), n);
        this.syncContentPosition();
    }

    public int getScrollY() {
        return this.scrollY;
    }

    public void setScrollY(int n) {
        int n2 = this.getMaxScrollY();
        this.scrollY = Math.max(0, Math.min(n, n2));
        this.syncContentPosition();
    }

    public int getScrollX() {
        return this.scrollX;
    }

    public void setScrollX(int n) {
        int n2 = this.getMaxScrollX();
        this.scrollX = Math.max(0, Math.min(n, n2));
        this.syncContentPosition();
    }

    private int getMaxScrollY() {
        return Math.max(0, this.contentHeight - this.getHeight());
    }

    private int getMaxScrollX() {
        return Math.max(0, this.contentWidth - this.getWidth());
    }

    private boolean canScrollVertically() {
        return this.getMaxScrollY() > 0;
    }

    private boolean canScrollHorizontally() {
        return this.getMaxScrollX() > 0;
    }

    private int consumeWheelAccumulatorY() {
        int n = (int)this.wheelAccumulatorY;
        this.wheelAccumulatorY -= (double)n;
        return n;
    }

    private int consumeWheelAccumulatorX() {
        int n = (int)this.wheelAccumulatorX;
        this.wheelAccumulatorX -= (double)n;
        return n;
    }

    private void syncContentPosition() {
        this.content.setBounds(-this.scrollX, -this.scrollY, Math.max(this.getWidth(), this.contentWidth), Math.max(this.getHeight(), this.contentHeight));
    }

    private int getVerticalThumbSize() {
        if (this.contentHeight <= this.getHeight() || this.getHeight() <= 0) {
            return 0;
        }
        return Math.max(20, (int)((float)this.getHeight() / (float)this.contentHeight * (float)this.getHeight()));
    }

    private int getHorizontalThumbSize() {
        if (this.contentWidth <= this.getWidth() || this.getWidth() <= 0) {
            return 0;
        }
        return Math.max(20, (int)((float)this.getWidth() / (float)this.contentWidth * (float)this.getWidth()));
    }

    private Rectangle getVerticalThumbBounds() {
        int n = this.getMaxScrollY();
        int n2 = this.getVerticalThumbSize();
        if (n <= 0 || n2 <= 0) {
            return null;
        }
        int n3 = this.getHeight() - n2;
        int n4 = (int)((float)this.scrollY / (float)n * (float)n3);
        return new Rectangle(this.getWidth() - 8, n4, 6, n2);
    }

    private Rectangle getHorizontalThumbBounds() {
        int n = this.getMaxScrollX();
        int n2 = this.getHorizontalThumbSize();
        if (n <= 0 || n2 <= 0) {
            return null;
        }
        int n3 = this.getWidth() - n2;
        int n4 = (int)((float)this.scrollX / (float)n * (float)n3);
        return new Rectangle(n4, this.getHeight() - 8, n2, 6);
    }

    @Override
    public void paint(Graphics graphics) {
        this.customGraphics(graphics);
        Graphics2D graphics2D = (Graphics2D)graphics.create();
        graphics2D.clipRect(0, 0, this.getWidth(), this.getHeight());
        this.paintChildren(graphics2D);
        if (this.scrollbarsVisible || this.draggingVerticalThumb || this.draggingHorizontalThumb) {
            Rectangle rectangle = this.getVerticalThumbBounds();
            Rectangle rectangle2 = this.getHorizontalThumbBounds();
            if (rectangle != null) {
                graphics2D.setColor(this.draggingVerticalThumb ? new Color(120, 120, 120, 180) : new Color(150, 150, 150, 140));
                graphics2D.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 6, 6);
            }
            if (rectangle2 != null) {
                graphics2D.setColor(this.draggingHorizontalThumb ? new Color(120, 120, 120, 180) : new Color(150, 150, 150, 140));
                graphics2D.fillRoundRect(rectangle2.x, rectangle2.y, rectangle2.width, rectangle2.height, 6, 6);
            }
        }
        graphics2D.dispose();
    }

    private void requestShowScrollbars() {
        if (!this.canScrollHorizontally() && !this.canScrollVertically()) {
            return;
        }
        if (!this.scrollbarsVisible) {
            this.scrollbarsVisible = true;
            this.invalidate();
        }
        if (!this.draggingVerticalThumb && !this.draggingHorizontalThumb) {
            this.hideScrollbarsTimer.restart();
        }
    }

    private ScrollAxis chooseLatchedAxis(boolean bl, boolean bl2, boolean bl3) {
        long l = System.nanoTime();
        if (this.lastWheelEventNanos == 0L || l - this.lastWheelEventNanos > AXIS_LATCH_TIMEOUT_NANOS) {
            this.latchedAxis = ScrollAxis.NONE;
        }
        this.lastWheelEventNanos = l;
        ScrollAxis scrollAxis = this.resolveAxisCandidate(bl, bl2, bl3);
        if (scrollAxis == ScrollAxis.NONE) {
            return ScrollAxis.NONE;
        }
        if (this.latchedAxis == ScrollAxis.NONE) {
            this.latchedAxis = scrollAxis;
            return this.latchedAxis;
        }
        if (this.latchedAxis == ScrollAxis.HORIZONTAL && !bl2 && bl3) {
            this.latchedAxis = ScrollAxis.VERTICAL;
            return this.latchedAxis;
        }
        if (this.latchedAxis == ScrollAxis.VERTICAL && !bl3 && bl2) {
            this.latchedAxis = ScrollAxis.HORIZONTAL;
            return this.latchedAxis;
        }
        if (bl && bl2) {
            this.latchedAxis = ScrollAxis.HORIZONTAL;
        }
        return this.latchedAxis;
    }

    private ScrollAxis resolveAxisCandidate(boolean bl, boolean bl2, boolean bl3) {
        if (bl && bl2) {
            return ScrollAxis.HORIZONTAL;
        }
        if (bl3) {
            return ScrollAxis.VERTICAL;
        }
        if (bl2) {
            return ScrollAxis.HORIZONTAL;
        }
        return ScrollAxis.NONE;
    }

    private static double readFactor(String string, double d) {
        String string2 = System.getProperty(string);
        if (string2 == null || string2.isBlank()) {
            return d;
        }
        try {
            double d2 = Double.parseDouble(string2.trim());
            return ScrollView.clamp(d2, 0.1, 8.0);
        }
        catch (NumberFormatException numberFormatException) {
            return d;
        }
    }

    private static long readLatchTimeoutNanos(String string, long l) {
        String string2 = System.getProperty(string);
        long l2 = l;
        if (string2 != null && !string2.isBlank()) {
            try {
                l2 = Long.parseLong(string2.trim());
            }
            catch (NumberFormatException numberFormatException) {
                l2 = l;
            }
        }
        l2 = Math.max(40L, Math.min(l2, 2000L));
        return l2 * 1000000L;
    }

    private static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    private static enum ScrollAxis {
        NONE,
        HORIZONTAL,
        VERTICAL;

    }
}

