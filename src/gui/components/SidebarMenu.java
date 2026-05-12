package gui.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import components.Button;
import components.Label;
import main.BaseComp;

public class SidebarMenu extends SurfaceCard {
    public static class Item {
        private final String key;
        private final String label;

        public Item(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

    private final CachedImageComp logo;
    private final Label appName;
    private final Label roleLabel;
    private final BaseComp avatarComp;
    private final String initials;
    private final Label userNameLabel;
    private final List<Button> itemButtons;
    private final Map<String, Button> byKey;
    private final Button logoutButton;
    private final Button themeSwitchButton;
    private String activeKey;
    private boolean darkMode;

    public SidebarMenu(String roleLabel, String userName, List<Item> items, String initialActiveKey, Consumer<String> onSelect,
            Runnable onLogout, Runnable onThemeToggle) {
        super(0, 0, 220, 100, new Color(251, 252, 254), new Color(226, 230, 238), 0);
        this.activeKey = initialActiveKey;
        this.itemButtons = new ArrayList<>();
        this.byKey = new HashMap<>();
        this.darkMode = false;

        this.logo = new CachedImageComp("assets/logo-esigelec.png", 14, 12, 96, 36);

        this.appName = new Label("Esigelec", 56, 10, 120, 22);
        this.appName.setFont(new Font("Dialog", Font.BOLD, 18));
        this.appName.setColor(new Color(26, 34, 49));

        this.roleLabel = new Label(roleLabel == null ? "" : roleLabel, 56, 30, 120, 16);
        this.roleLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        this.roleLabel.setColor(new Color(119, 128, 143));

        String resolvedName = userName == null ? "" : userName.trim();
        this.initials = buildInitials(resolvedName);
        this.avatarComp = new BaseComp(null) {
            @Override
            public void customGraphics(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(111, 76, 242));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Dialog", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(initials);
                int tx = Math.max(0, (getWidth() - tw) / 2);
                int ty = Math.max(11, (getHeight() / 2) + 4);
                g2.drawString(initials, tx, ty);
            }
        };

        this.userNameLabel = new Label(truncateName(resolvedName), 14, 52, 180, 16);
        this.userNameLabel.setFont(new Font("Dialog", Font.BOLD, 11));
        this.userNameLabel.setColor(new Color(88, 97, 114));

        addChild(logo);
        addChild(appName);
        addChild(this.roleLabel);
        addChild(this.avatarComp);
        addChild(this.userNameLabel);

        if (items != null) {
            for (Item item : items) {
                Button b = new Button(item.label, 14, 0, 190, 34, () -> {
                    setActiveKey(item.key);
                    if (onSelect != null) {
                        onSelect.accept(item.key);
                    }
                });
                b.setForeground(new Color(47, 57, 74));
                b.setBackground(new Color(239, 242, 248));
                addChild(b);
                itemButtons.add(b);
                byKey.put(item.key, b);
            }
        }

        this.logoutButton = new Button("Deconnexion", 14, 0, 190, 34, onLogout);
        this.logoutButton.setForeground(new Color(75, 85, 104));
        this.logoutButton.setBackground(new Color(244, 246, 250));
        addChild(logoutButton);

        this.themeSwitchButton = new Button("Mode clair/sombre", 14, 0, 190, 34, onThemeToggle);
        this.themeSwitchButton.setForeground(new Color(75, 85, 104));
        this.themeSwitchButton.setBackground(new Color(244, 246, 250));
        addChild(themeSwitchButton);

        setActiveKey(initialActiveKey);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        // During superclass construction, setBounds can be invoked before SidebarMenu fields are initialized.
        if (logo == null || appName == null || roleLabel == null || avatarComp == null || userNameLabel == null
                || itemButtons == null || logoutButton == null || themeSwitchButton == null) {
            return;
        }

        logo.setBounds(14, 14, 28, 28);
        appName.setBounds(50, 12, width - 62, 20);
        roleLabel.setBounds(50, 30, width - 62, 16);
        avatarComp.setBounds(14, 50, 20, 20);
        userNameLabel.setBounds(40, 52, width - 54, 16);

        int yCursor = 80;
        for (Button button : itemButtons) {
            button.setBounds(14, yCursor, width - 28, 34);
            yCursor += 44;
        }

        themeSwitchButton.setBounds(14, Math.max(14, height - 90), width - 28, 32);
        logoutButton.setBounds(14, Math.max(14, height - 46), width - 28, 32);
    }

    public void setActiveKey(String activeKey) {
        this.activeKey = activeKey;
        for (Map.Entry<String, Button> entry : byKey.entrySet()) {
            boolean active = entry.getKey().equals(this.activeKey);
            Button b = entry.getValue();
            if (darkMode) {
                b.setBackground(active ? new Color(52, 63, 94) : new Color(26, 34, 49));
                b.setForeground(active ? new Color(196, 213, 255) : new Color(211, 222, 244));
            } else {
                b.setBackground(active ? new Color(236, 232, 255) : new Color(244, 246, 250));
                b.setForeground(active ? new Color(103, 64, 219) : new Color(62, 72, 91));
            }
        }
        invalidate();
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (dark) {
            setBackground(new Color(18, 24, 35));
            appName.setColor(new Color(235, 241, 255));
            roleLabel.setColor(new Color(145, 160, 187));
            userNameLabel.setColor(new Color(216, 225, 243));

            for (Button b : itemButtons) {
                b.setBackground(new Color(26, 34, 49));
                b.setForeground(new Color(211, 222, 244));
            }
            themeSwitchButton.setBackground(new Color(34, 44, 62));
            themeSwitchButton.setForeground(new Color(211, 222, 244));
            logoutButton.setBackground(new Color(34, 44, 62));
            logoutButton.setForeground(new Color(211, 222, 244));
        } else {
            setBackground(new Color(251, 252, 254));
            appName.setColor(new Color(26, 34, 49));
            roleLabel.setColor(new Color(119, 128, 143));
            userNameLabel.setColor(new Color(88, 97, 114));

            for (Button b : itemButtons) {
                b.setBackground(new Color(244, 246, 250));
                b.setForeground(new Color(62, 72, 91));
            }
            themeSwitchButton.setBackground(new Color(244, 246, 250));
            themeSwitchButton.setForeground(new Color(75, 85, 104));
            logoutButton.setBackground(new Color(244, 246, 250));
            logoutButton.setForeground(new Color(75, 85, 104));
        }
        setActiveKey(activeKey);
    }

    private String truncateName(String name) {
        if (name == null || name.isBlank()) {
            return "Utilisateur";
        }
        String trimmed = name.trim();
        if (trimmed.length() <= 22) {
            return trimmed;
        }
        return trimmed.substring(0, 19) + "...";
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        String first = parts[0].isEmpty() ? "" : parts[0].substring(0, 1);
        String last = parts[parts.length - 1].isEmpty() ? "" : parts[parts.length - 1].substring(0, 1);
        String combined = (first + last).toUpperCase();
        return combined.isBlank() ? "U" : combined;
    }
}
