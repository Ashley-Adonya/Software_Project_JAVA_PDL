package model;

/**
 * Represents a ranked session choice submitted by a student during a campaign.
 * Each choice links a student to a specific session slot with a preference rank
 * (rankOrder from 1 to N, where 1 is the highest preference). These choices are
 * the primary input for the attribution algorithm, which assigns students to
 * sessions based on their ranked preferences and available capacity.
 * <p>
 * Maps to the {@code choices} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class Choice {
    /** Unique identifier of the choice. Auto-generated primary key. */
    private int id;

    /** Identifier of the campaign this choice belongs to. Foreign key to the campaigns table. */
    private int campaignId;

    /** Identifier of the student who made this choice. Foreign key to the users table. */
    private int studentId;

    /** Identifier of the session slot being chosen. Foreign key to the session_slots table. */
    private int sessionId;

    /** Preference rank order (1-based). Lower values indicate higher preference. Must be unique per student per campaign. */
    private int rankOrder;

    /**
     * Default no-argument constructor.
     * Creates an empty Choice instance. Fields must be set via setters before use.
     */
    public Choice() {
    }

    /**
     * Constructs a Choice with all fields.
     *
     * @param id         the unique identifier of the choice
     * @param campaignId the campaign this choice belongs to
     * @param studentId  the student who made the choice
     * @param sessionId  the session slot being chosen
     * @param rankOrder  the preference rank (1 = highest)
     */
    public Choice(int id, int campaignId, int studentId, int sessionId, int rankOrder) {
        this.id = id;
        this.campaignId = campaignId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.rankOrder = rankOrder;
    }

    /**
     * Returns the unique identifier of the choice.
     *
     * @return the choice ID
     */
    public int getId() {
         return id; 
    }

    /**
     * Sets the unique identifier of the choice.
     *
     * @param id the choice ID to set
     */
    public void setId(int id) {
         this.id = id; 
    }

    /**
     * Returns the campaign identifier this choice belongs to.
     *
     * @return the campaign ID
     */
    public int getCampaignId() {
         return campaignId; 
    }

    /**
     * Sets the campaign identifier this choice belongs to.
     *
     * @param campaignId the campaign ID to set
     */
    public void setCampaignId(int campaignId) {
         this.campaignId = campaignId; 
    }

    /**
     * Returns the student identifier who made this choice.
     *
     * @return the student ID
     */
    public int getStudentId() {
         return studentId; 
    }

    /**
     * Sets the student identifier who made this choice.
     *
     * @param studentId the student ID to set
     */
    public void setStudentId(int studentId) {
         this.studentId = studentId; }

    /**
     * Returns the session slot identifier being chosen.
     *
     * @return the session slot ID
     */
    public int getSessionId() {
         return sessionId; 
    }

    /**
     * Sets the session slot identifier being chosen.
     *
     * @param sessionId the session slot ID to set
     */
    public void setSessionId(int sessionId) {
         this.sessionId = sessionId; 
    }

    /**
     * Returns the preference rank order of this choice.
     *
     * @return the rank order (1 = highest preference)
     */
    public int getRankOrder() {
         return rankOrder; 
    }

    /**
     * Sets the preference rank order of this choice.
     *
     * @param rankOrder the rank order to set (1 = highest preference)
     */
    public void setRankOrder(int rankOrder) {
         this.rankOrder = rankOrder; 
    }
}
