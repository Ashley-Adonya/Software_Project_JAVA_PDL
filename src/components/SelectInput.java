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
import main.BaseWindow;

/**
 * A custom dropdown / select input component for the PDL application UI.
 * <p>
 * Displays the currently selected option in a rounded rectangle. On click a
 * pop-up layer containing a scrollable list of options is opened on top of the
 * window via {@link BaseWindow#openLayer(BaseComp)}. Navigation can also be
 * performed with the keyboard (arrow keys and Escape). The selected value is
 * communicated to clients through a {@link Consumer} callback.
 * </p>
 */
public class SelectInput extends BaseComp {
    private static final int ROW_HEIGHT = 30;
    private static final int MAX_VISIBLE_ROWS = 6;

    private final List<String> options = new ArrayList<>();
    private int selectedIndex = 0;
    private Color background = Color.WHITE;
    private Color border = new Color(200, 205, 211);
    private Color focusBorder = new Color(66, 133, 244);
    private Color textColor = new Color(40, 46, 54);
    private boolean popupOpen = false;
    private BaseComp popupLayer = null;
    private Consumer<String> onChange = null;

    /**
     * Constructs a SelectInput at the given position and size.
     * <p>
     * Registers a POINTER_UP listener that toggles the pop-up option list. The
     * component is focusable and uses a hand cursor.
     * </p>
     *
     * @param x      the x-coordinate of the component's top-left corner
     * @param y      the y-coordinate of the component's top-left corner
     * @param width  the component width in pixels
     * @param height the component height in pixels
     */
    public SelectInput(int x, int y, int width, int height) {
        super(null);
        setBounds(x, y, width, height);
        setFocusable(true);
        setCursor(java.awt.Cursor.HAND_CURSOR);

        getEventManager().register(UiEvent.Type.POINTER_UP, (component, event) -> {
            if (event.getTarget() != this) {
                return;
            }
            togglePopup();
            event.stopPropagation();
        });
    }

    /**
     * Handles keyboard input to change the selected option or close the pop-up.
     * <p>
     * RIGHT / DOWN increment the selection index; LEFT / UP decrement it.
     * ESCAPE closes the pop-up if open. Only responds when the component has
     * input focus.
     * </p>
     *
     * @param keyCode the integer code of the pressed key
     * @param keyChar the character associated with the pressed key
     * @return {@code true} if the key press was handled, {@code false} otherwise
     */
    @Override
    public boolean onKeyPressed(int keyCode, char keyChar) {
        if (!isFocused()) {
            return false;
        }
        if (options.isEmpty()) {
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_RIGHT || keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            selectedIndex = (selectedIndex + 1) % options.size();
            fireOnChange();
            invalidate();
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT || keyCode == java.awt.event.KeyEvent.VK_UP) {
            selectedIndex = (selectedIndex - 1 + options.size()) % options.size();
            fireOnChange();
            invalidate();
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            closePopup();
            return true;
        }
        return false;
    }

