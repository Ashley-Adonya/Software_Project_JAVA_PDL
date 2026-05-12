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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import main.BaseComp;

public class SegmentedSelect
extends BaseComp {
    private final List<String> options = new ArrayList<String>();
    private int selectedIndex = 0;
    private Consumer<String> onChange = null;

    public SegmentedSelect(int n, int n2, int n3, int n4) {
        super(null);
        this.setBounds(n, n2, n3, n4);
        this.setCursor(12);
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (uiEvent.getTarget() != this || this.options.isEmpty()) {
                return;
            }
            int n = this.toLocalX(uiEvent.getX());
            int n2 = Math.max(0, Math.min(this.options.size() - 1, (int)((double)n / (double)Math.max(1, this.getWidth()) * (double)this.options.size())));
            this.setSelectedIndex(n2, true);
            uiEvent.stopPropagation();
        });
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        int n = 10;
        graphics2D.setColor(new Color(236, 240, 248));
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), n, n);
        graphics2D.setColor(new Color(198, 208, 222));
        graphics2D.setStroke(new BasicStroke(1.2f));
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, n, n);
        if (this.options.isEmpty()) {
            graphics2D.setColor(new Color(110, 122, 139));
            graphics2D.setFont(new Font("Dialog", 0, 13));
            graphics2D.drawString("No options", 10, Math.max(18, this.getHeight() / 2 + 5));
            return;
        }
        int n2 = Math.max(1, this.getWidth() / this.options.size());
        for (int i = 0; i < this.options.size(); ++i) {
            boolean bl;
            int n3 = i * n2;
            int n4 = i == this.options.size() - 1 ? this.getWidth() - n3 : n2;
            boolean bl2 = bl = i == this.selectedIndex;
            if (bl) {
                graphics2D.setColor(new Color(66, 133, 244));
                graphics2D.fillRoundRect(n3 + 1, 1, Math.max(1, n4 - 2), Math.max(1, this.getHeight() - 2), n, n);
            }
            graphics2D.setColor(bl ? Color.WHITE : new Color(62, 75, 93));
            graphics2D.setFont(new Font("Dialog", 1, 13));
            String string = this.options.get(i);
            int n5 = graphics2D.getFontMetrics().stringWidth(string);
            int n6 = n3 + Math.max(8, (n4 - n5) / 2);
            int n7 = Math.max(18, this.getHeight() / 2 + 5);
            graphics2D.drawString(string, n6, n7);
            if (i >= this.options.size() - 1) continue;
            graphics2D.setColor(new Color(205, 214, 226));
            graphics2D.drawLine(n3 + n4, 6, n3 + n4, this.getHeight() - 7);
        }
    }

    public void setOptions(List<String> list) {
        this.options.clear();
        if (list != null) {
            this.options.addAll(list);
        }
        if (this.selectedIndex >= this.options.size()) {
            this.selectedIndex = Math.max(0, this.options.size() - 1);
        }
        this.invalidate();
    }

    public void setSelectedOption(String string) {
        int n = this.options.indexOf(string);
        if (n >= 0) {
            this.setSelectedIndex(n, false);
        }
    }

    public String getSelectedOption() {
        if (this.options.isEmpty()) {
            return "";
        }
        return this.options.get(this.selectedIndex);
    }

    public void setOnChange(Consumer<String> consumer) {
        this.onChange = consumer;
    }

    private void setSelectedIndex(int n, boolean bl) {
        int n2 = Math.max(0, Math.min(n, Math.max(0, this.options.size() - 1)));
        if (this.selectedIndex == n2) {
            return;
        }
        this.selectedIndex = n2;
        this.invalidate();
        if (bl && this.onChange != null && !this.options.isEmpty()) {
            this.onChange.accept(this.options.get(this.selectedIndex));
        }
    }
}

