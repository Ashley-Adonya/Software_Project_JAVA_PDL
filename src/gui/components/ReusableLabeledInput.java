package gui.components;

import java.awt.Color;
import java.awt.Font;

import components.Label;
import components.TextField;
import main.BaseComp;

public class ReusableLabeledInput extends BaseComp {
    private static final int LABEL_HEIGHT = 18;
    private static final int FIELD_TOP = 22;

    private final Label label;
    private final boolean masked;
    private final TextField textField;
    private final PasswordField passwordField;

    public ReusableLabeledInput(String labelText, String placeholder, int x, int y, int width, int height) {
        this(labelText, placeholder, x, y, width, height, false);
    }

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

    public String getValue() {
        return masked ? passwordField.getText() : textField.getText();
    }

    public void setValue(String value) {
        if (masked) {
            passwordField.setText(value == null ? "" : value);
        } else {
            textField.setText(value == null ? "" : value);
        }
    }

    public TextField getTextField() {
        return textField;
    }
}
