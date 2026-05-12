/*
 * Decompiled with CFR 0.152.
 */
package event;

import main.BaseComp;
import main.BaseWindow;

public class UiEvent {
    private final Type type;
    private final int x;
    private final int y;
    private final int screenX;
    private final int screenY;
    private final int button;
    private final double wheelRotation;
    private final boolean shiftDown;
    private final int clickCount;
    private boolean propagationStopped;
    private BaseComp target;
    private BaseWindow window;

    public UiEvent(Type type, int n, int n2, int n3, int n4, int n5) {
        this(type, n, n2, n3, n4, n5, 0.0, false, 1);
    }

    public UiEvent(Type type, int n, int n2, int n3, int n4, int n5, double d) {
        this(type, n, n2, n3, n4, n5, d, false, 1);
    }

    public UiEvent(Type type, int n, int n2, int n3, int n4, int n5, double d, boolean bl) {
        this(type, n, n2, n3, n4, n5, d, bl, 1);
    }

    public UiEvent(Type type, int n, int n2, int n3, int n4, int n5, double d, boolean bl, int n6) {
        this.type = type;
        this.x = n;
        this.y = n2;
        this.screenX = n3;
        this.screenY = n4;
        this.button = n5;
        this.wheelRotation = d;
        this.shiftDown = bl;
        this.clickCount = Math.max(1, n6);
        this.propagationStopped = false;
    }

    public boolean isShiftDown() {
        return this.shiftDown;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public Type getType() {
        return this.type;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getScreenX() {
        return this.screenX;
    }

    public int getScreenY() {
        return this.screenY;
    }

    public int getButton() {
        return this.button;
    }

    public double getWheelRotation() {
        return this.wheelRotation;
    }

    public BaseComp getTarget() {
        return this.target;
    }

    public void setTarget(BaseComp baseComp) {
        this.target = baseComp;
    }

    public BaseWindow getWindow() {
        return this.window;
    }

    public void setWindow(BaseWindow baseWindow) {
        this.window = baseWindow;
    }

    public void stopPropagation() {
        this.propagationStopped = true;
    }

    public boolean isPropagationStopped() {
        return this.propagationStopped;
    }

    public static enum Type {
        POINTER_DOWN,
        POINTER_MOVE,
        POINTER_UP,
        CLICK,
        WHEEL;

    }
}

