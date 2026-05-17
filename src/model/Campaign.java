package model;

/**
 * Represents an attribution campaign for dominant presentation sessions.
 * This is the root entity that orchestrates the entire session registration process.
 * A campaign defines temporal parameters (start date, end date, registration day),
 * the maximum number of choices a student can submit, and the lifecycle status
 * which transitions through PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED,
 * and ARCHIVED states. Each campaign is associated with a specific student promotion
 * and tracks timestamps for every status transition.
 * <p>
 * Maps to the {@code campaigns} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class Campaign {
    /** Unique identifier of the campaign. Auto-generated primary key. */
    private int id;

    /** Human-readable name or title of the campaign (e.g. "2025 S1 Dominante Registration"). */
    private String name;

    /** Target promotion code (e.g. "ING1", "ING2") that this campaign applies to. */
    private String promo;

    /** Specific day designated for student registration, typically a date string in yyyy-MM-dd format. */
    private String registrationDay;

    /** Start date of the campaign's active phase, after which students can begin submitting choices. Date string in yyyy-MM-dd format. */
    private String startDate;

    /** End date of the campaign's active phase, after which no more submissions are accepted. Date string in yyyy-MM-dd format. */
    private String endDate;

    /** Maximum number of ranked choices a student is allowed to submit during this campaign. Must be positive. */
    private int maxChoices;

    /** Current lifecycle status of the campaign. One of: PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED, ARCHIVED. */
    private String status;

    /** Identifier of the administrator user who created this campaign. Foreign key to the users table. */
    private int createdBy;

    /** Timestamp (ISO-8601) of when the campaign was opened for submissions. Nullable — set when status transitions to OPEN. */
    private String openedAt;

    /** Timestamp (ISO-8601) of when the campaign was closed for submissions. Nullable — set when status transitions to CLOSED. */
    private String closedAt;

    /** Timestamp (ISO-8601) of when the attribution algorithm was run. Nullable — set when status transitions to PROCESSING. */
    private String processedAt;

    /** Timestamp (ISO-8601) of when the results were validated by an administrator. Nullable — set when status transitions to VALIDATED. */
    private String validatedAt;

    /** Timestamp (ISO-8601) of when the campaign was archived. Nullable — set when status transitions to ARCHIVED. */
    private String archivedAt;

    /**
     * Default no-argument constructor.
     * Creates an empty Campaign instance. Fields must be set via setters before use.
     */
    public Campaign() {
    }

    /**
     * Constructs a Campaign with core fields, excluding timestamp tracking fields.
     *
     * @param id             the unique identifier of the campaign
     * @param name           the name/title of the campaign
     * @param promo          the target promotion code
     * @param registrationDay the designated registration day
     * @param startDate      the start date of the active phase
     * @param endDate        the end date of the active phase
     * @param maxChoices     the maximum number of choices per student
     * @param status         the initial lifecycle status
     * @param createdBy      the administrator ID who created the campaign
     */
    public Campaign(int id, String name, String promo, String registrationDay, String startDate, String endDate, int maxChoices, String status, int createdBy) {
        this.id = id;
        this.name = name;
        this.promo = promo;
        this.registrationDay = registrationDay;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxChoices = maxChoices;
        this.status = status;
        this.createdBy = createdBy;
    }

    /**
     * Constructs a Campaign with all fields including status transition timestamps.
     *
     * @param id              the unique identifier of the campaign
     * @param name            the name/title of the campaign
     * @param promo           the target promotion code
     * @param registrationDay the designated registration day
     * @param startDate       the start date of the active phase
     * @param endDate         the end date of the active phase
     * @param maxChoices      the maximum number of choices per student
     * @param status          the current lifecycle status
     * @param createdBy       the administrator ID who created the campaign
     * @param openedAt        the timestamp when the campaign was opened
     * @param closedAt        the timestamp when the campaign was closed
     * @param processedAt     the timestamp when the campaign was processed
     * @param validatedAt     the timestamp when the campaign was validated
     * @param archivedAt      the timestamp when the campaign was archived
     */
    public Campaign(int id, String name, String promo, String registrationDay, String startDate, String endDate, int maxChoices, String status, int createdBy, String openedAt, String closedAt, String processedAt, String validatedAt, String archivedAt) {
        this.id = id;
        this.name = name;
        this.promo = promo;
        this.registrationDay = registrationDay;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxChoices = maxChoices;
        this.status = status;
        this.createdBy = createdBy;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.processedAt = processedAt;
        this.validatedAt = validatedAt;
        this.archivedAt = archivedAt;
    }

    /**
     * Returns the unique identifier of the campaign.
     *
     * @return the campaign ID
     */
    public int getId() { 
        return id; 
    }

    /**
     * Sets the unique identifier of the campaign.
     *
     * @param id the campaign ID to set
     */
    public void setId(int id) {
         this.id = id; 
    }

    /**
     * Returns the name/title of the campaign.
     *
     * @return the campaign name
     */
    public String getName() {
         return name; 
    }

    /**
     * Sets the name/title of the campaign.
     *
     * @param name the campaign name to set
     */
    public void setName(String name) {
         this.name = name; }

    /**
     * Returns the target promotion code for this campaign.
     *
     * @return the promotion code (e.g. "ING1", "ING2")
     */
    public String getPromo() {
         return promo; 
    }

    /**
     * Sets the target promotion code for this campaign.
     *
     * @param promo the promotion code to set
     */
    public void setPromo(String promo) {
         this.promo = promo; 
    }

    /**
     * Returns the designated registration day for this campaign.
     *
     * @return the registration day as a date string
     */
    public String getRegistrationDay() {
         return registrationDay; 
    }

    /**
     * Sets the designated registration day for this campaign.
     *
     * @param registrationDay the registration day to set
     */
    public void setRegistrationDay(String registrationDay) {
         this.registrationDay = registrationDay; 
    }

    /**
     * Returns the start date of the campaign's active phase.
     *
     * @return the start date as a string
     */
    public String getStartDate() {
         return startDate; 
    }

    /**
     * Sets the start date of the campaign's active phase.
     *
     * @param startDate the start date to set
     */
    public void setStartDate(String startDate) {
         this.startDate = startDate; 
    }

    /**
     * Returns the end date of the campaign's active phase.
     *
     * @return the end date as a string
     */
    public String getEndDate() { 
        return endDate; 

    }

    /**
     * Sets the end date of the campaign's active phase.
     *
     * @param endDate the end date to set
     */
    public void setEndDate(String endDate) {
         this.endDate = endDate; 
    }

    /**
     * Returns the maximum number of choices a student can submit.
     *
     * @return the maximum choices count
     */
    public int getMaxChoices() {
         return maxChoices; 
    }

    /**
     * Sets the maximum number of choices a student can submit.
     *
     * @param maxChoices the maximum choices count to set
     */
    public void setMaxChoices(int maxChoices) {
         this.maxChoices = maxChoices; 
    }

    /**
     * Returns the current lifecycle status of the campaign.
     *
     * @return the status string (PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED, ARCHIVED)
     */
    public String getStatus() {
         return status; 
    }

    /**
     * Sets the current lifecycle status of the campaign.
     *
     * @param status the status string to set
     */
    public void setStatus(String status) {
         this.status = status; 
    }

    /**
     * Returns the administrator ID who created this campaign.
     *
     * @return the creator user ID
     */
    public int getCreatedBy() {
         return createdBy; 
    }

    /**
     * Sets the administrator ID who created this campaign.
     *
     * @param createdBy the creator user ID to set
     */
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the timestamp when the campaign was opened for submissions.
     *
     * @return the opened-at timestamp, or null if not yet opened
     */
    public String getOpenedAt() {
        return openedAt;
    }

    /**
     * Sets the timestamp when the campaign was opened for submissions.
     *
     * @param openedAt the opened-at timestamp to set
     */
    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    /**
     * Returns the timestamp when the campaign was closed for submissions.
     *
     * @return the closed-at timestamp, or null if not yet closed
     */
    public String getClosedAt() {
        return closedAt;
    }

    /**
     * Sets the timestamp when the campaign was closed for submissions.
     *
     * @param closedAt the closed-at timestamp to set
     */
    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * Returns the timestamp when the attribution algorithm was executed.
     *
     * @return the processed-at timestamp, or null if not yet processed
     */
    public String getProcessedAt() {
        return processedAt;
    }

    /**
     * Sets the timestamp when the attribution algorithm was executed.
     *
     * @param processedAt the processed-at timestamp to set
     */
    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    /**
     * Returns the timestamp when the campaign results were validated.
     *
     * @return the validated-at timestamp, or null if not yet validated
     */
    public String getValidatedAt() {
        return validatedAt;
    }

    /**
     * Sets the timestamp when the campaign results were validated.
     *
     * @param validatedAt the validated-at timestamp to set
     */
    public void setValidatedAt(String validatedAt) {
        this.validatedAt = validatedAt;
    }

    /**
     * Returns the timestamp when the campaign was archived.
     *
     * @return the archived-at timestamp, or null if not yet archived
     */
    public String getArchivedAt() {
        return archivedAt;
    }

    /**
     * Sets the timestamp when the campaign was archived.
     *
     * @param archivedAt the archived-at timestamp to set
     */
    public void setArchivedAt(String archivedAt) {
        this.archivedAt = archivedAt;
    }
}
