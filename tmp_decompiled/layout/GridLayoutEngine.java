/*
 * Decompiled with CFR 0.152.
 */
package layout;

import layout.BaseLayoutEngine;
import main.BaseComp;

public class GridLayoutEngine
extends BaseLayoutEngine {
    @Override
    public void doLayout(BaseComp baseComp) {
        BaseComp[] baseCompArray = baseComp.getChildren();
        if (baseCompArray == null || baseCompArray.length == 0 || baseComp.getStyleManager() == null) {
            return;
        }
        int n = baseCompArray.length;
        boolean bl = baseComp.getStyleManager().isRowFirst();
        int n2 = baseComp.getStyleManager().getGap();
        if (bl) {
            int n3 = Math.max(1, baseComp.getStyleManager().getNumRows());
            int n4 = (int)Math.ceil((double)n / (double)n3);
            int n5 = Math.max(1, baseComp.getWidth() / Math.max(1, n4) - n2);
            int n6 = Math.max(1, baseComp.getHeight() / Math.max(1, n3) - n2);
            for (int i = 0; i < n; ++i) {
                int n7 = i / n4;
                int n8 = i % n4;
                baseCompArray[i].setBounds(n8 * (n5 + n2), n7 * (n6 + n2), n5, n6);
            }
        } else {
            int n9 = Math.max(1, baseComp.getStyleManager().getNumCols());
            int n10 = (int)Math.ceil((double)n / (double)n9);
            int n11 = Math.max(1, baseComp.getWidth() / Math.max(1, n9) - n2);
            int n12 = Math.max(1, baseComp.getHeight() / Math.max(1, n10) - n2);
            for (int i = 0; i < n; ++i) {
                int n13 = i / n10;
                int n14 = i % n10;
                baseCompArray[i].setBounds(n13 * (n11 + n2), n14 * (n12 + n2), n11, n12);
            }
        }
    }
}

