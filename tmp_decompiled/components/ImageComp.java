/*
 * Decompiled with CFR 0.152.
 */
package components;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import main.BaseComp;

public class ImageComp
extends BaseComp {
    private Image image;
    private String altText;
    private String source;
    private float alpha = 1.0f;

    public ImageComp(String string, int n, int n2, int n3, int n4) {
        this(string, n, n2, n3, n4, "Image indisponible");
    }

    public ImageComp(String string, int n, int n2, int n3, int n4, String string2) {
        super(null);
        this.altText = string2 == null ? "Image indisponible" : string2;
        this.setBounds(n, n2, n3, n4);
        this.setImageSource(string);
    }

    private void loadFromPath(String string) {
        if (string == null || string.isBlank()) {
            this.image = null;
            return;
        }
        try {
            this.image = ImageIO.read(new File(string));
        }
        catch (IOException iOException) {
            this.image = null;
        }
    }

    private void loadFromUrl(String string) {
        if (string == null || string.isBlank()) {
            this.image = null;
            return;
        }
        try {
            this.image = ImageIO.read(new URL(string));
        }
        catch (IOException iOException) {
            this.image = null;
        }
    }

    private boolean isWebUrl(String string) {
        if (string == null) {
            return false;
        }
        try {
            URL uRL = new URL(string);
            String string2 = uRL.getProtocol();
            return "http".equalsIgnoreCase(string2) || "https".equalsIgnoreCase(string2);
        }
        catch (MalformedURLException malformedURLException) {
            return false;
        }
    }

    public void setImagePath(String string) {
        this.source = string;
        this.loadFromPath(string);
        this.invalidate();
    }

    public void setImageUrl(String string) {
        this.source = string;
        this.loadFromUrl(string);
        this.invalidate();
    }

    public void setImageSource(String string) {
        this.source = string;
        if (this.isWebUrl(string)) {
            this.loadFromUrl(string);
        } else {
            this.loadFromPath(string);
        }
        this.invalidate();
    }

    public void setAltText(String string) {
        this.altText = string == null || string.isBlank() ? "Image indisponible" : string;
        this.invalidate();
    }

    public String getSource() {
        return this.source;
    }

    public String getAltText() {
        return this.altText;
    }

    public void setAlpha(float f) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, f));
        this.invalidate();
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(new Color(236, 238, 242));
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 14, 14);
        if (this.image != null) {
            Composite composite = graphics2D.getComposite();
            graphics2D.setComposite(AlphaComposite.getInstance(3, this.alpha));
            graphics2D.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), null);
            graphics2D.setComposite(composite);
        } else {
            graphics2D.setColor(new Color(138, 144, 153));
            String string = this.altText == null || this.altText.isBlank() ? "Image indisponible" : this.altText;
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            int n = Math.max(20, this.getWidth() - 24);
            int n2 = 22;
            for (String string2 : this.wrapLines(string, fontMetrics, n)) {
                graphics2D.drawString(string2, 12, n2);
                if ((n2 += fontMetrics.getHeight()) <= this.getHeight() - 8) continue;
                break;
            }
        }
    }

    private List<String> wrapLines(String string, FontMetrics fontMetrics, int n) {
        String[] stringArray;
        ArrayList<String> arrayList = new ArrayList<String>();
        if (string == null || string.isBlank()) {
            arrayList.add("Image indisponible");
            return arrayList;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : stringArray = string.split("\\s+")) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append(string2);
                continue;
            }
            String string3 = String.valueOf(stringBuilder) + " " + string2;
            if (fontMetrics.stringWidth(string3) <= n) {
                stringBuilder.append(' ').append(string2);
                continue;
            }
            arrayList.add(stringBuilder.toString());
            stringBuilder = new StringBuilder(string2);
        }
        if (stringBuilder.length() > 0) {
            arrayList.add(stringBuilder.toString());
        }
        return arrayList;
    }
}

