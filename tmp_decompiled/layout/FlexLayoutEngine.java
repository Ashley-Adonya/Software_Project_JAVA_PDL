/*
 * Decompiled with CFR 0.152.
 */
package layout;

import layout.BaseLayoutEngine;
import main.BaseComp;
import style.StyleManager;

public class FlexLayoutEngine
extends BaseLayoutEngine {
    @Override
    public void doLayout(BaseComp baseComp) {
        BaseComp[] baseCompArray = baseComp.getChildren();
        StyleManager styleManager = baseComp.getStyleManager();
        if (baseCompArray == null || styleManager == null) {
            return;
        }
        boolean bl = styleManager.isColumnFlex();
        int n = styleManager.getGap();
        int n2 = 0;
        for (BaseComp baseComp2 : baseCompArray) {
            if (baseComp2 == null) continue;
            if (bl) {
                baseComp2.setBounds(0, n2, baseComp2.getWidth(), baseComp2.getHeight());
                n2 += baseComp2.getHeight() + n;
                continue;
            }
            baseComp2.setBounds(n2, 0, baseComp2.getWidth(), baseComp2.getHeight());
            n2 += baseComp2.getWidth() + n;
        }
    }
}

