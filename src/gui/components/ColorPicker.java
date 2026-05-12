package gui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import event.UiEvent;

public class ColorPicker extends SurfaceCard {
    private String selectedColor;
    private final List<SurfaceCard> colorSquares = new ArrayList<>();

    private final String[] colors = {
            "#ef4444", "#f97316", "#f59e0b", "#10b981", "#14b8a6",
            "#0ea5e9", "#3b82f6", "#6366f1", "#8b5cf6", "#d946ef"
    };

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

    public Color getSelectedColor() {
        try {
            return Color.decode(selectedColor);
        } catch (Exception e) {
            return Color.decode(colors[0]);
        }
    }
}
