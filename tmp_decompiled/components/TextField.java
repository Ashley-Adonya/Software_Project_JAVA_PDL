/*
 * Decompiled with CFR 0.152.
 */
package components;

import event.UiEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import main.BaseComp;

public class TextField
extends BaseComp {
    private static final int PADDING_X = 12;
    private static final int BASELINE_OFFSET = 6;
    private static final Font UI_FONT = new Font("Dialog", 0, 14);
    private static final int MAX_HISTORY = 120;
    private String text = "";
    private String placeholder = "";
    private int maxLength = 180;
    private Color background = Color.WHITE;
    private Color border = new Color(200, 205, 211);
    private Color focusBorder = new Color(66, 133, 244);
    private Color textColor = new Color(40, 46, 54);
    private Color placeholderColor = new Color(140, 146, 156);
    private Color selectionColor = new Color(66, 133, 244, 90);
    private int caretIndex = 0;
    private int selectionAnchor = 0;
    private boolean draggingSelection = false;
    private final Deque<EditState> undoStack = new ArrayDeque<EditState>();
    private final Deque<EditState> redoStack = new ArrayDeque<EditState>();

    public TextField(int n, int n2, int n3, int n4) {
        super(null);
        this.setBounds(n, n2, n3, n4);
        this.setFocusable(true);
        this.setCursor(2);
        this.getEventManager().register(UiEvent.Type.POINTER_DOWN, (baseComp, uiEvent) -> {
            if (uiEvent.getTarget() != this) {
                return;
            }
            int n = this.toLocalX(uiEvent.getX());
            int n2 = this.caretFromLocalX(n);
            this.setCaret(n2, false);
            this.draggingSelection = true;
            if (uiEvent.getWindow() != null) {
                uiEvent.getWindow().capturePointer(this);
            }
            uiEvent.stopPropagation();
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_MOVE, (baseComp, uiEvent) -> {
            if (!this.draggingSelection) {
                return;
            }
            int n = this.toLocalX(uiEvent.getX());
            int n2 = this.caretFromLocalX(n);
            this.setCaret(n2, true);
            uiEvent.stopPropagation();
            this.invalidate();
        });
        this.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp, uiEvent) -> {
            if (!this.draggingSelection) {
                return;
            }
            this.draggingSelection = false;
            if (uiEvent.getWindow() != null) {
                uiEvent.getWindow().releasePointer(this);
            }
            uiEvent.stopPropagation();
            this.invalidate();
        });
    }

    @Override
    public boolean onKeyPressed(KeyEvent keyEvent) {
        if (!this.isFocused() || keyEvent == null) {
            return false;
        }
        boolean bl = keyEvent.isControlDown() || keyEvent.isMetaDown();
        boolean bl2 = keyEvent.isShiftDown();
        int n = keyEvent.getKeyCode();
        if (bl) {
            if (this.isCtrlShortcut(keyEvent, n, 'a', 1)) {
                this.selectionAnchor = 0;
                this.caretIndex = this.text.length();
                this.invalidate();
                return true;
            }
            if (this.isCtrlShortcut(keyEvent, n, 'c', 3)) {
                this.copySelection();
                return true;
            }
            if (this.isCtrlShortcut(keyEvent, n, 'x', 24)) {
                this.cutSelection();
                return true;
            }
            if (this.isCtrlShortcut(keyEvent, n, 'v', 22)) {
                this.pasteFromClipboard();
                return true;
            }
            if (this.isCtrlShortcut(keyEvent, n, 'z', 26)) {
                if (bl2) {
                    this.redo();
                } else {
                    this.undo();
                }
                return true;
            }
            if (this.isCtrlShortcut(keyEvent, n, 'y', 25)) {
                this.redo();
                return true;
            }
        }
        if (n == 37) {
            this.setCaret(this.caretIndex - 1, bl2);
            this.invalidate();
            return true;
        }
        if (n == 39) {
            this.setCaret(this.caretIndex + 1, bl2);
            this.invalidate();
            return true;
        }
        if (n == 36 || n == 33) {
            this.setCaret(0, bl2);
            this.invalidate();
            return true;
        }
        if (n == 35 || n == 34) {
            this.setCaret(this.text.length(), bl2);
            this.invalidate();
            return true;
        }
        if (n == 8) {
            this.backspace();
            return true;
        }
        if (n == 127) {
            this.deleteForward();
            return true;
        }
        return n == 10;
    }

    @Override
    public boolean onKeyTyped(KeyEvent keyEvent) {
        if (!this.isFocused() || keyEvent == null) {
            return false;
        }
        if (keyEvent.isControlDown() || keyEvent.isMetaDown() || keyEvent.isAltDown()) {
            return false;
        }
        char c = keyEvent.getKeyChar();
        if (Character.isISOControl(c)) {
            return false;
        }
        this.replaceSelectionOrInsert(String.valueOf(c), false);
        return true;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        int n;
        int n2;
        int n3;
        int n4;
        Graphics2D graphics2D = (Graphics2D)graphics;
        int n5 = 10;
        graphics2D.setColor(this.background);
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), n5, n5);
        graphics2D.setColor(this.isFocused() ? this.focusBorder : this.border);
        graphics2D.setStroke(new BasicStroke(this.isFocused() ? 2.0f : 1.5f));
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, n5, n5);
        graphics2D.setFont(UI_FONT);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int n6 = Math.max(18, this.getHeight() / 2 + 6);
        if (this.text.isEmpty()) {
            graphics2D.setColor(this.placeholderColor);
            graphics2D.drawString(this.placeholder, 12, n6);
        } else {
            n4 = this.getSelectionStart();
            n3 = this.getSelectionEnd();
            if (this.hasSelection()) {
                n2 = 12 + fontMetrics.stringWidth(this.text.substring(0, n4));
                n = fontMetrics.stringWidth(this.text.substring(n4, n3));
                int n7 = Math.max(6, n6 - fontMetrics.getAscent());
                int n8 = Math.max(16, fontMetrics.getHeight());
                graphics2D.setColor(this.selectionColor);
                graphics2D.fillRoundRect(n2, n7, Math.max(1, n), n8, 6, 6);
            }
            graphics2D.setColor(this.textColor);
            graphics2D.drawString(this.text, 12, n6);
        }
        if (this.isFocused()) {
            n4 = Math.max(0, Math.min(this.caretIndex, this.text.length()));
            n3 = Math.min(this.getWidth() - 8, 12 + fontMetrics.stringWidth(this.text.substring(0, n4)) + 1);
            n2 = Math.max(8, n6 - fontMetrics.getAscent());
            n = Math.min(this.getHeight() - 8, n2 + fontMetrics.getHeight());
            graphics2D.setColor(this.focusBorder);
            graphics2D.drawLine(n3, n2, n3, n);
        }
    }

    public String getText() {
        return this.text;
    }

    public void setText(String string) {
        this.text = string == null ? "" : string;
        this.setCaret(this.text.length(), false);
        this.clearHistory();
        this.invalidate();
    }

    public void setPlaceholder(String string) {
        this.placeholder = string == null ? "" : string;
        this.invalidate();
    }

    public void setMaxLength(int n) {
        this.maxLength = Math.max(1, n);
    }

    private void setCaret(int n, boolean bl) {
        this.caretIndex = Math.max(0, Math.min(n, this.text.length()));
        if (!bl) {
            this.selectionAnchor = this.caretIndex;
        }
    }

    private boolean hasSelection() {
        return this.caretIndex != this.selectionAnchor;
    }

    private int getSelectionStart() {
        return Math.min(this.caretIndex, this.selectionAnchor);
    }

    private int getSelectionEnd() {
        return Math.max(this.caretIndex, this.selectionAnchor);
    }

    private void clearSelection() {
        this.selectionAnchor = this.caretIndex;
    }

    private int caretFromLocalX(int n) {
        int n2 = Math.max(0, n - 12);
        if (this.text.isEmpty() || n2 <= 0) {
            return 0;
        }
        FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(UI_FONT);
        for (int i = 0; i < this.text.length(); ++i) {
            int n3;
            int n4 = fontMetrics.stringWidth(this.text.substring(0, i));
            if (n2 > (n4 + (n3 = fontMetrics.stringWidth(this.text.substring(0, i + 1)))) / 2) continue;
            return i;
        }
        return this.text.length();
    }

    private void pushUndoState() {
        this.undoStack.push(new EditState(this.text, this.caretIndex, this.selectionAnchor));
        while (this.undoStack.size() > 120) {
            this.undoStack.removeLast();
        }
    }

    private void clearHistory() {
        this.undoStack.clear();
        this.redoStack.clear();
    }

    private void restoreState(EditState editState) {
        if (editState == null) {
            return;
        }
        this.text = editState.text;
        this.caretIndex = Math.max(0, Math.min(editState.caret, this.text.length()));
        this.selectionAnchor = Math.max(0, Math.min(editState.anchor, this.text.length()));
        this.invalidate();
    }

    private void undo() {
        if (this.undoStack.isEmpty()) {
            return;
        }
        this.redoStack.push(new EditState(this.text, this.caretIndex, this.selectionAnchor));
        this.restoreState(this.undoStack.pop());
    }

    private void redo() {
        if (this.redoStack.isEmpty()) {
            return;
        }
        this.undoStack.push(new EditState(this.text, this.caretIndex, this.selectionAnchor));
        this.restoreState(this.redoStack.pop());
    }

    private void replaceSelectionOrInsert(String string, boolean bl) {
        if (string == null || string.isEmpty()) {
            return;
        }
        String string2 = string;
        if (bl) {
            string2 = string2.replace('\r', ' ').replace('\n', ' ');
        }
        int n = this.getSelectionStart();
        int n2 = this.getSelectionEnd();
        int n3 = n2 - n;
        int n4 = this.maxLength - (this.text.length() - n3);
        if (n4 <= 0) {
            return;
        }
        if (string2.length() > n4) {
            string2 = string2.substring(0, n4);
        }
        this.pushUndoState();
        this.redoStack.clear();
        if (this.hasSelection()) {
            this.text = this.text.substring(0, n) + string2 + this.text.substring(n2);
            this.caretIndex = n + string2.length();
            this.clearSelection();
        } else {
            this.text = this.text.substring(0, this.caretIndex) + string2 + this.text.substring(this.caretIndex);
            this.caretIndex += string2.length();
            this.clearSelection();
        }
        this.invalidate();
    }

    private void deleteSelectionIfAny() {
        if (!this.hasSelection()) {
            return;
        }
        int n = this.getSelectionStart();
        int n2 = this.getSelectionEnd();
        this.text = this.text.substring(0, n) + this.text.substring(n2);
        this.caretIndex = n;
        this.clearSelection();
    }

    private void backspace() {
        if (this.hasSelection()) {
            this.pushUndoState();
            this.redoStack.clear();
            this.deleteSelectionIfAny();
            this.invalidate();
            return;
        }
        if (this.caretIndex <= 0 || this.text.isEmpty()) {
            return;
        }
        this.pushUndoState();
        this.redoStack.clear();
        this.text = this.text.substring(0, this.caretIndex - 1) + this.text.substring(this.caretIndex);
        --this.caretIndex;
        this.clearSelection();
        this.invalidate();
    }

    private void deleteForward() {
        if (this.hasSelection()) {
            this.pushUndoState();
            this.redoStack.clear();
            this.deleteSelectionIfAny();
            this.invalidate();
            return;
        }
        if (this.caretIndex >= this.text.length()) {
            return;
        }
        this.pushUndoState();
        this.redoStack.clear();
        this.text = this.text.substring(0, this.caretIndex) + this.text.substring(this.caretIndex + 1);
        this.clearSelection();
        this.invalidate();
    }

    private void copySelection() {
        if (!this.hasSelection()) {
            return;
        }
        String string = this.text.substring(this.getSelectionStart(), this.getSelectionEnd());
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
    }

    private void cutSelection() {
        if (!this.hasSelection()) {
            return;
        }
        this.copySelection();
        this.pushUndoState();
        this.redoStack.clear();
        this.deleteSelectionIfAny();
        this.invalidate();
    }

    private void pasteFromClipboard() {
        try {
            String string;
            Object object = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (object instanceof String && !(string = (String)object).isEmpty()) {
                this.replaceSelectionOrInsert(string, true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean isCtrlShortcut(KeyEvent keyEvent, int n, char c, int n2) {
        if (keyEvent == null) {
            return false;
        }
        char c2 = Character.toLowerCase(keyEvent.getKeyChar());
        if (c2 == c || c2 == (char)n2) {
            return true;
        }
        return switch (c) {
            case 'a' -> {
                if (n == 65) {
                    yield true;
                }
                yield false;
            }
            case 'c' -> {
                if (n == 67) {
                    yield true;
                }
                yield false;
            }
            case 'x' -> {
                if (n == 88) {
                    yield true;
                }
                yield false;
            }
            case 'v' -> {
                if (n == 86) {
                    yield true;
                }
                yield false;
            }
            case 'z' -> {
                if (n == 90) {
                    yield true;
                }
                yield false;
            }
            case 'y' -> {
                if (n == 89) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private static class EditState {
        final String text;
        final int caret;
        final int anchor;

        EditState(String string, int n, int n2) {
            this.text = string;
            this.caret = n;
            this.anchor = n2;
        }
    }
}

