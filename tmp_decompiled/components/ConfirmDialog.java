/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Button;
import components.FormModal;
import components.Label;
import java.awt.Color;

public class ConfirmDialog
extends FormModal {
    public ConfirmDialog(int n, int n2, String string, String string2, Runnable runnable, Runnable runnable2) {
        super(n, n2, string, runnable2);
        Label label = new Label(string2 == null ? "Confirmer l'action ?" : string2, 18, 18, n - 36, 54);
        label.setColor(new Color(72, 84, 102));
        this.getBody().addChild(label);
        Button button = new Button("Annuler", n - 244, n2 - 52 - 54, 104, 34, runnable2);
        button.setBackground(new Color(191, 201, 216));
        button.setForeground(new Color(58, 70, 86));
        this.getBody().addChild(button);
        Button button2 = new Button("Confirmer", n - 128, n2 - 52 - 54, 104, 34, runnable);
        button2.setBackground(new Color(216, 102, 102));
        button2.setForeground(Color.WHITE);
        this.getBody().addChild(button2);
    }
}

