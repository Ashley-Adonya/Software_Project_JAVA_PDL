/*
 * Decompiled with CFR 0.152.
 */
package components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import main.BaseComp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SvgFromStringComp
extends BaseComp {
    private final List<Primitive> primitives = new ArrayList<Primitive>();
    private String svgSource;
    private String errorMessage;
    private double viewWidth;
    private double viewHeight;

    public SvgFromStringComp(String string, int n, int n2, int n3, int n4) {
        super(null);
        this.svgSource = string == null ? "" : string;
        this.errorMessage = null;
        this.viewWidth = 100.0;
        this.viewHeight = 100.0;
        this.setBounds(n, n2, n3, n4);
        this.parse();
    }

    public void setSvgSource(String string) {
        this.svgSource = string == null ? "" : string;
        this.parse();
        this.invalidate();
    }

    public String getSvgSource() {
        return this.svgSource;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public void customGraphics(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(new Color(245, 247, 250));
        graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
        if (this.errorMessage != null) {
            graphics2D.setColor(new Color(201, 74, 74));
            graphics2D.drawString("SVG error", 12, 20);
            graphics2D.setColor(new Color(120, 128, 138));
            graphics2D.drawString(this.errorMessage, 12, 38);
            return;
        }
        double d = (double)this.getWidth() / Math.max(1.0, this.viewWidth);
        double d2 = (double)this.getHeight() / Math.max(1.0, this.viewHeight);
        Graphics2D graphics2D2 = (Graphics2D)graphics2D.create();
        graphics2D2.scale(d, d2);
        for (Primitive primitive : this.primitives) {
            switch (primitive.type) {
                case "rect": {
                    this.drawRect(graphics2D2, primitive);
                    break;
                }
                case "circle": {
                    this.drawCircle(graphics2D2, primitive);
                    break;
                }
                case "ellipse": {
                    this.drawEllipse(graphics2D2, primitive);
                    break;
                }
                case "line": {
                    this.drawLine(graphics2D2, primitive);
                    break;
                }
                case "polyline": {
                    this.drawPolyline(graphics2D2, primitive, false);
                    break;
                }
                case "polygon": {
                    this.drawPolyline(graphics2D2, primitive, true);
                    break;
                }
            }
        }
        graphics2D2.dispose();
    }

    private void drawRect(Graphics2D graphics2D, Primitive primitive) {
        Rectangle2D.Double double_ = new Rectangle2D.Double(primitive.values[0], primitive.values[1], primitive.values[2], primitive.values[3]);
        if (primitive.fill.getAlpha() > 0) {
            graphics2D.setColor(primitive.fill);
            graphics2D.fill(double_);
        }
        if (primitive.stroke.getAlpha() > 0 && primitive.strokeWidth > 0.0f) {
            graphics2D.setColor(primitive.stroke);
            graphics2D.setStroke(new BasicStroke(primitive.strokeWidth));
            graphics2D.draw(double_);
        }
    }

    private void drawCircle(Graphics2D graphics2D, Primitive primitive) {
        double d = primitive.values[0];
        double d2 = primitive.values[1];
        double d3 = primitive.values[2];
        Ellipse2D.Double double_ = new Ellipse2D.Double(d - d3, d2 - d3, d3 * 2.0, d3 * 2.0);
        if (primitive.fill.getAlpha() > 0) {
            graphics2D.setColor(primitive.fill);
            graphics2D.fill(double_);
        }
        if (primitive.stroke.getAlpha() > 0 && primitive.strokeWidth > 0.0f) {
            graphics2D.setColor(primitive.stroke);
            graphics2D.setStroke(new BasicStroke(primitive.strokeWidth));
            graphics2D.draw(double_);
        }
    }

    private void drawEllipse(Graphics2D graphics2D, Primitive primitive) {
        double d = primitive.values[0];
        double d2 = primitive.values[1];
        double d3 = primitive.values[2];
        double d4 = primitive.values[3];
        Ellipse2D.Double double_ = new Ellipse2D.Double(d - d3, d2 - d4, d3 * 2.0, d4 * 2.0);
        if (primitive.fill.getAlpha() > 0) {
            graphics2D.setColor(primitive.fill);
            graphics2D.fill(double_);
        }
        if (primitive.stroke.getAlpha() > 0 && primitive.strokeWidth > 0.0f) {
            graphics2D.setColor(primitive.stroke);
            graphics2D.setStroke(new BasicStroke(primitive.strokeWidth));
            graphics2D.draw(double_);
        }
    }

    private void drawLine(Graphics2D graphics2D, Primitive primitive) {
        if (primitive.stroke.getAlpha() == 0 || primitive.strokeWidth <= 0.0f) {
            return;
        }
        graphics2D.setColor(primitive.stroke);
        graphics2D.setStroke(new BasicStroke(primitive.strokeWidth));
        graphics2D.draw(new Line2D.Double(primitive.values[0], primitive.values[1], primitive.values[2], primitive.values[3]));
    }

    private void drawPolyline(Graphics2D graphics2D, Primitive primitive, boolean bl) {
        double[] dArray = this.parsePoints(primitive.points);
        if (dArray.length < 4) {
            return;
        }
        Path2D.Double double_ = new Path2D.Double();
        ((Path2D)double_).moveTo(dArray[0], dArray[1]);
        int n = 2;
        while (n + 1 < dArray.length) {
            ((Path2D)double_).lineTo(dArray[n], dArray[n + 1]);
            n += 2;
        }
        if (bl) {
            double_.closePath();
        }
        if (bl && primitive.fill.getAlpha() > 0) {
            graphics2D.setColor(primitive.fill);
            graphics2D.fill(double_);
        }
        if (primitive.stroke.getAlpha() > 0 && primitive.strokeWidth > 0.0f) {
            graphics2D.setColor(primitive.stroke);
            graphics2D.setStroke(new BasicStroke(primitive.strokeWidth));
            graphics2D.draw(double_);
        }
    }

    private double[] parsePoints(String string) {
        if (string == null || string.isBlank()) {
            return new double[0];
        }
        String string2 = string.replace(',', ' ').trim();
        String[] stringArray = string2.split("\\s+");
        double[] dArray = new double[stringArray.length];
        int n = 0;
        for (String string3 : stringArray) {
            try {
                dArray[n++] = Double.parseDouble(string3);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        double[] dArray2 = new double[n];
        System.arraycopy(dArray, 0, dArray2, 0, n);
        return dArray2;
    }

    private void parse() {
        this.primitives.clear();
        this.errorMessage = null;
        this.viewWidth = 100.0;
        this.viewHeight = 100.0;
        if (this.svgSource == null || this.svgSource.isBlank()) {
            this.errorMessage = "empty svg";
            return;
        }
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(this.svgSource)));
            Element element = document.getDocumentElement();
            if (element == null || !"svg".equalsIgnoreCase(element.getTagName())) {
                this.errorMessage = "root <svg> missing";
                return;
            }
            this.readViewBox(element);
            this.collectPrimitives(element);
        }
        catch (Exception exception) {
            this.errorMessage = exception.getMessage() == null ? "parse failed" : exception.getMessage();
        }
    }

    private void readViewBox(Element element) {
        String[] stringArray;
        String string = element.getAttribute("viewBox");
        if (string != null && !string.isBlank() && (stringArray = string.trim().split("\\s+")).length == 4) {
            try {
                this.viewWidth = Double.parseDouble(stringArray[2]);
                this.viewHeight = Double.parseDouble(stringArray[3]);
                return;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        this.viewWidth = this.parseDouble(element.getAttribute("width"), 100.0);
        this.viewHeight = this.parseDouble(element.getAttribute("height"), 100.0);
    }

    private void collectPrimitives(Element element) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Primitive primitive;
            String string;
            Node node = nodeList.item(i);
            if (!(node instanceof Element)) continue;
            Element element2 = (Element)node;
            switch (string = element2.getTagName().toLowerCase()) {
                case "rect": 
                case "circle": 
                case "ellipse": 
                case "line": 
                case "polyline": 
                case "polygon": {
                    Primitive primitive2 = new Primitive(string);
                    break;
                }
                default: {
                    Primitive primitive2 = primitive = null;
                }
            }
            if (primitive != null) {
                this.fillPrimitiveAttributes(element2, primitive);
                this.primitives.add(primitive);
            }
            this.collectPrimitives(element2);
        }
    }

    private void fillPrimitiveAttributes(Element element, Primitive primitive) {
        primitive.fill = this.parseColor(element.getAttribute("fill"), new Color(0, 0, 0, 0));
        primitive.stroke = this.parseColor(element.getAttribute("stroke"), new Color(0, 0, 0, 0));
        primitive.strokeWidth = (float)this.parseDouble(element.getAttribute("stroke-width"), 1.0);
        switch (primitive.type) {
            case "rect": {
                primitive.values[0] = this.parseDouble(element.getAttribute("x"), 0.0);
                primitive.values[1] = this.parseDouble(element.getAttribute("y"), 0.0);
                primitive.values[2] = this.parseDouble(element.getAttribute("width"), 0.0);
                primitive.values[3] = this.parseDouble(element.getAttribute("height"), 0.0);
                break;
            }
            case "circle": {
                primitive.values[0] = this.parseDouble(element.getAttribute("cx"), 0.0);
                primitive.values[1] = this.parseDouble(element.getAttribute("cy"), 0.0);
                primitive.values[2] = this.parseDouble(element.getAttribute("r"), 0.0);
                break;
            }
            case "ellipse": {
                primitive.values[0] = this.parseDouble(element.getAttribute("cx"), 0.0);
                primitive.values[1] = this.parseDouble(element.getAttribute("cy"), 0.0);
                primitive.values[2] = this.parseDouble(element.getAttribute("rx"), 0.0);
                primitive.values[3] = this.parseDouble(element.getAttribute("ry"), 0.0);
                break;
            }
            case "line": {
                primitive.values[0] = this.parseDouble(element.getAttribute("x1"), 0.0);
                primitive.values[1] = this.parseDouble(element.getAttribute("y1"), 0.0);
                primitive.values[2] = this.parseDouble(element.getAttribute("x2"), 0.0);
                primitive.values[3] = this.parseDouble(element.getAttribute("y2"), 0.0);
                break;
            }
            case "polyline": 
            case "polygon": {
                primitive.points = element.getAttribute("points");
                break;
            }
        }
    }

    private double parseDouble(String string, double d) {
        if (string == null || string.isBlank()) {
            return d;
        }
        try {
            String string2 = string.trim().replace("px", "");
            return Double.parseDouble(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return d;
        }
    }

    private Color parseColor(String string, Color color) {
        if (string == null || string.isBlank()) {
            return color;
        }
        String string2 = string.trim().toLowerCase();
        if ("none".equals(string2)) {
            return new Color(0, 0, 0, 0);
        }
        if (string2.startsWith("#")) {
            return this.parseHexColor(string2, color);
        }
        if (string2.startsWith("rgb(")) {
            return this.parseRgbColor(string2, color);
        }
        return switch (string2) {
            case "black" -> new Color(0, 0, 0);
            case "white" -> new Color(255, 255, 255);
            case "red" -> new Color(239, 68, 68);
            case "green" -> new Color(34, 197, 94);
            case "blue" -> new Color(59, 130, 246);
            case "yellow" -> new Color(245, 158, 11);
            case "gray", "grey" -> new Color(148, 163, 184);
            default -> color;
        };
    }

    private Color parseHexColor(String string, Color color) {
        Object object = string.substring(1);
        if (((String)object).length() == 3) {
            object = "" + ((String)object).charAt(0) + ((String)object).charAt(0) + ((String)object).charAt(1) + ((String)object).charAt(1) + ((String)object).charAt(2) + ((String)object).charAt(2);
        }
        if (((String)object).length() != 6) {
            return color;
        }
        try {
            int n = Integer.parseInt((String)object, 16);
            return new Color(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF);
        }
        catch (NumberFormatException numberFormatException) {
            return color;
        }
    }

    private Color parseRgbColor(String string, Color color) {
        try {
            String string2 = string.substring(string.indexOf(40) + 1, string.lastIndexOf(41));
            String[] stringArray = string2.split(",");
            if (stringArray.length < 3) {
                return color;
            }
            int n = (int)this.parseDouble(stringArray[0], 0.0);
            int n2 = (int)this.parseDouble(stringArray[1], 0.0);
            int n3 = (int)this.parseDouble(stringArray[2], 0.0);
            return new Color(Math.max(0, Math.min(255, n)), Math.max(0, Math.min(255, n2)), Math.max(0, Math.min(255, n3)));
        }
        catch (Exception exception) {
            return color;
        }
    }

    private static class Primitive {
        String type;
        double[] values;
        String points;
        Color fill;
        Color stroke;
        float strokeWidth;

        Primitive(String string) {
            this.type = string;
            this.values = new double[8];
            this.points = "";
            this.fill = new Color(0, 0, 0, 0);
            this.stroke = new Color(0, 0, 0, 0);
            this.strokeWidth = 1.0f;
        }
    }
}

