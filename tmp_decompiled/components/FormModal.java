/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Button;
import components.Div;
import components.H;
import java.awt.Color;
import java.awt.Font;
import main.BaseComp;

public class FormModal
extends Div {
    private final H titleLabel;
    private final Button closeButton;
    private final Div body;
    private boolean initialized = false;

    public FormModal(int n, int n2, String string, Runnable runnable) {
        super(0, 0, n, n2, new Color(255, 255, 255), 16);
        this.titleLabel = new H(string == null ? "" : string, 18, 14, Math.max(80, n - 88), 28);
        this.titleLabel.setColor(new Color(27, 39, 56));
        this.titleLabel.setFont(new Font("Dialog", 1, 20));
        this.addChild(this.titleLabel);
        this.closeButton = new Button("X", n - 50, 14, 32, 28, runnable);
        this.closeButton.setBackground(new Color(222, 229, 238));
        this.closeButton.setForeground(new Color(54, 66, 82));
        this.addChild(this.closeButton);
        this.body = new Div(0, 54, n, n2 - 54, new Color(0, 0, 0, 0), 0);
        this.addChild(this.body);
        this.initialized = true;
        this.layoutInternal(n, n2);
    }

    public BaseComp getBody() {
        return this.body;
    }

    public void setTitle(String string) {
        this.titleLabel.setText(string == null ? "" : string);
        this.titleLabel.invalidate();
    }

    @Override
    public void setBounds(int n, int n2, int n3, int n4) {
        super.setBounds(n, n2, n3, n4);
        if (!this.initialized) {
            return;
        }
        this.layoutInternal(n3, n4);
    }

    private void layoutInternal(int n, int n2) {
        this.titleLabel.setBounds(18, 14, Math.max(80, n - 88), 28);
        this.closeButton.setBounds(n - 50, 14, 32, 28);
        this.body.setBounds(0, 54, n, Math.max(10, n2 - 54));
    }
}