    /**
     * Paints the select input's visual representation.
     * <p>
     * Draws a rounded rectangle border (highlighted when focused), the currently
     * selected option text (trimmed with ellipsis if necessary), and a downward /
     * upward arrow indicating the pop-up state.
     * </p>
     *
     * @param g the {@link Graphics} context supplied by the rendering pipeline
     */
    @Override
    public void customGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int radius = 10;

        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.setColor(isFocused() ? focusBorder : border);
        g2.setStroke(new BasicStroke(isFocused() ? 2.0f : 1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.setColor(textColor);
        g2.setFont(new Font("Dialog", Font.PLAIN, 14));
        String value = getSelectedOption();
        int baseline = Math.max(18, (getHeight() / 2) + 6);
        int available = Math.max(10, getWidth() - 34);
        g2.drawString(trimToWidth(g2, value, available), 12, baseline);

        int arrowX = getWidth() - 20;
        int arrowY = (getHeight() / 2) - 2;
        if (popupOpen) {
            g2.drawLine(arrowX - 4, arrowY + 4, arrowX, arrowY);
            g2.drawLine(arrowX, arrowY, arrowX + 4, arrowY + 4);
        } else {
            g2.drawLine(arrowX - 4, arrowY, arrowX, arrowY + 4);
            g2.drawLine(arrowX, arrowY + 4, arrowX + 4, arrowY);
        }
    }

    /**
     * Replaces the current list of selectable options.
     * <p>
     * Resets the selection index to stay within valid bounds. Does NOT fire the
     * change callback.
     * </p>
     *
     * @param values the new option strings; if {@code null} the list is cleared
     */
    public void setOptions(List<String> values) {
        options.clear();
        if (values != null) {
            options.addAll(values);
        }
        if (selectedIndex >= options.size()) {
            selectedIndex = Math.max(0, options.size() - 1);
        }
        invalidate();
    }

    /**
     * Selects the option that matches the given value.
     * <p>
     * If the value is found in the current option list the selection index is
     * updated and {@link #fireOnChange()} is called. Does nothing if the value
     * is {@code null} or not present.
     * </p>
     *
     * @param value the option string to select
     */
    public void setSelectedOption(String value) {
        if (value == null) {
            return;
        }
        int idx = options.indexOf(value);
        if (idx >= 0) {
            selectedIndex = idx;
            invalidate();
            fireOnChange();
        }
    }

    /**
     * Returns the currently selected option string.
     *
     * @return the selected text, or {@code "No option"} if the option list is
     *         empty
     */
    public String getSelectedOption() {
        if (options.isEmpty()) {
            return "No option";
        }
        return options.get(selectedIndex);
    }

    /**
     * Registers a callback that will be invoked whenever the selected option
     * changes (either via click, keyboard, or
     * {@link #setSelectedOption(String)}).
     *
     * @param onChange a {@link Consumer} receiving the newly selected option
     *                 string; may be {@code null} to clear
     */
    public void setOnChange(Consumer<String> onChange) {
        this.onChange = onChange;
    }

    private void togglePopup() {
        if (popupOpen) {
            closePopup();
            return;
        }
        openPopup();
    }

    private void openPopup() {
        if (options.isEmpty() || popupOpen) {
            return;
        }
        BaseWindow window = getOwnerWindow();
        if (window == null || window.getLayerHost() == null) {
            return;
        }

        int maxVisible = Math.min(MAX_VISIBLE_ROWS, options.size());
        int listHeight = Math.max(ROW_HEIGHT, maxVisible * ROW_HEIGHT);
        int popupHeight = listHeight + 8;

        BaseComp layer = new BaseComp(null) {
            @Override
            public void customGraphics(Graphics g) {
                // transparent click catcher layer
            }
        };
        layer.setBounds(0, 0, window.getLayerHost().getWidth(), window.getLayerHost().getHeight());

        Div panel = new Div(0, 0, Math.max(120, getWidth()), popupHeight, Color.WHITE, 10);
        int panelX = getGlobalX() - layer.getGlobalX();
        int panelY = getGlobalY() - layer.getGlobalY() + getHeight() + 4;
        panel.setBounds(panelX, panelY, panel.getWidth(), panel.getHeight());

        ScrollView listScroll = new ScrollView(4, 4, panel.getWidth() - 8, listHeight);
        BaseComp list = listScroll.getContent();

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            final int idx = i;
            Button row = new Button(option, 0, i * ROW_HEIGHT, listScroll.getWidth(), ROW_HEIGHT - 2, () -> {
                selectedIndex = idx;
                fireOnChange();
                closePopup();
                invalidate();
            });
            row.setBackground(idx == selectedIndex ? new Color(66, 133, 244) : new Color(244, 247, 252));
            row.setForeground(idx == selectedIndex ? Color.WHITE : new Color(54, 66, 82));
            list.addChild(row);
        }

        listScroll.setContentHeight(Math.max(listHeight, options.size() * ROW_HEIGHT));
        listScroll.setContentWidth(listScroll.getWidth());
        panel.addChild(listScroll);

        layer.addChild(panel);
        layer.getEventManager().register(UiEvent.Type.POINTER_UP, (component, event) -> {
            if (event.getTarget() == layer) {
                closePopup();
                event.stopPropagation();
            }
        });

        popupLayer = layer;
        popupOpen = true;
        window.openLayer(layer);
        invalidate();
    }

    private void closePopup() {
        if (!popupOpen) {
            return;
        }
        if (popupLayer != null && popupLayer.getParent() != null) {
            popupLayer.getParent().removeChild(popupLayer);
        }
        popupLayer = null;
        popupOpen = false;
        invalidate();
    }

    private void fireOnChange() {
        if (onChange != null && !options.isEmpty()) {
            onChange.accept(options.get(selectedIndex));
        }
    }

    private String trimToWidth(Graphics2D g2, String text, int maxWidth) {
        if (text == null) {
            return "";
        }
        if (g2.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int limit = Math.max(0, maxWidth - g2.getFontMetrics().stringWidth(ellipsis));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String candidate = builder.toString() + text.charAt(i);
            if (g2.getFontMetrics().stringWidth(candidate) > limit) {
                break;
            }
            builder.append(text.charAt(i));
        }
        return builder + ellipsis;
    }
}