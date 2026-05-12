/*
 * Decompiled with CFR 0.152.
 */
package components;

import components.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class LiveClockLabel
extends Label {
    private final DateTimeFormatter formatter;
    private final Timer timer;

    public LiveClockLabel(int n, int n2, int n3, int n4, String string) {
        super("", n, n2, n3, n4);
        this.formatter = DateTimeFormatter.ofPattern(string == null || string.isBlank() ? "dd/MM/yyyy HH:mm:ss" : string);
        this.timer = new Timer(800, actionEvent -> this.tick());
        this.timer.setRepeats(true);
        this.tick();
        this.timer.start();
    }

    private void tick() {
        this.setText(LocalDateTime.now().format(this.formatter));
        this.invalidate();
    }
}

