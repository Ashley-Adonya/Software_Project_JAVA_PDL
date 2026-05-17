package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;
import components.TextField;
import main.BaseComp;

/**
 * A reusable composite component that groups a bold label with an input
 * field (either a standard text field or a password-masked field). It
 * simplifies form creation by encapsulating the label, the input widget,
 * and layout coordination into a single component. Callers can read or
 * write the input value and access the underlying text field directly
 * if needed.
 */
public class ReusableLabeledInput extends BaseComp {
    private static final int LABEL_HEIGHT = 18;
    private static final int FIELD_TOP = 22;

    private final Label label;
    private final boolean masked;
    private final TextField textField;
    private final PasswordField passwordField;

    /**
     * Constructs a labeled input with a plain (unmasked) text field.
     *
     * @param labelText   the text displayed in the bold label above the field
     * @param placeholder the placeholder text shown inside the field when empty
     * @param x           the x-coordinate of this component
     * @param y           the y-coordinate of this component
     * @param width       the width of this component
     * @param height      the height of this component
     */
    public ReusableLabeledInput(String labelText, String placeholder, int x, int y, int width, int height) {
        this(labelText, placeholder, x, y, width, height, false);
    }

    /**
     * Constructs a labeled input with an optional masked (password) field.
     *
     * @param labelText   the text displayed in the bold label above the field
     * @param placeholder the placeholder text shown inside the field when empty
     * @param x           the x-coordinate of this component
     * @param y           the y-coordinate of this component
     * @param width       the width of this component
     * @param height      the height of this component
     * @param masked      if true a {@link PasswordField} is used; otherwise a
     *                    plain {@link TextField} is used
     */
    public ReusableLabeledInput(String labelText, String placeholder, int x, int y, int width, int height,
            boolean masked) {
        super(null);
        this.masked = masked;
        this.label = new Label(labelText, 0, 0, width, LABEL_HEIGHT);
        this.label.setFont(new Font("Dialog", Font.BOLD, 12));
        this.label.setColor(new Color(42, 48, 61));

        if (masked) {
            this.textField = null;
            this.passwordField = new PasswordField(0, FIELD_TOP, width, Math.max(34, height - FIELD_TOP));
            this.passwordField.setPlaceholder(placeholder);
            addChild(passwordField);
        } else {
            this.passwordField = null;
            this.textField = new TextField(0, FIELD_TOP, width, Math.max(34, height - FIELD_TOP));
            this.textField.setPlaceholder(placeholder);
            addChild(textField);
        }

        addChild(label);
        setBounds(x, y, width, height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (label == null) {
            return;
        }
        label.setBounds(0, 0, width, LABEL_HEIGHT);
        if (masked) {
            if (passwordField == null) {
                return;
            }
            passwordField.setBounds(0, FIELD_TOP, width, Math.max(34, height - FIELD_TOP));
        } else {
            if (textField == null) {
                return;
            }
            textField.setBounds(0, FIELD_TOP, width, Math.max(34, height - FIELD_TOP));
        }
    }

    /**
     * Returns the current value of the input field.
     *
     * @return the text content of the underlying input field (never null)
     */
    public String getValue() {
        return masked ? passwordField.getText() : textField.getText();
    }

    /**
     * Sets the value of the input field.
     *
     * @param value the new text; null is treated as an empty string
     */
    public void setValue(String value) {
        if (masked) {
            passwordField.setText(value == null ? "" : value);
        } else {
            textField.setText(value == null ? "" : value);
        }
    }

    /**
     * Returns the underlying plain {@link TextField}, or null if this
     * component was created in masked mode.
     *
     * @return the text field instance, or null if in password mode
     */
    public TextField getTextField() {
        return textField;
    }
}
