/*
 * Decompiled with CFR 0.152.
 */
package style;

import java.awt.Color;
import java.awt.Graphics;
import layout.AbsoluteLayoutEngine;
import layout.BaseLayoutEngine;
import layout.BlockLayoutEngine;
import layout.FlexLayoutEngine;
import layout.GridLayoutEngine;
import main.BaseComp;
import style.TailwindParser;

public class StyleManager {
    private BaseLayoutEngine layoutEngine;
    private Color color = Color.WHITE;
    private int borderRadius = 0;
    private int width = 0;
    private int height = 0;
    private int x = 0;
    private int y = 0;
    private boolean isColumnFlex = false;
    private boolean isRowFirst = false;
    private int numRows = 0;
    private int numCols = 0;
    private int gap = 0;

    public StyleManager(Color color, int n, int n2, int n3, int n4, int n5, String string) {
        this.color = color;
        this.borderRadius = n;
        this.width = n2;
        this.height = n3;
        this.x = n4;
        this.y = n5;
        this.layoutEngine = StyleManager.createLayoutEngine(string);
    }

    public void setFlexProps(boolean bl, int n) {
        this.isColumnFlex = bl;
        this.gap = n;
    }

    public void setBlockProps(int n) {
        this.gap = n;
    }

    public void setGridProps(boolean bl, int n, int n2) {
        this.isRowFirst = bl;
        this.numRows = n;
        this.numCols = n2;
    }

    public void setLayoutEngineType(String string) {
        this.layoutEngine = StyleManager.createLayoutEngine(string);
    }

    public StyleManager(StyleManager styleManager) {
        this.color = styleManager.color;
        this.layoutEngine = StyleManager.createLayoutEngine("block");
    }

    public StyleManager(String string) {
        this.color = new Color(0, 0, 0, 0);
        this.layoutEngine = new BlockLayoutEngine();
        TailwindParser.applyTailwind(this, string);
    }

    private static BaseLayoutEngine createLayoutEngine(String string) {
        return switch (string) {
            case "flex" -> new FlexLayoutEngine();
            case "block" -> new BlockLayoutEngine();
            case "grid" -> new GridLayoutEngine();
            case "absolute" -> new AbsoluteLayoutEngine();
            default -> new BaseLayoutEngine();
        };
    }

    public void apply(Graphics graphics) {
        graphics.setColor(this.color);
        if (this.borderRadius > 0) {
            graphics.fillRoundRect(0, 0, this.width, this.height, this.borderRadius, this.borderRadius);
        } else {
            graphics.fillRect(0, 0, this.width, this.height);
        }
    }

    public void doLayout(BaseComp baseComp) {
        this.layoutEngine.doLayout(baseComp);
    }

    public Graphics createChildGraphics(BaseComp baseComp, BaseComp baseComp2, Graphics graphics) {
        return graphics;
    }

    public void manageInheritStylePropagation(BaseComp baseComp) {
        BaseComp[] baseCompArray = baseComp.getChildren();
        if (baseCompArray == null) {
            return;
        }
        for (BaseComp baseComp2 : baseCompArray) {
            StyleManager.useAsDefaultStyleIfNotSet(baseComp2, this);
        }
    }

    public static void useAsDefaultStyleIfNotSet(BaseComp baseComp, StyleManager styleManager) {
        if (baseComp.getStyleManager() == null) {
            baseComp.setStyleManager(new StyleManager(styleManager));
            return;
        }
        StyleManager styleManager2 = baseComp.getStyleManager();
        if (styleManager2.color == null) {
            styleManager2.color = styleManager.color;
        }
    }

    public void setBounds(int n, int n2, int n3, int n4) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
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

    public boolean isColumnFlex() {
        return this.isColumnFlex;
    }

    public int getGap() {
        return this.gap;
    }

    public boolean isRowFirst() {
        return this.isRowFirst;
    }

    public int getNumRows() {
        return this.numRows;
    }

    public int getNumCols() {
        return this.numCols;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setBorderRadius(int n) {
        this.borderRadius = n;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setHeight(int n) {
        this.height = n;
    }
}

