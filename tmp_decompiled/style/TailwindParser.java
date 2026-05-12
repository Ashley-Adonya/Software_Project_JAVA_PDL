/*
 * Decompiled with CFR 0.152.
 */
package style;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import style.StyleManager;

public class TailwindParser {
    private static final Map<String, Color> COLOR_MAP = TailwindParser.buildColorMap();

    public static void applyTailwind(StyleManager styleManager, String string) {
        String[] stringArray;
        if (styleManager == null || string == null || string.isBlank()) {
            return;
        }
        for (String string2 : stringArray = string.trim().split("\\s+")) {
            if (string2 == null || string2.isBlank()) continue;
            String string3 = string2.trim();
            if (string3.startsWith("bg-")) {
                TailwindParser.applyBackground(styleManager, string3.substring(3));
                continue;
            }
            if ("flex".equals(string3)) {
                styleManager.setLayoutEngineType("flex");
                continue;
            }
            if ("grid".equals(string3)) {
                styleManager.setLayoutEngineType("grid");
                continue;
            }
            if ("block".equals(string3)) {
                styleManager.setLayoutEngineType("block");
                continue;
            }
            if ("absolute".equals(string3) || "relative".equals(string3) || "fixed".equals(string3) || "sticky".equals(string3)) {
                styleManager.setLayoutEngineType("absolute");
                continue;
            }
            if ("flex-col".equals(string3)) {
                styleManager.setFlexProps(true, styleManager.getGap());
                continue;
            }
            if ("flex-row".equals(string3)) {
                styleManager.setFlexProps(false, styleManager.getGap());
                continue;
            }
            if (string3.startsWith("gap-")) {
                int n = TailwindParser.parseSpacing(string3.substring(4));
                styleManager.setFlexProps(styleManager.isColumnFlex(), n);
                styleManager.setBlockProps(n);
                continue;
            }
            if (string3.startsWith("grid-cols-")) {
                int n = Math.max(1, TailwindParser.parseNumericToken(string3.substring("grid-cols-".length()), 1));
                styleManager.setGridProps(false, styleManager.getNumRows(), n);
                continue;
            }
            if (string3.startsWith("grid-rows-")) {
                int n = Math.max(1, TailwindParser.parseNumericToken(string3.substring("grid-rows-".length()), 1));
                styleManager.setGridProps(true, n, styleManager.getNumCols());
                continue;
            }
            if (string3.startsWith("rounded")) {
                Integer n = TailwindParser.parseRounded(string3);
                if (n == null) continue;
                styleManager.setBorderRadius(n);
                continue;
            }
            if (string3.startsWith("w-")) {
                TailwindParser.applyWidth(styleManager, string3.substring(2));
                continue;
            }
            if (!string3.startsWith("h-")) continue;
            TailwindParser.applyHeight(styleManager, string3.substring(2));
        }
    }

