/*
 * Decompiled with CFR 0.152.
 */
package components;

import event.UiEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import main.BaseComp;

public class CheckBox
extends BaseComp {
    private String label;
    private boolean checked;

    public CheckBox(String string, int n, int n2, int n3, int n4, boolean bl) {
        super(null);
        this.label = string;
        this.checked = bl;
        this.setCursor(12);
        this.setBounds(n, n2, n3, n4);
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (uiEvent.getTarget() != this) {
                return;
            }
            this.checked = !this.checked;
            this.invalidate();
        });
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        int n = 18;
        int n2 = (this.getHeight() - n) / 2;
        int n3 = 0;
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRoundRect(n3, n2, n, n, 6, 6);
        if (this.checked) {
            graphics2D.setColor(new Color(66, 133, 244));
            graphics2D.fillRoundRect(n3, n2, n, n, 6, 6);
            graphics2D.setColor(Color.WHITE);
            graphics2D.setStroke(new BasicStroke(2.0f, 1, 1));
            graphics2D.drawLine(n3 + 4, n2 + 9, n3 + 8, n2 + 13);
            graphics2D.drawLine(n3 + 8, n2 + 13, n3 + 14, n2 + 5);
        } else {
            graphics2D.setColor(new Color(200, 205, 211));
            graphics2D.setStroke(new BasicStroke(1.5f));
            graphics2D.drawRoundRect(n3, n2, n, n, 6, 6);
        }
        graphics2D.setColor(new Color(59, 63, 70));
        graphics2D.setFont(new Font("Dialog", 1, 13));
        int n4 = n2 + n - 4;
        graphics2D.drawString(this.label, n3 + n + 10, n4);
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setLabel(String string) {
        this.label = string;
    }
}

