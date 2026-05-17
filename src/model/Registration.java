package model;

/**
 * Represents the result of a student's registration to a session slot after
 * the attribution algorithm has run. A Registration records which student was
 * assigned to which session, the rank of the choice that led to this assignment
 * (if any), and the final status of the registration (ALLOCATED, WAITLIST,
 * REJECTED, or CANCELLED). This is the output entity of the attribution process.
 * <p>
 * Maps to the {@code registrations} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class Registration {
    /** Unique identifier of the registration. Auto-generated primary key. */
    private int id;

    /** Identifier of the campaign this registration belongs to. Foreign key to the campaigns table. */
    private int campaignId;

    /** Identifier of the student who was registered. Foreign key to the users table. */
    private int studentId;

    /** Identifier of the session slot the student was assigned to. Foreign key to the session_slots table. */
    private int sessionId;

    /**
     * The rank order of the choice that produced this registration, or null if
     * the assignment was not based on a student choice (e.g., forced assignment).
     * A lower value indicates the student got one of their higher preferences.
     */
    private Integer sourceChoiceRank;

    /**
     * Current status of the registration. One of:
     * ALLOCATED  — student successfully assigned to the session;
     * WAITLIST   — student placed on a waiting list;
     * REJECTED   — student could not be assigned;
     * CANCELLED  — student or admin cancelled the registration.
     */
    private String status;

    /**
     * Default no-argument constructor.
     * Creates an empty Registration instance. Fields must be set via setters before use.
     */
    public Registration() {
    }

    /**
     * Constructs a Registration with all fields.
     *
     * @param id               the unique identifier of the registration
     * @param campaignId       the campaign this registration belongs to
     * @param studentId        the student being registered
     * @param sessionId        the session slot assigned to the student
     * @param sourceChoiceRank the rank of the originating choice, or null
     * @param status           the registration status
     */
    public Registration(int id, int campaignId, int studentId, int sessionId, Integer sourceChoiceRank, String status) {
        this.id = id;
        this.campaignId = campaignId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.sourceChoiceRank = sourceChoiceRank;
        this.status = status;
    }

    /**
     * Returns the unique identifier of the registration.
     *
     * @return the registration ID
     */
    public int getId() {
         return id; 
    }

    /**
     * Sets the unique identifier of the registration.
     *
     * @param id the registration ID to set
     */
    public void setId(int id) {
         this.id = id; 
    }

    /**
     * Returns the campaign identifier this registration belongs to.
     *
     * @return the campaign ID
     */
    public int getCampaignId() {
         return campaignId; 
    }

    /**
     * Sets the campaign identifier this registration belongs to.
     *
     * @param campaignId the campaign ID to set
     */
    public void setCampaignId(int campaignId) {
         this.campaignId = campaignId; 
    }

    /**
     * Returns the student identifier who was registered.
     *
     * @return the student ID
     */
    public int getStudentId() {
         return studentId; 
    }

    /**
     * Sets the student identifier who was registered.
     *
     * @param studentId the student ID to set
     */
    public void setStudentId(int studentId) {
         this.studentId = studentId; 
    }

    /**
     * Returns the session slot identifier the student was assigned to.
     *
     * @return the session slot ID
     */
    public int getSessionId() { return sessionId; }

    /**
     * Sets the session slot identifier the student was assigned to.
     *
     * @param sessionId the session slot ID to set
     */
    public void setSessionId(int sessionId) {
         this.sessionId = sessionId; 
    }

    /**
     * Returns the rank of the student's choice that originated this registration.
     *
     * @return the source choice rank, or null if not applicable
     */
    public Integer getSourceChoiceRank() {
         return sourceChoiceRank; 
    }

    /**
     * Sets the rank of the student's choice that originated this registration.
     *
     * @param sourceChoiceRank the source choice rank to set, or null
     */
    public void setSourceChoiceRank(Integer sourceChoiceRank) {
         this.sourceChoiceRank = sourceChoiceRank; 
    }

    /**
     * Returns the current status of the registration.
     *
     * @return the status (ALLOCATED, WAITLIST, REJECTED, or CANCELLED)
     */
    public String getStatus() { 
        return status; 
}

    /**
     * Sets the current status of the registration.
     *
     * @param status the status to set (ALLOCATED, WAITLIST, REJECTED, or CANCELLED)
     */
    public void setStatus(String status) {
         this.status = status; 
    }
}
