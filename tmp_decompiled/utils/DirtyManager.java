/*
 * Decompiled with CFR 0.152.
 */
package utils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import main.BaseComp;

public class DirtyManager {
    private static final int MAX_REGIONS = 24;
    private static final int MERGE_PADDING = 2;
    private final List<Rectangle> dirtyRegions = new ArrayList<Rectangle>();
    private boolean fullRedrawRequested;

    public synchronized void addDirtyRegion(int n, int n2, int n3, int n4) {
        this.addDirtyRegion(new Rectangle(n, n2, Math.max(1, n3), Math.max(1, n4)));
    }

    public synchronized void addDirtyRegion(Rectangle rectangle) {
        boolean bl;
        if (rectangle == null || rectangle.width <= 0 || rectangle.height <= 0) {
            return;
        }
        if (this.fullRedrawRequested) {
            return;
        }
        Rectangle rectangle2 = new Rectangle(rectangle);
        block0: do {
            bl = false;
            for (int i = 0; i < this.dirtyRegions.size(); ++i) {
                Rectangle rectangle3 = this.dirtyRegions.get(i);
                if (!this.intersectsOrNear(rectangle3, rectangle2)) continue;
                rectangle2 = rectangle3.union(rectangle2);
                this.dirtyRegions.remove(i);
                bl = true;
                continue block0;
            }
        } while (bl);
        this.dirtyRegions.add(rectangle2);
        if (this.dirtyRegions.size() > 24) {
            this.requestFullRedraw();
        }
    }

    public synchronized void requestFullRedraw() {
        this.fullRedrawRequested = true;
        this.dirtyRegions.clear();
    }

    public synchronized boolean hasDirtyRegion() {
        return this.fullRedrawRequested || !this.dirtyRegions.isEmpty();
    }

    public synchronized boolean isFullRedrawRequested() {
        return this.fullRedrawRequested;
    }

    public synchronized List<Rectangle> getDirtyRegions() {
        return new ArrayList<Rectangle>(this.dirtyRegions);
    }

    public synchronized int getDirtyRegionCount() {
        return this.fullRedrawRequested ? 1 : this.dirtyRegions.size();
    }

    public synchronized int getEstimatedDirtyArea() {
        if (this.fullRedrawRequested) {
            return Integer.MAX_VALUE;
        }
        int n = 0;
        for (Rectangle rectangle : this.dirtyRegions) {
            n += Math.max(0, rectangle.width) * Math.max(0, rectangle.height);
        }
        return n;
    }

    public synchronized boolean shouldFallbackToFullRedraw(int n, int n2) {
        if (this.fullRedrawRequested) {
            return true;
        }
        int n3 = Math.max(1, n * n2);
        return this.getEstimatedDirtyArea() > (int)((double)n3 * 0.55);
    }

    public synchronized Rectangle getDirtyRegion() {
        if (this.fullRedrawRequested) {
            return null;
        }
        if (this.dirtyRegions.isEmpty()) {
            return null;
        }
        Rectangle rectangle = new Rectangle(this.dirtyRegions.get(0));
        for (int i = 1; i < this.dirtyRegions.size(); ++i) {
            rectangle = rectangle.union(this.dirtyRegions.get(i));
        }
        return rectangle;
    }

    public synchronized void clear() {
        this.dirtyRegions.clear();
        this.fullRedrawRequested = false;
    }

    public void markAll(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        this.requestFullRedraw();
    }

    private boolean intersectsOrNear(Rectangle rectangle, Rectangle rectangle2) {
        Rectangle rectangle3 = new Rectangle(rectangle.x - 2, rectangle.y - 2, rectangle.width + 4, rectangle.height + 4);
        return rectangle3.intersects(rectangle2) || rectangle3.contains(rectangle2) || rectangle2.contains(rectangle3);
    }
}

