/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Button;
import components.Div;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NavMenuBar
extends Div {
    private final List<Item> items = new ArrayList<Item>();
    private Consumer<String> selectionListener;
    private String selectedKey = null;

    public NavMenuBar(int n, int n2, int n3, int n4) {
        super(n, n2, n3, n4, new Color(27, 34, 48, 185), 14);
    }

    public void addItem(String string, String string2) {
        Button button = new Button(string2, 0, 0, 120, 34, () -> this.selectKey(string, true));
        button.setBackground(new Color(53, 64, 85));
        button.setForeground(new Color(225, 230, 240));
        this.addChild(button);
        int n = Math.max(96, Math.min(180, string2.length() * 8 + 30));
        this.items.add(new Item(string, n, button));
        this.relayoutButtons();
        if (this.selectedKey == null) {
            this.selectKey(string, false);
        }
    }

    public void setSelectionListener(Consumer<String> consumer) {
        this.selectionListener = consumer;
    }

    public String getSelectedKey() {
        return this.selectedKey;
    }

    public void setSelectedKey(String string) {
        this.selectKey(string, false);
    }

    @Override
    public void setBounds(int n, int n2, int n3, int n4) {
        super.setBounds(n, n2, n3, n4);
        this.relayoutButtons();
    }

    private void relayoutButtons() {
        if (this.items == null || this.items.isEmpty()) {
            return;
        }
        int n = 10;
        int n2 = 8;
        int n3 = Math.max(28, this.getHeight() - 16);
        for (Item item : this.items) {
            int n4 = item.width;
            item.button.setBounds(n, n2, n4, n3);
            n += n4 + 8;
        }
    }

    private void selectKey(String string, boolean bl) {
        if (string == null || string.equals(this.selectedKey)) {
            return;
        }
        this.selectedKey = string;
        this.applyVisualState();
        if (bl && this.selectionListener != null) {
            this.selectionListener.accept(string);
        }
    }

    private void applyVisualState() {
        for (Item item : this.items) {
            boolean bl = item.key.equals(this.selectedKey);
            item.button.setBackground(bl ? new Color(68, 145, 255) : new Color(53, 64, 85));
            item.button.setForeground(bl ? Color.WHITE : new Color(225, 230, 240));
            item.button.invalidate();
        }
        this.invalidate();
    }

    private static class Item {
        String key;
        int width;
        Button button;

        Item(String string, int n, Button button) {
            this.key = string;
            this.width = n;
            this.button = button;
        }
    }
}

