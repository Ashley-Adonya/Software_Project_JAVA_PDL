/*
 * Decompiled with CFR 0.152.
 */
package main;

import event.EventManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import main.BaseWindow;
import style.StyleManager;
import style.TailwindParser;

public class BaseComp {
    private StyleManager styleManager = null;
    private EventManager eventManager;
    private final List<BaseComp> children = new CopyOnWriteArrayList<BaseComp>();
    private BaseComp parent;
    private BaseWindow ownerWindow;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean draggable;
    private boolean windowDragHandle;
    private boolean focusable;
    private boolean focused;
    private boolean visible = true;
    private final List<ContainerQueryRule> containerQueries = new CopyOnWriteArrayList<ContainerQueryRule>();
    private boolean evaluatingContainerQueries = false;
    private int cursor = 0;

    public BaseComp(BaseComp[] baseCompArray) {
        this.eventManager = new EventManager();
        if (baseCompArray == null) {
            return;
        }
        for (BaseComp baseComp : baseCompArray) {
            this.attachChild(baseComp);
        }
    }

    private void attachChild(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        baseComp.parent = this;
        if (this.ownerWindow != null) {
            baseComp.setOwnerWindow(this.ownerWindow);
        }
        this.children.add(baseComp);
        this.invalidate();
    }

    public void setBounds(int n, int n2, int n3, int n4) {
        Rectangle rectangle = this.getGlobalBounds();
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        if (this.styleManager != null) {
            this.styleManager.setBounds(n, n2, n3, n4);
        }
        this.evaluateContainerQueries();
        Rectangle rectangle2 = this.getGlobalBounds();
        if (this.ownerWindow != null) {
            this.ownerWindow.invalidateRect(rectangle);
            this.ownerWindow.invalidateRect(rectangle2);
            this.ownerWindow.requestRenderIfNeeded();
        }
    }

    public void doLayout() {
        if (this.styleManager != null) {
            this.styleManager.doLayout(this);
        }
    }

    public void paint(Graphics graphics) {
        if (!this.visible) {
            return;
        }
        this.customGraphics(graphics);
        this.paintChildren(graphics);
    }

    protected void paintChildren(Graphics graphics) {
        if (this.children.isEmpty()) {
            return;
        }
        for (BaseComp baseComp : this.children) {
            if (baseComp == null || !baseComp.isVisible()) continue;
            Graphics graphics2 = graphics.create();
            graphics2.translate(baseComp.getX(), baseComp.getY());
            if (this.styleManager != null) {
                graphics2 = this.styleManager.createChildGraphics(this, baseComp, graphics2);
            }
            baseComp.paint(graphics2);
            graphics2.dispose();
        }
    }

    public void customGraphics(Graphics graphics) {
        if (this.styleManager != null) {
            this.styleManager.apply(graphics);
        }
    }

    public void render(Graphics2D graphics2D) {
        this.paint(graphics2D);
    }

    public boolean containsGlobalPoint(int n, int n2) {
        if (!this.visible) {
            return false;
        }
        int n3 = this.getGlobalX();
        int n4 = this.getGlobalY();
        return n >= n3 && n <= n3 + this.width && n2 >= n4 && n2 <= n4 + this.height;
    }

    public int toLocalX(int n) {
        return n - this.getGlobalX();
    }

    public int toLocalY(int n) {
        return n - this.getGlobalY();
    }

    public int getGlobalX() {
        if (this.parent == null) {
            return this.x;
        }
        return this.parent.getGlobalX() + this.x;
    }

    public int getGlobalY() {
        if (this.parent == null) {
            return this.y;
        }
        return this.parent.getGlobalY() + this.y;
    }

    public void addChild(BaseComp baseComp) {
        this.attachChild(baseComp);
    }

    public void removeChild(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        Rectangle rectangle = baseComp.getGlobalBounds();
        if (this.children.remove(baseComp)) {
            baseComp.parent = null;
            baseComp.setOwnerWindow(null);
            if (this.ownerWindow != null) {
                this.ownerWindow.invalidateRect(rectangle);
                this.ownerWindow.requestRenderIfNeeded();
            }
        }
    }

