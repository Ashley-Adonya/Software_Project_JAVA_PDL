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

/**
 * Custom UI component that loads, caches, and renders images from local file paths or URLs.
 * Extends BaseComp to integrate with the custom rendering pipeline.
 * <p>
 * Images are loaded asynchronously on a dedicated daemon thread to avoid blocking the UI.
 * A static in-memory cache (ConcurrentHashMap) ensures that the same image source is
 * only loaded once and reused across all CachedImageComp instances.
 * <p>
 * Key features:
 * - Async image loading with a single-threaded executor
 * - Static image cache shared across all instances
 * - Support for local file paths (with multi-directory fallback resolution) and web URLs
 * - Alpha transparency control for fade effects
 * - Alt text rendering when image is unavailable or still loading
 * - Text wrapping for alt text within component bounds
 * - Graceful degradation: empty placeholder with rounded corners on load failure
 */
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

    /**
     * Creates a cached image component with the given source, position, and size.
     * Uses the default alt text "Image indisponible" when the image is unavailable.
     *
     * @param source the image source path (local file path or URL)
     * @param x      the x-coordinate of the component bounds
     * @param y      the y-coordinate of the component bounds
     * @param width  the width of the component in pixels
     * @param height the height of the component in pixels
     */
    public CachedImageComp(String source, int x, int y, int width, int height) {
        this(source, x, y, width, height, "Image indisponible");
    }

    /**
     * Creates a cached image component with the given source, position, size, and alt text.
     * The image is loaded asynchronously from the provided source. If the source is already
     * cached from a previous load, it is used immediately.
     *
     * @param source the image source path (local file path or URL)
     * @param x      the x-coordinate of the component bounds
     * @param y      the y-coordinate of the component bounds
     * @param width  the width of the component in pixels
     * @param height the height of the component in pixels
     * @param altText the alternative text displayed when the image cannot be loaded
     */
    public CachedImageComp(String source, int x, int y, int width, int height, String altText) {
        super(null);
        this.altText = altText == null ? "Image indisponible" : altText;
        setBounds(x, y, width, height);
        setImageSource(source);
    }

    /**
     * Sets the image source to a local file path.
     * Delegates to {@link #setImageSource(String)} for loading and caching.
     *
     * @param path the local file path to the image
     */
    public void setImagePath(String path) {
        setImageSource(path);
    }

    /**
     * Sets the image source to a URL.
     * Delegates to {@link #setImageSource(String)} for loading and caching.
     *
     * @param url the web URL of the image (http/https)
     */
    public void setImageUrl(String url) {
        setImageSource(url);
    }

    /**
     * Sets the image source to the given path or URL and initiates async loading.
     * If the source is already in the cache, the cached image is used immediately.
     * Otherwise, an async load is triggered via the shared single-threaded executor.
     *
     * @param source the image source (local file path or http/https URL)
     */
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

    /**
     * Sets the alternative text displayed when the image cannot be loaded.
     * If the text is null or blank, a default "Image indisponible" is used.
     *
     * @param text the alt text to display; null or blank resets to default
     */
    public void setAltText(String text) {
        this.altText = text == null || text.isBlank() ? "Image indisponible" : text;
        invalidate();
    }

    /**
     * Returns the current image source path or URL.
     *
     * @return the source string as passed to the constructor or setImageSource
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Returns the current alternative text.
     *
     * @return the alt text string, never null (defaults to "Image indisponible")
     */
    public String getAltText() {
        return this.altText;
    }

    /**
     * Sets the alpha transparency level for the rendered image.
     * Values outside the [0.0, 1.0] range are clamped.
     * A value of 1.0 is fully opaque, 0.0 is fully transparent.
     *
     * @param alpha the alpha value between 0.0 (transparent) and 1.0 (opaque)
     */
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        invalidate();
    }

    /**
     * Renders the component with custom graphics. If the image is loaded and cached,
     * it is drawn with bilinear interpolation and the configured alpha transparency.
     * If no image is available or loading is pending, a rounded rectangle placeholder
     * is drawn with the alt text wrapped to fit within the component bounds.
     * Before rendering, a final cache check is performed in case the async load
     * completed just before this render call.
     *
     * @param graphics the Graphics context to render into; cast to Graphics2D for rendering hints
     */
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
