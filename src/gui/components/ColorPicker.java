package gui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import event.UiEvent;

/**
 * A colour picker component that displays a grid of predefined colour
 * swatches. Users can click a swatch to select it, and the selected swatch
 * is visually highlighted with a white border. The component provides the
 * selected colour as a {@link Color} object. It is built as a transparent
 * {@link SurfaceCard} containing clickable coloured squares arranged in a
 * 5-column grid.
 */
public class ColorPicker extends SurfaceCard {
    private String selectedColor;
    private final List<SurfaceCard> colorSquares = new ArrayList<>();

    private final String[] colors = {
            "#ef4444", "#f97316", "#f59e0b", "#10b981", "#14b8a6",
            "#0ea5e9", "#3b82f6", "#6366f1", "#8b5cf6", "#d946ef"
    };

    /**
     * Constructs a colour picker at the given position and size, pre-selecting
     * the specified initial colour (or the first colour in the palette if null
     * or blank).
     *
     * @param x             the x-coordinate of the picker
     * @param y             the y-coordinate of the picker
     * @param width         the width of the picker
     * @param height        the height of the picker
     * @param initialColor  the hex colour string (e.g. "#ef4444") to preselect;
     *                      may be null or blank to use the default
     */
    public ColorPicker(int x, int y, int width, int height, String initialColor) {
        super(x, y, width, height, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), 0);

        int sqSize = 32;
        int gap = 8;
        int col = 0;
        int row = 0;

        for (String c : colors) {
            SurfaceCard sq = new SurfaceCard(
                    col * (sqSize + gap),
                    row * (sqSize + gap),
                    sqSize,
                    sqSize,
                    Color.decode(c),
                    Color.decode(c),
                    4);
            sq.getEventManager().register(UiEvent.Type.CLICK, (comp, ev) -> setSelectedColor(c));
            addChild(sq);
            colorSquares.add(sq);

            col++;
            if (col >= 5) {
                col = 0;
                row++;
            }
        }

        String initStr = colors[0];
        if (initialColor != null && !initialColor.isBlank()) {
            initStr = initialColor;
        }
        setSelectedColor(initStr);
    }

    /**
     * Programmatically selects a colour swatch by its hex string. The
     * selected swatch is highlighted with a white border.
     *
     * @param color the hex colour string to select (e.g. "#3b82f6"); must
     *              match one of the predefined palette entries
     */
    public void setSelectedColor(String color) {
        this.selectedColor = color;
        for (int i = 0; i < colors.length; i++) {
            SurfaceCard sq = colorSquares.get(i);
            sq.setBorderColor(colors[i].equals(color) ? Color.WHITE : Color.decode(colors[i]));
        }
        if (getOwnerWindow() != null) {
            getOwnerWindow().requestRenderIfNeeded();
        }
    }

    /**
     * Returns the currently selected colour as a {@link Color} instance.
     *
     * @return the selected colour; falls back to the first palette colour on
     *         any decoding error
     */
    public Color getSelectedColor() {
        try {
            return Color.decode(selectedColor);
        } catch (Exception e) {
            return Color.decode(colors[0]);
        }
    }
}
