package gui.components;

import java.awt.Color;

import components.Button;

public class PrimaryButton extends Button {
    public PrimaryButton(String text, int x, int y, int width, int height, Runnable onClick) {
        super(text, x, y, width, height, onClick);
        setBackground(new Color(5, 9, 40));
        setForeground(Color.WHITE);
    }
}
