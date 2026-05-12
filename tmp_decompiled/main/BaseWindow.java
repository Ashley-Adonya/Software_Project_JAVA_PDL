/*
 * Decompiled with CFR 0.152.
 */
package main;

import event.UiEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import main.BaseComp;
import style.StyleManager;
import utils.DirtyManager;
import utils.HitTester;

public class BaseWindow
extends BaseComp {
    private static final int DEFAULT_HEADER_HEIGHT = 38;
    private static final int FRAME_RADIUS = 18;
    private static final int RESIZE_BORDER = 8;
    private static final int MIN_WIDTH = 520;
    private static final int MIN_HEIGHT = 380;
    private static final Color OPAQUE_WINDOW_BG = new Color(244, 245, 247);
    private final JFrame frame;
    private final JPanel canvas;
    private final HitTester hitTester;
    private final DirtyManager dirtyManager;
    private final int fps;
    private final boolean transparentWindow;
    private BaseComp root;
    private BaseComp header;
    private BaseComp content;
    private BaseComp layerHost;
    private BaseComp capturedPointer;
    private BaseComp pressedSystemButton;
    private boolean windowDragActive;
    private int windowDragOffsetX;
    private int windowDragOffsetY;
    private ResizeEdge activeResizeEdge;
    private Point resizeStartMouse;
    private Rectangle resizeStartBounds;
    private final Map<BaseComp, Consumer<BaseWindow>> systemButtons;
    private final Map<BaseComp, String> systemButtonHints;
    private final List<Runnable> resizeListeners;
    private final List<BaseWindow> childWindows;
    private BaseComp closeButton;
    private BaseComp minimizeButton;
    private BaseComp maximizeButton;
    private BaseComp hoveredSystemButton;
    private BaseComp focusedComponent;
    private Timer activeRenderTimer;
    private boolean repaintScheduled;
    private boolean debugOverlayEnabled;
    private boolean debugEventOverlayEnabled;
    private long lastFrameEndNanos;
    private double smoothedFrameMs;
    private int lastDirtyCount;
    private int lastDirtyArea;
    private boolean lastFrameFullRedraw;
    private final LinkedList<String> debugEventLines;
    private BufferedImage backBuffer;
    private static final int MAX_DEBUG_EVENT_LINES = 12;
    private int currentCursorId = 0;

    public BaseWindow(String string, int n, int n2, int n3) {
        super(null);
        this.fps = Math.max(0, n3);
        this.transparentWindow = this.resolveTransparentWindowSetting();
        this.hitTester = new HitTester();
        this.dirtyManager = new DirtyManager();
        this.frame = new JFrame(string);
        this.canvas = this.createCanvas();
        this.activeResizeEdge = ResizeEdge.NONE;
        this.systemButtons = new HashMap<BaseComp, Consumer<BaseWindow>>();
        this.systemButtonHints = new HashMap<BaseComp, String>();
        this.resizeListeners = new CopyOnWriteArrayList<Runnable>();
        this.childWindows = new CopyOnWriteArrayList<BaseWindow>();
        this.hoveredSystemButton = null;
        this.repaintScheduled = false;
        this.debugOverlayEnabled = true;
        this.debugEventOverlayEnabled = false;
        this.lastFrameEndNanos = System.nanoTime();
        this.smoothedFrameMs = 0.0;
        this.lastDirtyCount = 0;
        this.lastDirtyArea = 0;
        this.lastFrameFullRedraw = true;
        this.focusedComponent = null;
        this.debugEventLines = new LinkedList();
        this.frame.setDefaultCloseOperation(2);
        this.frame.setUndecorated(true);
        this.frame.setBackground(this.transparentWindow ? new Color(0, 0, 0, 0) : OPAQUE_WINDOW_BG);
        this.frame.setMinimumSize(new Dimension(520, 380));
        this.frame.setSize(n, n2);
        this.frame.setContentPane(this.canvas);
        this.canvas.setOpaque(!this.transparentWindow);
        this.canvas.setBackground(this.transparentWindow ? new Color(0, 0, 0, 0) : OPAQUE_WINDOW_BG);
        this.canvas.setBorder(BorderFactory.createEmptyBorder());
        this.frame.setLocationRelativeTo(null);
        this.applyRoundedShape(n, n2);
        this.root = this.createDefaultRoot(n, n2);
        this.root.setOwnerWindow(this);
        this.dirtyManager.requestFullRedraw();
        this.installResizeHook();
        this.installWindowCloseHook();
        this.wireMouseEvents();
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.root != null) {
            this.root.paint(graphics);
        }
    }

    public void show() {
        this.frame.setVisible(true);
        this.canvas.requestFocusInWindow();
        this.startRenderLoopIfNeeded();
        this.requestRender();
    }

    public void focusComponent(BaseComp baseComp) {
        if (baseComp != null && !baseComp.isFocusable()) {
            return;
        }
        this.setFocusedComponent(baseComp);
        this.canvas.requestFocusInWindow();
    }

    public void dispose() {
        for (BaseWindow baseWindow : this.childWindows) {
            if (baseWindow == null) continue;
            baseWindow.dispose();
        }
        this.childWindows.clear();
        if (this.activeRenderTimer != null) {
            this.activeRenderTimer.stop();
        }
        this.frame.dispose();
    }

    public JFrame getNativeFrame() {
        return this.frame;
    }

    public BaseComp getRoot() {
        return this.root;
    }

    public BaseComp getHeader() {
        return this.header;
    }

    public BaseComp getContent() {
        return this.content;
    }

    public BaseComp getLayerHost() {
        return this.layerHost;
    }

    public void addResizeListener(Runnable runnable) {
        if (runnable != null) {
            this.resizeListeners.add(runnable);
        }
    }

    public BaseWindow openChildWindow(String string, int n, int n2, int n3) {
        BaseWindow baseWindow = new BaseWindow(string, n, n2, n3);
        this.childWindows.add(baseWindow);
        baseWindow.show();
        return baseWindow;
    }

    public BaseComp openLayer(BaseComp baseComp) {
        if (baseComp == null || this.layerHost == null) {
            return null;
        }
        this.layerHost.addChild(baseComp);
        this.requestRender();
        return baseComp;
    }

    public void closeTopLayer() {
        if (this.layerHost == null || this.layerHost.getChildrenList().isEmpty()) {
            return;
        }
        int n = this.layerHost.getChildrenList().size() - 1;
        BaseComp baseComp = this.layerHost.getChildrenList().get(n);
        this.layerHost.removeChild(baseComp);
        this.requestRender();
    }

    public BaseComp openModal(BaseComp baseComp) {
        if (this.content == null || this.layerHost == null) {
            return null;
        }
        BaseComp baseComp3 = new BaseComp(this, null){

            @Override
            public void customGraphics(Graphics graphics) {
                graphics.setColor(new Color(20, 24, 31, 120));
                graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
        };
        baseComp3.setBounds(0, 0, this.content.getWidth(), this.content.getHeight());
        baseComp3.setDraggable(false);
        baseComp3.getEventManager().register(UiEvent.Type.POINTER_UP, (baseComp2, uiEvent) -> {
            if (uiEvent.getTarget() == baseComp3) {
                this.closeTopLayer();
                uiEvent.stopPropagation();
            }
        });
        if (baseComp != null) {
            int n = Math.max(12, (this.content.getWidth() - baseComp.getWidth()) / 2);
            int n2 = Math.max(12, (this.content.getHeight() - baseComp.getHeight()) / 2);
            baseComp.setBounds(n, n2, baseComp.getWidth(), baseComp.getHeight());
            baseComp3.addChild(baseComp);
        }
        this.layerHost.addChild(baseComp3);
        this.requestRender();
        return baseComp3;
    }

    public void setRoot(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        this.root = baseComp;
        this.root.setOwnerWindow(this);
        this.root.setBounds(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.relayoutTree();
        this.requestRender();
    }

    public void requestRender() {
        this.dirtyManager.requestFullRedraw();
        this.requestRenderIfNeeded();
    }

    public void requestRenderIfNeeded() {
        if (SwingUtilities.isEventDispatchThread()) {
            this.scheduleRepaint();
            return;
        }
        SwingUtilities.invokeLater(this::scheduleRepaint);
    }

    public void invalidateAll() {
        this.dirtyManager.requestFullRedraw();
    }

    public void invalidateComponent(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        this.invalidateRect(baseComp.getGlobalBounds());
    }

    public void invalidateRect(Rectangle rectangle) {
        if (rectangle == null || rectangle.width <= 0 || rectangle.height <= 0) {
            return;
        }
        this.dirtyManager.addDirtyRegion(rectangle);
    }

    private void scheduleRepaint() {
        if (this.repaintScheduled) {
            return;
        }
        this.repaintScheduled = true;
        SwingUtilities.invokeLater(() -> {
            this.repaintScheduled = false;
            this.canvas.repaint();
        });
    }

    public int getFps() {
        return this.fps;
    }

    public void setDebugOverlayEnabled(boolean bl) {
        this.debugOverlayEnabled = bl;
        this.requestRenderIfNeeded();
    }

    public void setDebugEventOverlayEnabled(boolean bl) {
        this.debugEventOverlayEnabled = bl;
        this.requestRenderIfNeeded();
    }

    public void capturePointer(BaseComp baseComp) {
        this.capturedPointer = baseComp;
    }

    public void releasePointer(BaseComp baseComp) {
        if (baseComp == null || this.capturedPointer == baseComp) {
            this.capturedPointer = null;
        }
    }

    private JPanel createCanvas() {
        JPanel jPanel = new JPanel(){

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D;
                boolean bl;
                int n;
                super.paintComponent(graphics);
                int n2 = this.getWidth();
                int n3 = this.getHeight();
                if (n2 <= 0 || n3 <= 0) {
                    return;
                }
                boolean bl2 = false;
                if (BaseWindow.this.backBuffer == null || BaseWindow.this.backBuffer.getWidth() != n2 || BaseWindow.this.backBuffer.getHeight() != n3) {
                    n = BaseWindow.this.transparentWindow ? 2 : 1;
                    BaseWindow.this.backBuffer = new BufferedImage(n2, n3, n);
                    bl2 = true;
                }
                n = BaseWindow.this.dirtyManager.hasDirtyRegion() ? 1 : 0;
                boolean bl3 = bl = bl2 || BaseWindow.this.fps > 0 || n != 0;
                if (bl) {
                    boolean bl4;
                    graphics2D = BaseWindow.this.backBuffer.createGraphics();
                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    RectangularShape rectangularShape = BaseWindow.this.transparentWindow ? new RoundRectangle2D.Double(0.0, 0.0, n2, n3, 18.0, 18.0) : new Rectangle(0, 0, n2, n3);
                    BaseWindow.this.lastFrameFullRedraw = bl4 = bl2 || BaseWindow.this.fps > 0 || BaseWindow.this.dirtyManager.shouldFallbackToFullRedraw(n2, n3);
                    BaseWindow.this.lastDirtyCount = BaseWindow.this.dirtyManager.getDirtyRegionCount();
                    BaseWindow.this.lastDirtyArea = BaseWindow.this.dirtyManager.getEstimatedDirtyArea();
                    if (bl4) {
                        graphics2D.setClip(rectangularShape);
                        BaseWindow.this.clearBufferRegion(graphics2D, 0, 0, n2, n3);
                        if (BaseWindow.this.root != null) {
                            BaseWindow.this.root.render(graphics2D);
                        }
                    } else {
                        List<Rectangle> list = BaseWindow.this.dirtyManager.getDirtyRegions();
                        for (Rectangle rectangle : list) {
                            Rectangle rectangle2 = rectangle.intersection(new Rectangle(0, 0, n2, n3));
                            if (rectangle2.isEmpty()) continue;
                            graphics2D.setClip(rectangle2);
                            BaseWindow.this.clearBufferRegion(graphics2D, rectangle2.x, rectangle2.y, rectangle2.width, rectangle2.height);
                            if (BaseWindow.this.root == null) continue;
                            BaseWindow.this.root.render(graphics2D);
                        }
                    }
                    BaseWindow.this.dirtyManager.clear();
                    graphics2D.dispose();
                }
                graphics2D = (Graphics2D)graphics.create();
                graphics2D.setComposite(AlphaComposite.SrcOver);
                graphics2D.drawImage((Image)BaseWindow.this.backBuffer, 0, 0, null);
                if (BaseWindow.this.debugOverlayEnabled) {
                    BaseWindow.this.drawDebugOverlay(graphics2D, n2, n3);
                }
                if (BaseWindow.this.debugEventOverlayEnabled) {
                    BaseWindow.this.drawEventDebugOverlay(graphics2D, n2, n3);
                }
                BaseWindow.this.drawWindowControlHint(graphics2D, n2, n3);
                graphics2D.dispose();
                if (BaseWindow.this.transparentWindow) {
                    Toolkit.getDefaultToolkit().sync();
                }
                long l = System.nanoTime();
                double d = (double)(l - BaseWindow.this.lastFrameEndNanos) / 1000000.0;
                BaseWindow.this.lastFrameEndNanos = l;
                BaseWindow.this.smoothedFrameMs = BaseWindow.this.smoothedFrameMs == 0.0 ? d : BaseWindow.this.smoothedFrameMs * 0.9 + d * 0.1;
            }
        };
        jPanel.setOpaque(!this.transparentWindow);
        jPanel.setFocusable(true);
        jPanel.addKeyListener(new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                boolean bl;
                if (keyEvent.getKeyCode() == 114) {
                    BaseWindow.this.debugOverlayEnabled = !BaseWindow.this.debugOverlayEnabled;
                    BaseWindow.this.requestRender();
                    keyEvent.consume();
                    return;
                }
                if (keyEvent.getKeyCode() == 115) {
                    BaseWindow.this.debugEventOverlayEnabled = !BaseWindow.this.debugEventOverlayEnabled;
                    BaseWindow.this.recordDebugEvent("DebugEvents=" + (BaseWindow.this.debugEventOverlayEnabled ? "ON" : "OFF"));
                    BaseWindow.this.requestRender();
                    keyEvent.consume();
                    return;
                }
                if (keyEvent.getKeyCode() == 27 && BaseWindow.this.layerHost != null && !BaseWindow.this.layerHost.getChildrenList().isEmpty()) {
                    BaseWindow.this.closeTopLayer();
                    keyEvent.consume();
                    return;
                }
                if (BaseWindow.this.focusedComponent != null && (bl = BaseWindow.this.focusedComponent.onKeyPressed(keyEvent))) {
                    keyEvent.consume();
                    BaseWindow.this.requestRenderIfNeeded();
                }
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
                boolean bl;
                if (BaseWindow.this.focusedComponent != null && (bl = BaseWindow.this.focusedComponent.onKeyTyped(keyEvent))) {
                    keyEvent.consume();
                    BaseWindow.this.requestRenderIfNeeded();
                }
            }
        });
        return jPanel;
    }

    private void clearBufferRegion(Graphics2D graphics2D, int n, int n2, int n3, int n4) {
        if (this.transparentWindow) {
            graphics2D.setComposite(AlphaComposite.Clear);
            graphics2D.fillRect(n, n2, n3, n4);
            graphics2D.setComposite(AlphaComposite.SrcOver);
            return;
        }
        graphics2D.setComposite(AlphaComposite.SrcOver);
        graphics2D.setColor(OPAQUE_WINDOW_BG);
        graphics2D.fillRect(n, n2, n3, n4);
    }

    private boolean resolveTransparentWindowSetting() {
        String string = System.getProperty("ui.window.transparent", "").trim().toLowerCase(Locale.ROOT);
        if ("true".equals(string) || "1".equals(string) || "yes".equals(string)) {
            return true;
        }
        if ("false".equals(string) || "0".equals(string) || "no".equals(string)) {
            return false;
        }
        String string2 = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return !string2.contains("linux");
    }

    private void drawDebugOverlay(Graphics2D graphics2D, int n, int n2) {
        Object object;
        if (this.smoothedFrameMs <= 0.0) {
            object = "FPS: --";
        } else {
            int n3 = (int)Math.max(1L, Math.round(1000.0 / this.smoothedFrameMs));
            object = "FPS: " + n3 + " (" + String.format(Locale.US, "%.2f", this.smoothedFrameMs) + " ms)";
        }
        String string = "Render: " + (this.lastFrameFullRedraw ? "FULL" : "PARTIAL");
        String string2 = "Dirty: " + this.lastDirtyCount + " rect(s), area=" + String.valueOf(this.lastDirtyArea == Integer.MAX_VALUE ? "FULL" : Integer.valueOf(this.lastDirtyArea));
        String string3 = "F3: Metrics | F4: EventDebug";
        int n4 = Math.min(360, n - 20);
        int n5 = 72;
        int n6 = 10;
        int n7 = Math.max(10, n2 - n5 - 10);
        graphics2D.setColor(new Color(17, 24, 39, 190));
        graphics2D.fillRoundRect(n6, n7, n4, n5, 12, 12);
        graphics2D.setColor(new Color(148, 163, 184, 220));
        graphics2D.drawRoundRect(n6, n7, n4, n5, 12, 12);
        graphics2D.setColor(new Color(241, 245, 249));
        graphics2D.drawString((String)object, n6 + 10, n7 + 20);
        graphics2D.drawString(string, n6 + 10, n7 + 36);
        graphics2D.drawString(string2, n6 + 10, n7 + 52);
        graphics2D.setColor(new Color(148, 163, 184));
        graphics2D.drawString(string3, n6 + 10, n7 + 67);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawEventDebugOverlay(Graphics2D graphics2D, int n, int n2) {
        ArrayList<String> arrayList;
        LinkedList<String> linkedList = this.debugEventLines;
        synchronized (linkedList) {
            arrayList = new ArrayList<String>(this.debugEventLines);
        }
        int n3 = 15;
        int n4 = Math.max(1, Math.min(12, arrayList.size()));
        int n5 = 34 + n4 * n3;
        int n6 = Math.min(580, n - 20);
        int n7 = Math.max(10, n - n6 - 10);
        int n8 = 10;
        graphics2D.setColor(new Color(11, 18, 32, 205));
        graphics2D.fillRoundRect(n7, n8, n6, n5, 10, 10);
        graphics2D.setColor(new Color(79, 70, 229, 210));
        graphics2D.drawRoundRect(n7, n8, n6, n5, 10, 10);
        graphics2D.setColor(new Color(226, 232, 240));
        graphics2D.drawString("Event Debug (F4)", n7 + 10, n8 + 18);
        int n9 = n8 + 34;
        if (arrayList.isEmpty()) {
            graphics2D.setColor(new Color(148, 163, 184));
            graphics2D.drawString("No events captured yet.", n7 + 10, n9);
            return;
        }
        for (String string : arrayList) {
            graphics2D.setColor(new Color(196, 205, 219));
            graphics2D.drawString(string, n7 + 10, n9);
            if ((n9 += n3) <= n8 + n5 - 6) continue;
            break;
        }
    }

    private void drawWindowControlHint(Graphics2D graphics2D, int n, int n2) {
        BaseComp baseComp = this.hoveredSystemButton;
        if (baseComp == null) {
            return;
        }
        String string = this.systemButtonHints.get(baseComp);
        if (string == null || string.isBlank()) {
            return;
        }
        Font font = graphics2D.getFont();
        graphics2D.setFont(new Font("Dialog", 1, 12));
        int n3 = graphics2D.getFontMetrics().stringWidth(string);
        int n4 = n3 + 18;
        int n5 = 24;
        int n6 = baseComp.getGlobalX() + baseComp.getWidth() / 2;
        int n7 = baseComp.getGlobalY() + baseComp.getHeight() + 10;
        int n8 = n6 - n4 / 2;
        n8 = Math.max(8, Math.min(n8, Math.max(8, n - n4 - 8)));
        n7 = Math.max(8, Math.min(n7, Math.max(8, n2 - n5 - 8)));
        graphics2D.setColor(new Color(15, 23, 42, 215));
        graphics2D.fillRoundRect(n8, n7, n4, n5, 10, 10);
        graphics2D.setColor(new Color(148, 163, 184, 220));
        graphics2D.drawRoundRect(n8, n7, n4, n5, 10, 10);
        graphics2D.setColor(new Color(241, 245, 249));
        graphics2D.drawString(string, n8 + 9, n7 + 16);
        graphics2D.setFont(font);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void recordDebugEvent(String string) {
        if (!this.debugEventOverlayEnabled) {
            return;
        }
        String string2 = String.format(Locale.US, "%tT.%<tL %s", System.currentTimeMillis(), string);
        LinkedList<String> linkedList = this.debugEventLines;
        synchronized (linkedList) {
            this.debugEventLines.addLast(string2);
            while (this.debugEventLines.size() > 12) {
                this.debugEventLines.removeFirst();
            }
        }
    }

    private void startRenderLoopIfNeeded() {
        if (this.fps <= 0 || this.activeRenderTimer != null) {
            return;
        }
        int n = Math.max(1, 1000 / this.fps);
        this.activeRenderTimer = new Timer(n, actionEvent -> this.canvas.repaint());
        this.activeRenderTimer.start();
    }

    private BaseComp createDefaultRoot(int n, int n2) {
        this.header = this.createDefaultHeader(n);
        this.content = new BaseComp(null);
        this.content.setStyleManager(new StyleManager(new Color(245, 245, 245), 0, n, Math.max(1, n2 - 38), 0, 38, "absolute"));
        this.content.setBounds(0, 38, n, Math.max(1, n2 - 38));
        this.layerHost = new BaseComp(this, null){

            @Override
            public boolean containsGlobalPoint(int n, int n2) {
                return !this.getChildrenList().isEmpty() && super.containsGlobalPoint(n, n2);
            }
        };
        this.layerHost.setBounds(0, 38, n, Math.max(1, n2 - 38));
        BaseComp baseComp = new BaseComp(this, new BaseComp[]{this.header, this.content, this.layerHost}){

            @Override
            public void customGraphics(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D)graphics;
                graphics2D.setColor(new Color(244, 245, 247));
                graphics2D.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 18, 18);
                graphics2D.setColor(new Color(208, 210, 214));
                graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 18, 18);
            }
        };
        baseComp.setStyleManager(new StyleManager(new Color(232, 232, 232), 18, n, n2, 0, 0, "absolute"));
        baseComp.setBounds(0, 0, n, n2);
        return baseComp;
    }

    private BaseComp createDefaultHeader(int n) {
        this.header = new BaseComp(null){

            @Override
            public void customGraphics(Graphics graphics) {
                graphics.setColor(new Color(235, 235, 235));
                graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
                graphics.setColor(new Color(190, 193, 198));
                graphics.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);
                graphics.setColor(new Color(74, 78, 84));
                int n = graphics.getFontMetrics().stringWidth(BaseWindow.this.frame.getTitle());
                int n2 = Math.max(16, (this.getWidth() - n) / 2);
                graphics.drawString(BaseWindow.this.frame.getTitle(), n2, 24);
            }
        };
        this.header.setWindowDragHandle(true);
        this.header.setBounds(0, 0, n, 38);
        this.header.setStyleManager(new StyleManager(new Color(235, 235, 235), 0, n, 38, 0, 0, "absolute"));
        this.closeButton = this.createSystemButton(0, new Color(255, 95, 86), "Fermer", baseWindow -> baseWindow.dispose());
        this.minimizeButton = this.createSystemButton(0, new Color(255, 189, 46), "Minimiser", baseWindow -> baseWindow.getNativeFrame().setState(1));
        this.maximizeButton = this.createSystemButton(0, new Color(39, 201, 63), "Maximiser / Restaurer", baseWindow -> {
            if ((baseWindow.getNativeFrame().getExtendedState() & 6) == 6) {
                baseWindow.getNativeFrame().setExtendedState(0);
            } else {
                baseWindow.getNativeFrame().setExtendedState(6);
            }
        });
        this.header.addChild(this.closeButton);
        this.header.addChild(this.minimizeButton);
        this.header.addChild(this.maximizeButton);
        this.repositionHeaderButtons(n);
        return this.header;
    }

    private BaseComp createSystemButton(int n, final Color color, String string, Consumer<BaseWindow> consumer) {
        BaseComp baseComp = new BaseComp(this, null){

            @Override
            public void customGraphics(Graphics graphics) {
                graphics.setColor(color);
                graphics.fillOval(0, 0, this.getWidth(), this.getHeight());
                graphics.setColor(new Color(0, 0, 0, 45));
                graphics.drawOval(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            }
        };
        baseComp.setBounds(n, 12, 12, 12);
        this.systemButtons.put(baseComp, consumer);
        this.systemButtonHints.put(baseComp, string == null ? "Action" : string);
        return baseComp;
    }

    private void setHoveredSystemButton(BaseComp baseComp) {
        if (this.hoveredSystemButton == baseComp) {
            return;
        }
        this.hoveredSystemButton = baseComp;
        this.requestRenderIfNeeded();
    }

    private void repositionHeaderButtons(int n) {
        int n2 = 12;
        int n3 = 12;
        int n4 = 8;
        int n5 = 20;
        int n6 = n - n5 - n3;
        if (this.closeButton != null) {
            this.closeButton.setBounds(n6, n2, n3, n3);
            n6 -= n3 + n4;
        }
        if (this.minimizeButton != null) {
            this.minimizeButton.setBounds(n6, n2, n3, n3);
            n6 -= n3 + n4;
        }
        if (this.maximizeButton != null) {
            this.maximizeButton.setBounds(n6, n2, n3, n3);
        }
    }

    private void installResizeHook() {
        this.canvas.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (BaseWindow.this.root == null) {
                    return;
                }
                BaseWindow.this.root.setBounds(0, 0, BaseWindow.this.canvas.getWidth(), BaseWindow.this.canvas.getHeight());
                BaseWindow.this.header.setBounds(0, 0, BaseWindow.this.canvas.getWidth(), 38);
                BaseWindow.this.repositionHeaderButtons(BaseWindow.this.canvas.getWidth());
                BaseWindow.this.content.setBounds(0, 38, BaseWindow.this.canvas.getWidth(), Math.max(1, BaseWindow.this.canvas.getHeight() - 38));
                BaseWindow.this.layerHost.setBounds(0, 38, BaseWindow.this.canvas.getWidth(), Math.max(1, BaseWindow.this.canvas.getHeight() - 38));
                for (BaseComp baseComp : BaseWindow.this.layerHost.getChildren()) {
                    baseComp.setBounds(0, 0, BaseWindow.this.layerHost.getWidth(), BaseWindow.this.layerHost.getHeight());
                    BaseComp[] baseCompArray = baseComp.getChildren();
                    if (baseCompArray.length <= 0) continue;
                    BaseComp baseComp2 = baseCompArray[0];
                    int n = Math.max(12, (BaseWindow.this.layerHost.getWidth() - baseComp2.getWidth()) / 2);
                    int n2 = Math.max(12, (BaseWindow.this.layerHost.getHeight() - baseComp2.getHeight()) / 2);
                    baseComp2.setBounds(n, n2, baseComp2.getWidth(), baseComp2.getHeight());
                }
                BaseWindow.this.applyRoundedShape(BaseWindow.this.frame.getWidth(), BaseWindow.this.frame.getHeight());
                BaseWindow.this.relayoutTree();
                for (Runnable runnable : BaseWindow.this.resizeListeners) {
                    runnable.run();
                }
                BaseWindow.this.requestRender();
            }
        });
    }

    private void installWindowCloseHook() {
        this.frame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosed(WindowEvent windowEvent) {
                if (BaseWindow.this.activeRenderTimer != null) {
                    BaseWindow.this.activeRenderTimer.stop();
                }
            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {
                if (BaseWindow.this.layerHost != null && !BaseWindow.this.layerHost.getChildrenList().isEmpty()) {
                    BaseWindow.this.closeTopLayer();
                }
            }
        });
    }

    private void relayoutTree() {
        this.relayoutTree(this.root);
    }

    private void relayoutTree(BaseComp baseComp) {
        if (baseComp == null) {
            return;
        }
        baseComp.doLayout();
        for (BaseComp baseComp2 : baseComp.getChildren()) {
            this.relayoutTree(baseComp2);
        }
    }

    private void wireMouseEvents() {
        MouseAdapter mouseAdapter = new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                BaseWindow.this.canvas.requestFocusInWindow();
                if (BaseWindow.this.dispatchLegacyWheelButton(mouseEvent)) {
                    return;
                }
                BaseComp baseComp = BaseWindow.this.hitTester.findBaseComp(mouseEvent.getX(), mouseEvent.getY(), BaseWindow.this.root);
                BaseWindow.this.setFocusedComponent(BaseWindow.this.resolveFocusableTarget(baseComp));
                if (mouseEvent.getButton() == 1 && BaseWindow.this.systemButtons.containsKey(baseComp)) {
                    BaseWindow.this.dispatchPointerEvent(UiEvent.Type.POINTER_DOWN, mouseEvent);
                    return;
                }
                ResizeEdge resizeEdge = BaseWindow.this.detectResizeEdge(mouseEvent.getX(), mouseEvent.getY());
                if (resizeEdge != ResizeEdge.NONE) {
                    BaseWindow.this.beginResize(resizeEdge, mouseEvent);
                    return;
                }
                BaseWindow.this.dispatchPointerEvent(UiEvent.Type.POINTER_DOWN, mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (BaseWindow.this.activeResizeEdge != ResizeEdge.NONE) {
                    BaseWindow.this.stopResize();
                    return;
                }
                BaseWindow.this.dispatchPointerEvent(UiEvent.Type.POINTER_UP, mouseEvent);
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (BaseWindow.this.activeResizeEdge != ResizeEdge.NONE) {
                    BaseWindow.this.applyResize(mouseEvent);
                    return;
                }
                BaseWindow.this.dispatchPointerEvent(UiEvent.Type.POINTER_MOVE, mouseEvent);
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                BaseComp baseComp = BaseWindow.this.hitTester.findBaseComp(mouseEvent.getX(), mouseEvent.getY(), BaseWindow.this.root);
                if (BaseWindow.this.systemButtons.containsKey(baseComp)) {
                    BaseWindow.this.setHoveredSystemButton(baseComp);
                    BaseWindow.this.changeCursor(12);
                } else if (BaseWindow.this.activeResizeEdge == ResizeEdge.NONE) {
                    BaseWindow.this.setHoveredSystemButton(null);
                    if (baseComp != null && baseComp.getCursor() != 0) {
                        BaseWindow.this.changeCursor(baseComp.getCursor());
                    } else if (baseComp != null && baseComp.isDraggable()) {
                        BaseWindow.this.changeCursor(12);
                    } else {
                        BaseWindow.this.updateResizeCursor(mouseEvent.getX(), mouseEvent.getY());
                    }
                } else {
                    BaseWindow.this.setHoveredSystemButton(null);
                }
                BaseWindow.this.dispatchPointerEvent(UiEvent.Type.POINTER_MOVE, mouseEvent);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                BaseWindow.this.setHoveredSystemButton(null);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                BaseWindow.this.dispatchWheelEvent(mouseWheelEvent);
            }
        };
        this.canvas.addMouseListener(mouseAdapter);
        this.canvas.addMouseMotionListener(mouseAdapter);
        this.canvas.addMouseWheelListener(mouseAdapter);
    }

    private void dispatchPointerEvent(UiEvent.Type type, MouseEvent mouseEvent) {
        Object object;
        BaseComp baseComp;
        if (this.root == null) {
            return;
        }
        this.recordDebugEvent("PTR " + String.valueOf((Object)type) + " btn=" + mouseEvent.getButton() + " x=" + mouseEvent.getX() + " y=" + mouseEvent.getY());
        UiEvent uiEvent = new UiEvent(type, mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen(), mouseEvent.getButton(), 0.0, mouseEvent.isShiftDown(), mouseEvent.getClickCount());
        uiEvent.setWindow(this);
        BaseComp baseComp2 = this.hitTester.findBaseComp(mouseEvent.getX(), mouseEvent.getY(), this.root);
        BaseComp baseComp3 = baseComp = this.capturedPointer != null ? this.capturedPointer : baseComp2;
        if (baseComp == null) {
            baseComp = this.root;
        }
        uiEvent.setTarget(baseComp);
        boolean bl = false;
        if (type == UiEvent.Type.POINTER_DOWN && mouseEvent.getButton() == 1 && this.systemButtons.containsKey(baseComp)) {
            this.pressedSystemButton = baseComp;
            return;
        }
        if (type == UiEvent.Type.POINTER_UP && mouseEvent.getButton() == 1 && this.pressedSystemButton != null) {
            if (this.pressedSystemButton == baseComp && this.systemButtons.containsKey(baseComp) && (object = this.systemButtons.get(baseComp)) != null) {
                object.accept(this);
            }
            this.pressedSystemButton = null;
            bl = true;
        }
        if (type == UiEvent.Type.POINTER_DOWN) {
            BaseComp baseComp4;
            object = this.findFirstAncestor(baseComp, BaseComp::isWindowDragHandle);
            if (object != null) {
                this.windowDragActive = true;
                this.windowDragOffsetX = mouseEvent.getXOnScreen() - this.frame.getX();
                this.windowDragOffsetY = mouseEvent.getYOnScreen() - this.frame.getY();
                bl = true;
            }
            if ((baseComp4 = this.findFirstAncestor(baseComp, BaseComp::isDraggable)) != null) {
                this.capturedPointer = baseComp4;
                bl = true;
            }
        }
        if (this.windowDragActive && type == UiEvent.Type.POINTER_MOVE) {
            int n = mouseEvent.getXOnScreen() - this.windowDragOffsetX;
            int n2 = mouseEvent.getYOnScreen() - this.windowDragOffsetY;
            this.frame.setLocation(n, n2);
            bl = true;
        }
        this.dispatchBubble(baseComp, uiEvent);
        if (type == UiEvent.Type.POINTER_UP) {
            this.capturedPointer = null;
            this.windowDragActive = false;
            bl = true;
        }
        if (this.fps == 0 && (type != UiEvent.Type.POINTER_MOVE || bl)) {
            this.requestRenderIfNeeded();
        }
    }

    private void dispatchWheelEvent(MouseWheelEvent mouseWheelEvent) {
        if (this.root == null) {
            return;
        }
        double d = mouseWheelEvent.getPreciseWheelRotation();
        if (d == 0.0) {
            d = mouseWheelEvent.getWheelRotation();
        }
        if (d == 0.0 && mouseWheelEvent.getUnitsToScroll() != 0) {
            d = (double)mouseWheelEvent.getUnitsToScroll() / 3.0;
        }
        this.recordDebugEvent(String.format(Locale.US, "WHL rot=%.3f precise=%.3f wheel=%d units=%d shift=%s type=%d", d, mouseWheelEvent.getPreciseWheelRotation(), mouseWheelEvent.getWheelRotation(), mouseWheelEvent.getUnitsToScroll(), mouseWheelEvent.isShiftDown(), mouseWheelEvent.getScrollType()));
        UiEvent uiEvent = new UiEvent(UiEvent.Type.WHEEL, mouseWheelEvent.getX(), mouseWheelEvent.getY(), mouseWheelEvent.getXOnScreen(), mouseWheelEvent.getYOnScreen(), 0, d, mouseWheelEvent.isShiftDown());
        uiEvent.setWindow(this);
        BaseComp baseComp = this.hitTester.findBaseComp(mouseWheelEvent.getX(), mouseWheelEvent.getY(), this.root);
        if (baseComp == null) {
            baseComp = this.root;
        }
        uiEvent.setTarget(baseComp);
        this.dispatchBubble(baseComp, uiEvent);
        if (this.fps == 0) {
            this.requestRenderIfNeeded();
        }
    }

    private boolean dispatchLegacyWheelButton(MouseEvent mouseEvent) {
        double d;
        int n = mouseEvent.getButton();
        if (n < 4) {
            return false;
        }
        boolean bl = false;
        switch (n) {
            case 4: {
                d = -1.0;
                break;
            }
            case 5: {
                d = 1.0;
                break;
            }
            case 6: {
                d = -1.0;
                bl = true;
                break;
            }
            case 7: {
                d = 1.0;
                bl = true;
                break;
            }
            default: {
                return false;
            }
        }
        UiEvent uiEvent = new UiEvent(UiEvent.Type.WHEEL, mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen(), 0, d, bl || mouseEvent.isShiftDown());
        this.recordDebugEvent("LEGACY-WHL btn=" + n + " rot=" + d + " shiftLike=" + bl);
        uiEvent.setWindow(this);
        BaseComp baseComp = this.hitTester.findBaseComp(mouseEvent.getX(), mouseEvent.getY(), this.root);
        if (baseComp == null) {
            baseComp = this.root;
        }
        uiEvent.setTarget(baseComp);
        this.dispatchBubble(baseComp, uiEvent);
        if (this.fps == 0) {
            this.requestRenderIfNeeded();
        }
        return true;
    }

    private BaseComp resolveFocusableTarget(BaseComp baseComp) {
        for (BaseComp baseComp2 = baseComp; baseComp2 != null; baseComp2 = baseComp2.getParent()) {
            if (!baseComp2.isFocusable()) continue;
            return baseComp2;
        }
        return null;
    }

    private BaseComp findFirstAncestor(BaseComp baseComp, Predicate<BaseComp> predicate) {
        for (BaseComp baseComp2 = baseComp; baseComp2 != null; baseComp2 = baseComp2.getParent()) {
            if (!predicate.test(baseComp2)) continue;
            return baseComp2;
        }
        return null;
    }

    private void setFocusedComponent(BaseComp baseComp) {
        if (this.focusedComponent == baseComp) {
            return;
        }
        if (this.focusedComponent != null) {
            this.focusedComponent.setFocused(false);
        }
        this.focusedComponent = baseComp;
        if (this.focusedComponent != null) {
            this.focusedComponent.setFocused(true);
        }
    }

    private ResizeEdge detectResizeEdge(int n, int n2) {
        boolean bl;
        int n3 = this.canvas.getWidth();
        int n4 = this.canvas.getHeight();
        boolean bl2 = n2 <= 8;
        boolean bl3 = n2 >= n4 - 8;
        boolean bl4 = n <= 8;
        boolean bl5 = bl = n >= n3 - 8;
        if (bl2 && bl4) {
            return ResizeEdge.NORTH_WEST;
        }
        if (bl2 && bl) {
            return ResizeEdge.NORTH_EAST;
        }
        if (bl3 && bl4) {
            return ResizeEdge.SOUTH_WEST;
        }
        if (bl3 && bl) {
            return ResizeEdge.SOUTH_EAST;
        }
        if (bl2) {
            return ResizeEdge.NORTH;
        }
        if (bl3) {
            return ResizeEdge.SOUTH;
        }
        if (bl4) {
            return ResizeEdge.WEST;
        }
        if (bl) {
            return ResizeEdge.EAST;
        }
        return ResizeEdge.NONE;
    }

    private void changeCursor(int n) {
        if (this.currentCursorId != n) {
            this.currentCursorId = n;
            this.canvas.setCursor(Cursor.getPredefinedCursor(n));
        }
    }

    private void updateResizeCursor(int n, int n2) {
        ResizeEdge resizeEdge = this.detectResizeEdge(n, n2);
        int n3 = switch (resizeEdge.ordinal()) {
            case 1, 2 -> 8;
            case 3, 4 -> 11;
            case 5, 8 -> 7;
            case 6, 7 -> 6;
            default -> 0;
        };
        this.changeCursor(n3);
    }

    private void beginResize(ResizeEdge resizeEdge, MouseEvent mouseEvent) {
        this.activeResizeEdge = resizeEdge;
        this.resizeStartMouse = mouseEvent.getLocationOnScreen();
        this.resizeStartBounds = this.frame.getBounds();
    }

    private void applyResize(MouseEvent mouseEvent) {
        if (this.resizeStartMouse == null || this.resizeStartBounds == null) {
            return;
        }
        int n = mouseEvent.getXOnScreen() - this.resizeStartMouse.x;
        int n2 = mouseEvent.getYOnScreen() - this.resizeStartMouse.y;
        int n3 = this.resizeStartBounds.x;
        int n4 = this.resizeStartBounds.y;
        int n5 = this.resizeStartBounds.width;
        int n6 = this.resizeStartBounds.height;
        switch (this.activeResizeEdge.ordinal()) {
            case 3: {
                n5 = this.resizeStartBounds.width + n;
                break;
            }
            case 4: {
                n3 = this.resizeStartBounds.x + n;
                n5 = this.resizeStartBounds.width - n;
                break;
            }
            case 2: {
                n6 = this.resizeStartBounds.height + n2;
                break;
            }
            case 1: {
                n4 = this.resizeStartBounds.y + n2;
                n6 = this.resizeStartBounds.height - n2;
                break;
            }
            case 5: {
                n4 = this.resizeStartBounds.y + n2;
                n6 = this.resizeStartBounds.height - n2;
                n5 = this.resizeStartBounds.width + n;
                break;
            }
            case 6: {
                n3 = this.resizeStartBounds.x + n;
                n5 = this.resizeStartBounds.width - n;
                n4 = this.resizeStartBounds.y + n2;
                n6 = this.resizeStartBounds.height - n2;
                break;
            }
            case 7: {
                n5 = this.resizeStartBounds.width + n;
                n6 = this.resizeStartBounds.height + n2;
                break;
            }
            case 8: {
                n3 = this.resizeStartBounds.x + n;
                n5 = this.resizeStartBounds.width - n;
                n6 = this.resizeStartBounds.height + n2;
                break;
            }
        }
        if (n5 < 520) {
            if (this.activeResizeEdge == ResizeEdge.WEST || this.activeResizeEdge == ResizeEdge.NORTH_WEST || this.activeResizeEdge == ResizeEdge.SOUTH_WEST) {
                n3 -= 520 - n5;
            }
            n5 = 520;
        }
        if (n6 < 380) {
            if (this.activeResizeEdge == ResizeEdge.NORTH || this.activeResizeEdge == ResizeEdge.NORTH_WEST || this.activeResizeEdge == ResizeEdge.NORTH_EAST) {
                n4 -= 380 - n6;
            }
            n6 = 380;
        }
        this.frame.setBounds(n3, n4, n5, n6);
        this.applyRoundedShape(n5, n6);
        this.requestRender();
    }

    private void stopResize() {
        this.activeResizeEdge = ResizeEdge.NONE;
        this.resizeStartMouse = null;
        this.resizeStartBounds = null;
        this.changeCursor(0);
    }

    private void applyRoundedShape(int n, int n2) {
        if (n <= 0 || n2 <= 0) {
            return;
        }
        if (!this.transparentWindow) {
            // empty if block
        }
    }

    private void dispatchBubble(BaseComp baseComp, UiEvent uiEvent) {
        for (BaseComp baseComp2 = baseComp; baseComp2 != null; baseComp2 = baseComp2.getParent()) {
            if (baseComp2.getEventManager() == null) continue;
            baseComp2.getEventManager().trigger(uiEvent, baseComp2);
            if (uiEvent.isPropagationStopped()) break;
        }
    }

    private static enum ResizeEdge {
        NONE,
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST;

    }
}

