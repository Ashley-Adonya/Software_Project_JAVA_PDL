package gui.components;

import java.awt.Color;

import components.Button;

/**
 * A styled primary action button with a dark navy background and white
 * foreground text. This is a convenience wrapper around {@link Button}
 * that applies a consistent primary colour scheme used throughout the
 * application for the most prominent call-to-action buttons.
 */
public class PrimaryButton extends Button {
    /**
     * Constructs a primary button with the given text, position, dimensions,
     * and click handler. The background defaults to dark navy and the
     * foreground to white.
     *
     * @param text    the label displayed on the button
     * @param x       the x-coordinate of the button
     * @param y       the y-coordinate of the button
     * @param width   the width of the button
     * @param height  the height of the button
     * @param onClick runnable invoked when the button is clicked; may be null
     */
    public PrimaryButton(String text, int x, int y, int width, int height, Runnable onClick) {
        super(text, x, y, width, height, onClick);
        setBackground(new Color(5, 9, 40));
        setForeground(Color.WHITE);
    }
}
