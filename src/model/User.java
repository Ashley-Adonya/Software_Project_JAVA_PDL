package model;

/**
 * Represents a user of the session attribution system.
 * Users can be either administrators (who create and manage campaigns, sessions,
 * and dominantes) or students (who submit their ranked choices during a campaign).
 * Each user has login credentials, a role-based profile, an activation state,
 * and is optionally associated with a promotion (for students).
 * <p>
 * Maps to the {@code users} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class User {
    /** Unique identifier of the user. Auto-generated primary key. */
    private int id;

    /** Unique login username used for authentication. Cannot be null. */
    private String login;

    /** Hashed password used for authentication. Stored securely, never in plain text. */
    private String password;

    /** Full display name of the user (first name and last name). */
    private String fullName;

    /** Role of the user in the system. Expected values: "admin" or "student". Determines permissions and available actions. */
    private String role;

    /** Promotion/year group the student belongs to (e.g. "ING1", "ING2"). May be null for administrators. */
    private String promo;

    /** Whether the user account is active. Inactive accounts cannot log in or perform actions. */
    private boolean active;

    /**
     * Default no-argument constructor.
     * Creates an empty User instance. Fields must be populated via setters before use.
     */
    public User() {
    }

    /**
     * Constructs a User with all fields.
     *
     * @param id       the unique identifier of the user
     * @param login    the unique login username
     * @param password the hashed password
     * @param fullName the full display name of the user
     * @param role     the system role ("admin" or "student")
     * @param promo    the promotion code (nullable, primarily for students)
     * @param active   whether the account is active
     */
    public User(int id, String login, String password, String fullName, String role, String promo, boolean active) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.promo = promo;
        this.active = active;
    }

    /**
     * Returns the unique identifier of the user.
     *
     * @return the user ID
     */
    public int getId() { 
        return id;
     }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id the user ID to set
     */
    public void setId(int id) { 
        this.id = id;
     }

    /**
     * Returns the unique login username of the user.
     *
     * @return the login username
     */
    public String getLogin() { 
        return login;
     }

    /**
     * Sets the unique login username of the user.
     *
     * @param login the login username to set
     */
    public void setLogin(String login) {
         this.login = login;
         }

    /**
     * Returns the hashed password of the user.
     *
     * @return the hashed password string
     */
    public String getPassword() {
         return password;
    }

    /**
     * Sets the hashed password of the user.
     *
     * @param password the hashed password to set
     */
    public void setPassword(String password) {
         this.password = password; 
    }

    /**
     * Returns the full display name of the user.
     *
     * @return the full name (first and last name)
     */
    public String getFullName() {
         return fullName; 
    }

    /**
     * Sets the full display name of the user.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
         this.fullName = fullName; 
    }

    /**
     * Returns the system role of the user.
     *
     * @return the role ("admin" or "student")
     */
    public String getRole() {
         return role; 
    }

    /**
     * Sets the system role of the user.
     *
     * @param role the role to set ("admin" or "student")
     */
    public void setRole(String role) {
     this.role = role; 
    }

    /**
     * Returns the promotion/year group of the user.
     *
     * @return the promotion code, or null if not applicable
     */
    public String getPromo() {
     return promo; 
    }

    /**
     * Sets the promotion/year group of the user.
     *
     * @param promo the promotion code to set
     */
    public void setPromo(String promo) {
         this.promo = promo; 
    }

    /**
     * Indicates whether the user account is active.
     *
     * @return true if the account is active, false otherwise
     */
    public boolean isActive() {
         return active; 
    }

    /**
     * Sets whether the user account is active.
     *
     * @param active true to activate the account, false to deactivate
     */
    public void setActive(boolean active) {
         this.active = active; 
    }
}
