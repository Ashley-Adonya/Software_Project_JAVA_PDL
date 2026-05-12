/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Button;
import components.Div;
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
import main.BaseWindow;

public class SelectInput
extends BaseComp {
    private final List<String> options = new ArrayList<String>();
    private int selectedIndex = 0;
    private Color background = Color.WHITE;
    private Color border = new Color(200, 205, 211);
    private Color focusBorder = new Color(66, 133, 244);
    private Color textColor = new Color(40, 46, 54);
    private boolean popupOpen = false;
    private BaseComp popupLayer = null;
    private Consumer<String> onChange = null;

    public SelectInput(int n, int n2, int n3, int n4) {
        super(null);
        this.setBounds(n, n2, n3, n4);
        this.setFocusable(true);
        this.setCursor(12);
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (uiEvent.getTarget() != this) {
                return;
            }
            this.togglePopup();
            uiEvent.stopPropagation();
        });
    }

    @Override
    public boolean onKeyPressed(int n, char c) {
        if (!this.isFocused()) {
            return false;
        }
        if (this.options.isEmpty()) {
            return true;
        }
        if (n == 39 || n == 40) {
            this.selectedIndex = (this.selectedIndex + 1) % this.options.size();
            this.fireOnChange();
            this.invalidate();
            return true;
        }
        if (n == 37 || n == 38) {
            this.selectedIndex = (this.selectedIndex - 1 + this.options.size()) % this.options.size();
            this.fireOnChange();
            this.invalidate();
            return true;
        }
        if (n == 27) {
            this.closePopup();
            return true;
        }
        return false;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        int n = 10;
        graphics2D.setColor(this.background);
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), n, n);
        graphics2D.setColor(this.isFocused() ? this.focusBorder : this.border);
        graphics2D.setStroke(new BasicStroke(this.isFocused() ? 2.0f : 1.5f));
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, n, n);
        graphics2D.setColor(this.textColor);
        graphics2D.setFont(new Font("Dialog", 0, 14));
        String string = this.getSelectedOption();
        int n2 = Math.max(18, this.getHeight() / 2 + 6);
        graphics2D.drawString(string, 12, n2);
        int n3 = this.getWidth() - 20;
        int n4 = this.getHeight() / 2 - 2;
        if (this.popupOpen) {
            graphics2D.drawLine(n3 - 4, n4 + 4, n3, n4);
            graphics2D.drawLine(n3, n4, n3 + 4, n4 + 4);
        } else {
            graphics2D.drawLine(n3 - 4, n4, n3, n4 + 4);
            graphics2D.drawLine(n3, n4 + 4, n3 + 4, n4);
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
        if (string == null) {
            return;
        }
        int n = this.options.indexOf(string);
        if (n >= 0) {
            this.selectedIndex = n;
            this.invalidate();
            this.fireOnChange();
        }
    }

    public String getSelectedOption() {
        if (this.options.isEmpty()) {
            return "No option";
        }
        return this.options.get(this.selectedIndex);
    }

    public void setOnChange(Consumer<String> consumer) {
        this.onChange = consumer;
    }

    private void togglePopup() {
        if (this.popupOpen) {
            this.closePopup();
            return;
        }
        this.openPopup();
    }

    private void openPopup() {
        if (this.options.isEmpty() || this.popupOpen) {
            return;
        }
        BaseWindow baseWindow = this.getOwnerWindow();
        if (baseWindow == null || baseWindow.getLayerHost() == null) {
            return;
        }
        int n = Math.min(220, Math.max(30, this.options.size() * 30) + 8);
        BaseComp baseComp = new BaseComp(this, null){

            @Override
            public void customGraphics(Graphics graphics) {
            }
        };
        baseComp.setBounds(0, 0, baseWindow.getLayerHost().getWidth(), baseWindow.getLayerHost().getHeight());
        Div div = new Div(0, 0, Math.max(120, this.getWidth()), n, Color.WHITE, 10);
        int n2 = this.getGlobalX() - baseComp.getGlobalX();
        int n3 = this.getGlobalY() - baseComp.getGlobalY() + this.getHeight() + 4;
        div.setBounds(n2, n3, div.getWidth(), div.getHeight());
        for (int i = 0; i < this.options.size(); ++i) {
            String string = this.options.get(i);
            int n4 = i;
            Button button = new Button(string, 4, 4 + i * 30, div.getWidth() - 8, 28, () -> {
                this.selectedIndex = n4;
                this.fireOnChange();
                this.closePopup();
                this.invalidate();
            });
            button.setBackground(n4 == this.selectedIndex ? new Color(66, 133, 244) : new Color(244, 247, 252));
            button.setForeground(n4 == this.selectedIndex ? Color.WHITE : new Color(54, 66, 82));
            div.addChild(button);
        }
        baseComp.addChild(div);
        baseComp.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp2, uiEvent) -> {
            if (uiEvent.getTarget() == baseComp) {
                this.closePopup();
                uiEvent.stopPropagation();
            }
        });
        this.popupLayer = baseComp;
        this.popupOpen = true;
        baseWindow.openLayer(baseComp);
        this.invalidate();
    }

    private void closePopup() {
        if (!this.popupOpen) {
            return;
        }
        if (this.popupLayer != null && this.popupLayer.getParent() != null) {
            this.popupLayer.getParent().removeChild(this.popupLayer);
        }
        this.popupLayer = null;
        this.popupOpen = false;
        this.invalidate();
    }

    private void fireOnChange() {
        if (this.onChange != null && !this.options.isEmpty()) {
            this.onChange.accept(this.options.get(this.selectedIndex));
        }
    }
}