    private static void applyBackground(StyleManager styleManager, String string) {
        Color color;
        if (string == null || string.isBlank()) {
            return;
        }
        String string2 = string;
        Integer n = null;
        int n2 = string.indexOf(47);
        if (n2 > 0 && n2 < string.length() - 1) {
            string2 = string.substring(0, n2);
            n = TailwindParser.parseAlpha(string.substring(n2 + 1));
        }
        if ((color = TailwindParser.parseColor(string2)) == null) {
            return;
        }
        if (n != null) {
            int n3 = Math.max(0, Math.min(255, n));
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), n3);
        }
        styleManager.setColor(color);
    }

    private static void applyWidth(StyleManager styleManager, String string) {
        Integer n = TailwindParser.parseSizeToken(string);
        if (n != null) {
            styleManager.setWidth(n);
        }
    }

    private static void applyHeight(StyleManager styleManager, String string) {
        Integer n = TailwindParser.parseSizeToken(string);
        if (n != null) {
            styleManager.setHeight(n);
        }
    }

    private static Integer parseSizeToken(String string) {
        String[] stringArray;
        if (string == null || string.isBlank()) {
            return null;
        }
        if ("full".equals(string)) {
            return -1;
        }
        if ("screen".equals(string)) {
            return 1000;
        }
        if (string.contains("/") && (stringArray = string.split("/")).length == 2) {
            try {
                double d = Double.parseDouble(stringArray[0]);
                double d2 = Double.parseDouble(stringArray[1]);
                if (d2 != 0.0) {
                    return (int)Math.max(1L, Math.round(d / d2 * 1000.0));
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return TailwindParser.parseNumericToken(string, null);
    }

    private static Integer parseRounded(String string) {
        if ("rounded-none".equals(string)) {
            return 0;
        }
        if ("rounded-sm".equals(string)) {
            return 4;
        }
        if ("rounded".equals(string) || "rounded-md".equals(string)) {
            return 8;
        }
        if ("rounded-lg".equals(string)) {
            return 12;
        }
        if ("rounded-xl".equals(string)) {
            return 16;
        }
        if ("rounded-2xl".equals(string)) {
            return 24;
        }
        if ("rounded-3xl".equals(string)) {
            return 32;
        }
        if ("rounded-full".equals(string)) {
            return 9999;
        }
        if (string.startsWith("rounded-[") && string.endsWith("]")) {
            String string2 = string.substring("rounded-[".length(), string.length() - 1);
            return TailwindParser.parseNumericToken(string2, 8);
        }
        return null;
    }

    private static int parseSpacing(String string) {
        Integer n = TailwindParser.parseNumericToken(string, 0);
        return Math.max(0, n == null ? 0 : n);
    }

    private static Integer parseNumericToken(String string, Integer n) {
        if (string == null || string.isBlank()) {
            return n;
        }
        String string2 = string.trim();
        boolean bl = false;
        if (string2.startsWith("[") && string2.endsWith("]") && string2.length() > 2) {
            string2 = string2.substring(1, string2.length() - 1);
            bl = true;
        }
        if (string2.endsWith("px")) {
            string2 = string2.substring(0, string2.length() - 2);
        }
        if (string2.endsWith("rem")) {
            try {
                double d = Double.parseDouble(string2.substring(0, string2.length() - 3));
                return (int)Math.round(d * 16.0);
            }
            catch (NumberFormatException numberFormatException) {
                return n;
            }
        }
        try {
            if (string2.contains(".")) {
                return (int)Math.round(Double.parseDouble(string2));
            }
            int n2 = Integer.parseInt(string2);
            if (bl) {
                return n2;
            }
            return n2 * 4;
        }
        catch (NumberFormatException numberFormatException) {
            return n;
        }
    }

    private static Integer parseAlpha(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        try {
            int n = Integer.parseInt(string.trim());
            n = Math.max(0, Math.min(100, n));
            return (int)Math.round((double)n / 100.0 * 255.0);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private static Color parseColor(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = string.trim().toLowerCase();
        if (string2.startsWith("[") && string2.endsWith("]") && string2.length() > 2) {
            string2 = string2.substring(1, string2.length() - 1).trim();
        }
        if ("transparent".equals(string2)) {
            return new Color(0, 0, 0, 0);
        }
        if (string2.startsWith("#")) {
            return TailwindParser.parseHexColor(string2);
        }
        if (string2.startsWith("rgb(")) {
            return TailwindParser.parseRgbColor(string2, false);
        }
        if (string2.startsWith("rgba(")) {
            return TailwindParser.parseRgbColor(string2, true);
        }
        return COLOR_MAP.getOrDefault(string2, null);
    }

    private static Color parseHexColor(String string) {
        Object object = string.substring(1);
        if (((String)object).length() == 3) {
            object = "" + ((String)object).charAt(0) + ((String)object).charAt(0) + ((String)object).charAt(1) + ((String)object).charAt(1) + ((String)object).charAt(2) + ((String)object).charAt(2);
        }
        if (((String)object).length() != 6) {
            return null;
        }
        try {
            int n = Integer.parseInt((String)object, 16);
            return new Color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private static Color parseRgbColor(String string, boolean bl) {
        try {
            String string2 = string.substring(string.indexOf(40) + 1, string.lastIndexOf(41));
            String[] stringArray = string2.split(",");
            if (stringArray.length < 3) {
                return null;
            }
            int n = TailwindParser.clamp255((int)Math.round(Double.parseDouble(stringArray[0].trim())));
            int n2 = TailwindParser.clamp255((int)Math.round(Double.parseDouble(stringArray[1].trim())));
            int n3 = TailwindParser.clamp255((int)Math.round(Double.parseDouble(stringArray[2].trim())));
            if (!bl || stringArray.length < 4) {
                return new Color(n, n2, n3);
            }
            double d = Double.parseDouble(stringArray[3].trim());
            int n4 = TailwindParser.clamp255((int)Math.round(d <= 1.0 ? d * 255.0 : d));
            return new Color(n, n2, n3, n4);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static int clamp255(int n) {
        return Math.max(0, Math.min(255, n));
    }

    private static Map<String, Color> buildColorMap() {
        HashMap<String, Color> hashMap = new HashMap<String, Color>();
        hashMap.put("black", new Color(0, 0, 0));
        hashMap.put("white", new Color(255, 255, 255));
        hashMap.put("gray-50", new Color(249, 250, 251));
        hashMap.put("gray-100", new Color(243, 244, 246));
        hashMap.put("gray-200", new Color(229, 231, 235));
        hashMap.put("gray-400", new Color(156, 163, 175));
        hashMap.put("gray-600", new Color(75, 85, 99));
        hashMap.put("gray-800", new Color(31, 41, 55));
        hashMap.put("slate-700", new Color(51, 65, 85));
        hashMap.put("slate-900", new Color(15, 23, 42));
        hashMap.put("blue-400", new Color(96, 165, 250));
        hashMap.put("blue-500", new Color(59, 130, 246));
        hashMap.put("blue-600", new Color(37, 99, 235));
        hashMap.put("green-400", new Color(74, 222, 128));
        hashMap.put("green-500", new Color(34, 197, 94));
        hashMap.put("red-400", new Color(248, 113, 113));
        hashMap.put("red-500", new Color(239, 68, 68));
        hashMap.put("yellow-400", new Color(250, 204, 21));
        hashMap.put("purple-500", new Color(168, 85, 247));
        hashMap.put("cyan-500", new Color(6, 182, 212));
        hashMap.put("amber-500", new Color(245, 158, 11));
        return hashMap;
    }
}