    public void moveChild(int n, int n2) {
        if (n < 0 || n >= this.children.size()) {
            return;
        }
        int n3 = Math.max(0, Math.min(n2, this.children.size() - 1));
        BaseComp baseComp = this.children.remove(n);
        this.children.add(n3, baseComp);
        this.invalidate();
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public BaseComp[] getChildren() {
        return (BaseComp[])this.children.toArray(BaseComp[]::new);
    }

    public List<BaseComp> getChildrenList() {
        return this.children;
    }

    public StyleManager getStyleManager() {
        return this.styleManager;
    }

    public void setStyleManager(StyleManager styleManager) {
        this.styleManager = styleManager;
    }

    public void setClass(String string) {
        if (this.styleManager == null) {
            this.styleManager = new StyleManager(string);
        } else {
            TailwindParser.applyTailwind(this.styleManager, string);
        }
    }

    public BaseComp getParent() {
        return this.parent;
    }

    public void setOwnerWindow(BaseWindow baseWindow) {
        this.ownerWindow = baseWindow;
        for (BaseComp baseComp : this.children) {
            if (baseComp == null) continue;
            baseComp.setOwnerWindow(baseWindow);
        }
    }

    public BaseWindow getOwnerWindow() {
        return this.ownerWindow;
    }

    public Rectangle getGlobalBounds() {
        return new Rectangle(this.getGlobalX(), this.getGlobalY(), Math.max(1, this.width), Math.max(1, this.height));
    }

    public void invalidate() {
        if (this.ownerWindow == null) {
            return;
        }
        this.ownerWindow.invalidateComponent(this);
        this.ownerWindow.requestRenderIfNeeded();
    }

    public void invalidateLocalRect(int n, int n2, int n3, int n4) {
        if (this.ownerWindow == null || n3 <= 0 || n4 <= 0) {
            return;
        }
        this.ownerWindow.invalidateRect(new Rectangle(this.getGlobalX() + n, this.getGlobalY() + n2, n3, n4));
        this.ownerWindow.requestRenderIfNeeded();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public void setDraggable(boolean bl) {
        this.draggable = bl;
    }

    public boolean isWindowDragHandle() {
        return this.windowDragHandle;
    }

    public void setWindowDragHandle(boolean bl) {
        this.windowDragHandle = bl;
    }

    public boolean isFocusable() {
        return this.focusable;
    }

    public void setFocusable(boolean bl) {
        this.focusable = bl;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public void setFocused(boolean bl) {
        this.focused = bl;
        this.invalidate();
    }

    public boolean onKeyPressed(int n, char c) {
        return false;
    }

    public boolean onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return false;
        }
        return this.onKeyPressed(keyEvent.getKeyCode(), keyEvent.getKeyChar());
    }

    public boolean onKeyTyped(char c) {
        return false;
    }

    public boolean onKeyTyped(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return false;
        }
        return this.onKeyTyped(keyEvent.getKeyChar());
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean bl) {
        if (this.visible == bl) {
            return;
        }
        this.visible = bl;
        this.invalidate();
    }

    public void addContainerQuery(int n, int n2, int n3, int n4, Runnable runnable, Runnable runnable2) {
        ContainerQueryRule containerQueryRule = new ContainerQueryRule(n, n2, n3, n4, runnable == null ? () -> {} : runnable, runnable2 == null ? () -> {} : runnable2);
        this.containerQueries.add(containerQueryRule);
        this.evaluateContainerQueries();
    }

    public void addWidthContainerQuery(int n, Runnable runnable, Runnable runnable2) {
        this.addContainerQuery(Integer.MIN_VALUE, n, Integer.MIN_VALUE, Integer.MAX_VALUE, runnable, runnable2);
    }

    public void clearContainerQueries() {
        this.containerQueries.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void evaluateContainerQueries() {
        if (this.containerQueries.isEmpty() || this.evaluatingContainerQueries) {
            return;
        }
        this.evaluatingContainerQueries = true;
        try {
            int n = Math.max(0, this.width);
            int n2 = Math.max(0, this.height);
            for (ContainerQueryRule containerQueryRule : this.containerQueries) {
                boolean bl = containerQueryRule.matches(n, n2);
                if (bl && !containerQueryRule.active) {
                    containerQueryRule.active = true;
                    containerQueryRule.onEnter.run();
                    continue;
                }
                if (bl || !containerQueryRule.active) continue;
                containerQueryRule.active = false;
                containerQueryRule.onExit.run();
            }
        }
        finally {
            this.evaluatingContainerQueries = false;
        }
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setCursor(int n) {
        this.cursor = n;
    }

    private static class ContainerQueryRule {
        private final int minWidth;
        private final int maxWidth;
        private final int minHeight;
        private final int maxHeight;
        private final Runnable onEnter;
        private final Runnable onExit;
        private boolean active;

        private ContainerQueryRule(int n, int n2, int n3, int n4, Runnable runnable, Runnable runnable2) {
            this.minWidth = n;
            this.maxWidth = n2;
            this.minHeight = n3;
            this.maxHeight = n4;
            this.onEnter = runnable;
            this.onExit = runnable2;
            this.active = false;
        }

        private boolean matches(int n, int n2) {
            return n >= this.minWidth && n <= this.maxWidth && n2 >= this.minHeight && n2 <= this.maxHeight;
        }
    }
}

