import java.nio.file.*;
import java.util.regex.*;

public class Fixer {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/gui/screen/AdminDashboardScreen.java");
        String content = new String(Files.readAllBytes(p), "UTF-8");
        String regex = "(?s)private static class ColorPicker extends Div \\{.*?return selectedColor;\\s*\\}\\s*\\}";
        String replacement = "private static class ColorPicker extends gui.components.SurfaceCard {\\n" +
            "        private String selectedColor;\\n" +
            "        private final java.util.List<gui.components.SurfaceCard> colorSquares = new java.util.ArrayList<>();\\n" +
            "        private final String[] colors = { \"#ef4444\", \"#f97316\", \"#f59e0b\", \"#10b981\", \"#14b8a6\", \"#0ea5e9\", \"#3b82f6\", \"#6366f1\", \"#8b5cf6\", \"#d946ef\" };\\n" +
            "        public ColorPicker(int x, int y, int width, int height, java.awt.Color initialColor) {\\n" +
            "            super(x, y, width, height, new java.awt.Color(0,0,0,0), new java.awt.Color(0,0,0,0), 0);\\n" +
            "            int sqSize = 32; int gap = 8; int col = 0; int row = 0;\\n" +
            "            for (String c : colors) {\\n" +
            "                gui.components.SurfaceCard sq = new gui.components.SurfaceCard(col * (sqSize + gap), row * (sqSize + gap), sqSize, sqSize, java.awt.Color.decode(c), java.awt.Color.decode(c), 4);\\n" +
            "                sq.getEventManager().register(event.UiEvent.Type.CLICK, (comp, ev) -> { setSelectedColor(c); });\\n" +
            "                addChild(sq); colorSquares.add(sq); col++; if (col >= 5) { col = 0; row++; }\\n" +
            "            }\\n" +
            "            String initStr = colors[0];\\n" +
            "            if (initialColor != null) {\\n" +
            "                String hex = Integer.toHexString(initialColor.getRGB());\\n" +
            "                initStr = \"#\" + hex.substring(Math.max(0, hex.length() - 6));\\n" +
            "            }\\n" +
            "            setSelectedColor(initStr);\\n" +
            "        }\\n" +
            "        public void setSelectedColor(String color) {\\n" +
            "            this.selectedColor = color;\\n" +
            "            for (int i = 0; i < colors.length; i++) {\\n" +
            "                gui.components.SurfaceCard sq = colorSquares.get(i);\\n" +
            "                sq.setBorderColor(colors[i].equals(color) ? java.awt.Color.WHITE : java.awt.Color.decode(colors[i]));\\n" +
            "            }\\n" +
            "            if (getOwnerWindow() != null) getOwnerWindow().requestRenderIfNeeded();\\n" +
            "        }\\n" +
            "        public java.awt.Color getSelectedColor() {\\n" +
            "            try { return java.awt.Color.decode(selectedColor); } catch(Exception e) { return java.awt.Color.decode(colors[0]); }\\n" +
            "        }\\n" +
            "    }";
        content = content.replaceAll(regex, replacement);
        Files.write(p, content.getBytes("UTF-8"));
    }
}
