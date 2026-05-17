package model;

import java.awt.Color;

/**
 * Represents a dominante (specialty / field of study) offered by the institution.
 * A dominante is the academic subject around which presentation sessions are organized.
 * Each dominante has a unique code, a name, a responsible faculty member, a description,
 * a display color for UI identification, and an active flag to indicate whether it is
 * currently available for registration. Multiple session slots can be associated with
 * a single dominante within a campaign.
 * <p>
 * Maps to the {@code dominantes} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class Dominante {
    /** Unique identifier of the dominante. Auto-generated primary key. */
    private int id;

    /** Unique short code identifying the dominante (e.g. "CS", "AI", "NET"). Used as a human-readable key. */
    private String code;

    /** Full name of the dominante (e.g. "Computer Science", "Artificial Intelligence"). */
    private String name;

    /** Full name of the faculty member or teacher responsible for this dominante. */
    private String responsibleName;

    /** Detailed description of the dominante content and objectives. Shown to students during the choice process. */
    private String description;

    /** Hexadecimal color code (e.g. "#ff5733") used for visual identification in the user interface. */
    private String color;

    /** Whether this dominante is active and available for registration. Inactive dominantes are hidden from student choices. */
    private boolean active;

    /**
     * Default no-argument constructor.
     * Creates an empty Dominante instance. Fields must be set via setters before use.
     */
    public Dominante() {
    }

    /**
     * Constructs a Dominante with all fields.
     *
     * @param id              the unique identifier of the dominante
     * @param code            the short unique code
     * @param name            the full name of the dominante
     * @param responsibleName the name of the responsible faculty member
     * @param description     a detailed description of the dominante
     * @param color           the hex color code for UI display
     * @param active          whether the dominante is active
     */
    public Dominante(int id, String code, String name, String responsibleName, String description, String color, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.responsibleName = responsibleName;
        this.description = description;
        this.color = color;
        this.active = active;
    }

    /**
     * Returns the unique identifier of the dominante.
     *
     * @return the dominante ID
     */
    public int getId() {
         return id; 
    }

    /**
     * Sets the unique identifier of the dominante.
     *
     * @param id the dominante ID to set
     */
    public void setId(int id) {
         this.id = id; 
    }

    /**
     * Returns the short unique code of the dominante.
     *
     * @return the dominante code (e.g. "CS", "AI")
     */
    public String getCode() {
         return code; 
    }

    /**
     * Sets the short unique code of the dominante.
     *
     * @param code the dominante code to set
     */
    public void setCode(String code) { this.code = code; }

    /**
     * Returns the full name of the dominante.
     *
     * @return the dominante name
     */
    public String getName() {
         return name; 
    }

    /**
     * Sets the full name of the dominante.
     *
     * @param name the dominante name to set
     */
    public void setName(String name) {
         this.name = name; 
    }

    /**
     * Returns the name of the responsible faculty member.
     *
     * @return the responsible person's name
     */
    public String getResponsibleName() {
         return responsibleName; 
    }

    /**
     * Sets the name of the responsible faculty member.
     *
     * @param responsibleName the responsible person's name to set
     */
    public void setResponsibleName(String responsibleName) {
         this.responsibleName = responsibleName; 
    }

    /**
     * Returns the detailed description of the dominante.
     *
     * @return the description text
     */
    public String getDescription() { 
        return description; 
    }

    /**
     * Sets the detailed description of the dominante.
     *
     * @param description the description text to set
     */
    public void setDescription(String description) {
         this.description = description; 
    }

    /**
     * Returns the hex color code used for UI display.
     *
     * @return the color as a hex string (e.g. "#ff5733")
     */
    public String getColor() {
         return color; 
    }

    /**
     * Sets the hex color code for UI display using a string value.
     *
     * @param color the hex color string to set (e.g. "#ff5733")
     */
    public void setColor(String color) {
         this.color = color; 
    }

    /**
     * Sets the hex color code for UI display using a java.awt.Color object.
     * Converts the Color to a hexadecimal string representation (e.g. "#ff5733").
     *
     * @param color the java.awt.Color object to convert and set
     */
    public void setColor(Color color) {
         this.color = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Indicates whether this dominante is active and available for registration.
     *
     * @return true if the dominante is active, false otherwise
     */
    public boolean isActive() {
         return active; 
    }

    /**
     * Sets whether this dominante is active and available for registration.
     *
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
         this.active = active; 
    }

}
