/*
 * Decompiled with CFR 0.152.
 */
package layout;

import layout.BaseLayoutEngine;
import main.BaseComp;

public class BlockLayoutEngine
extends BaseLayoutEngine {
    @Override
    public void doLayout(BaseComp baseComp) {
        BaseComp[] baseCompArray = baseComp.getChildren();
        if (baseCompArray == null) {
            return;
        }
        int n = 0;
        for (BaseComp baseComp2 : baseCompArray) {
            if (baseComp2 == null) continue;
            baseComp2.setBounds(0, n, baseComp2.getWidth(), baseComp2.getHeight());
            n += baseComp2.getHeight();
        }
    }
}

