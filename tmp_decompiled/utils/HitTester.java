/*
 * Decompiled with CFR 0.152.
 */
package utils;

import main.BaseComp;

public class HitTester {
    public boolean isHit(int n, int n2, BaseComp baseComp) {
        return baseComp != null && baseComp.containsGlobalPoint(n, n2);
    }

    public BaseComp findBaseComp(int n, int n2, BaseComp baseComp) {
        if (baseComp == null || !baseComp.isVisible() || !this.isHit(n, n2, baseComp)) {
            return null;
        }
        BaseComp[] baseCompArray = baseComp.getChildren();
        if (baseCompArray != null && baseCompArray.length > 0) {
            for (int i = baseCompArray.length - 1; i >= 0; --i) {
                BaseComp baseComp2;
                BaseComp baseComp3 = baseCompArray[i];
                if (baseComp3 == null || !baseComp3.isVisible() || (baseComp2 = this.findBaseComp(n, n2, baseComp3)) == null) continue;
                return baseComp2;
            }
        }
        return baseComp;
    }
}

