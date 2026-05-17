package gui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import main.BaseComp;

public class CachedImageComp extends BaseComp {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();
    private static final ExecutorService LOADER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });

    private Image image;
    private String altText;
    private String source;
    private float alpha = 1.0f;

    public CachedImageComp(String source, int x, int y, int width, int height) {
        this(source, x, y, width, height, "Image indisponible");
    }

    public CachedImageComp(String source, int x, int y, int width, int height, String altText) {
        super(null);
        this.altText = altText == null ? "Image indisponible" : altText;
        setBounds(x, y, width, height);
        setImageSource(source);
    }

    public void setImagePath(String path) {
        setImageSource(path);
    }

    public void setImageUrl(String url) {
        setImageSource(url);
    }

    public void setImageSource(String source) {
        this.source = source;
        Image cached = CACHE.get(source);
        if (cached != null) {
            this.image = cached;
            invalidate();
            return;
        }
        this.image = null;
        loadAsync(source);
        invalidate();
    }

    public void setAltText(String text) {
        this.altText = text == null || text.isBlank() ? "Image indisponible" : text;
        invalidate();
    }

    public String getSource() {
        return this.source;
    }

    public String getAltText() {
        return this.altText;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        invalidate();
    }

    @Override
    public void customGraphics(Graphics graphics) {
        if (this.image == null && this.source != null) {
            Image cached = CACHE.get(this.source);
            if (cached != null) {
                this.image = cached;
            }
        }

        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(new Color(236, 238, 242));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

        if (this.image != null) {
            Composite composite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));
            g2.drawImage(this.image, 0, 0, getWidth(), getHeight(), null);
            g2.setComposite(composite);
            return;
        }

        g2.setColor(new Color(138, 144, 153));
        String msg = this.altText == null || this.altText.isBlank() ? "Image indisponible" : this.altText;
        FontMetrics fm = g2.getFontMetrics();
        int maxWidth = Math.max(20, getWidth() - 24);
        int y = 22;
        for (String line : wrapLines(msg, fm, maxWidth)) {
            g2.drawString(line, 12, y);
            y += fm.getHeight();
            if (y > getHeight() - 8) {
                break;
            }
        }
    }

    private void loadAsync(String source) {
        if (source == null || source.isBlank()) {
            return;
        }
        if (LOADING.putIfAbsent(source, Boolean.TRUE) != null) {
            return;
        }
        LOADER.submit(() -> {
            Image loaded = loadImage(source);
            if (loaded != null) {
                CACHE.put(source, loaded);
            }
            LOADING.remove(source);
            if (source.equals(this.source)) {
                SwingUtilities.invokeLater(() -> {
                    this.image = loaded;
                    invalidate();
                });
            }
        });
    }

    private Image loadImage(String source) {
        if (source == null || source.isBlank()) return null;
        if (source.startsWith("http://") || source.startsWith("https://")) {
            try { return javax.imageio.ImageIO.read(new java.net.URL(source)); } catch (Exception e) { return null; }
        }
        String path = source.trim();
        if (path.startsWith("./") || path.startsWith(".\\")) path = path.substring(2);
        path = path.replace('\\', '/');
        String userDir = System.getProperty("user.dir");
        File[] candidates = {
            new File(userDir, path),
            new File(new File(userDir), "assets/" + new File(path).getName()),
            new File(new File(userDir), "bin/" + new File(path).getName()),
            new File(userDir + File.separator + "assets", new File(path).getName()),
            new File(path),
            new File("./" + path)
        };
        for (File f : candidates) {
            if (f.exists() && f.isFile() && f.canRead()) {
                try { return javax.imageio.ImageIO.read(f); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private boolean isWebUrl(String source) {
        if (source == null) {
            return false;
        }
        try {
            URL url = URI.create(source).toURL();
            String protocol = url.getProtocol();
            return "http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private List<String> wrapLines(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("Image indisponible");
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }
            String next = current + " " + word;
            if (fm.stringWidth(next) <= maxWidth) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
