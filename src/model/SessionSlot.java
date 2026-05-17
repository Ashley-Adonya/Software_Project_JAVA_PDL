package model;

/**
 * Represents a time slot session for a dominant (specialty) presentation.
 * Each session slot defines when and where a specific dominante presentation
 * takes place, including the room, date, start and end times (in minutes from
 * midnight), and the maximum number of students that can attend. Sessions are
 * associated with a campaign and a dominante, and are created by an administrator.
 * Students submit ranked choices for these slots during the registration campaign.
 * <p>
 * Maps to the {@code session_slots} database table.
 * </p>
 *
 * @author Sado Adonya &amp; VIEYRA Kolawole
 * @version 1.0
 */
public class SessionSlot {
    /** Unique identifier of the session slot. Auto-generated primary key. */
    private int id;

    /** Identifier of the campaign this session belongs to. Foreign key to the campaigns table. */
    private int campaignId;

    /** Identifier of the dominante (specialty) being presented. Foreign key to the dominantes table. */
    private int dominanteId;

    /** Title or summary of the presentation session (e.g. "Cybersecurity Intro Session 1"). */
    private String title;

    /** Room or location where the session takes place (e.g. "A101", "Amphi B"). */
    private String room;

    /** Calendar date of the session in yyyy-MM-dd format. */
    private String sessionDate;

    /** Start time expressed as minutes elapsed since midnight (e.g. 480 = 08:00). */
    private int startMinute;

    /** End time expressed as minutes elapsed since midnight (e.g. 540 = 09:00). Must be greater than startMinute. */
    private int endMinute;

    /** Maximum number of students that can be assigned to this session. */
    private int capacity;

    /** Identifier of the administrator user who created this session slot. Foreign key to the users table. */
    private int createdBy;

    /**
     * Default no-argument constructor.
     * Creates an empty SessionSlot instance. Fields must be set via setters before use.
     */
    public SessionSlot() {
    }

    /**
     * Constructs a SessionSlot with all fields.
     *
     * @param id          the unique identifier of the session slot
     * @param campaignId  the campaign this session belongs to
     * @param dominanteId the dominante (specialty) being presented
     * @param title       the title of the session
     * @param room        the room/location of the session
     * @param sessionDate the date of the session
     * @param startMinute the start time in minutes from midnight
     * @param endMinute   the end time in minutes from midnight
     * @param capacity    the maximum number of attendees
     * @param createdBy   the administrator who created this session
     */
    public SessionSlot(int id, int campaignId, int dominanteId, String title, String room, String sessionDate,
            int startMinute, int endMinute, int capacity, int createdBy) {
        this.id = id;
        this.campaignId = campaignId;
        this.dominanteId = dominanteId;
        this.title = title;
        this.room = room;
        this.sessionDate = sessionDate;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.capacity = capacity;
        this.createdBy = createdBy;
    }

    /**
     * Returns the unique identifier of the session slot.
     *
     * @return the session slot ID
     */
    public int getId() {
         return id; 
    }

    /**
     * Sets the unique identifier of the session slot.
     *
     * @param id the session slot ID to set
     */
    public void setId(int id) {
         this.id = id; 
    }

    /**
     * Returns the campaign identifier this session belongs to.
     *
     * @return the campaign ID
     */
    public int getCampaignId() {
         return campaignId; 
    }

    /**
     * Sets the campaign identifier this session belongs to.
     *
     * @param campaignId the campaign ID to set
     */
    public void setCampaignId(int campaignId) {
         this.campaignId = campaignId; 
    }

    /**
     * Returns the dominante identifier being presented in this session.
     *
     * @return the dominante ID
     */
    public int getDominanteId() {
         return dominanteId; 
    }

    /**
     * Sets the dominante identifier being presented in this session.
     *
     * @param dominanteId the dominante ID to set
     */
    public void setDominanteId(int dominanteId) {
         this.dominanteId = dominanteId; 
    }

    /**
     * Returns the title of the session.
     *
     * @return the session title
     */
    public String getTitle() {
         return title; 
    }

    /**
     * Sets the title of the session.
     *
     * @param title the session title to set
     */
    public void setTitle(String title) {
         this.title = title; 
    }

    /**
     * Returns the room/location of the session.
     *
     * @return the room name
     */
    public String getRoom() {
         return room; 
    }

    /**
     * Sets the room/location of the session.
     *
     * @param room the room name to set
     */
    public void setRoom(String room) {
         this.room = room; 
    }

    /**
     * Returns the calendar date of the session.
     *
     * @return the session date as a string (yyyy-MM-dd)
     */
    public String getSessionDate() {
         return sessionDate; 
    }

    /**
     * Sets the calendar date of the session.
     *
     * @param sessionDate the session date to set
     */
    public void setSessionDate(String sessionDate) {
         this.sessionDate = sessionDate; }

    /**
     * Returns the start time of the session in minutes from midnight.
     *
     * @return the start minute (e.g. 480 for 08:00)
     */
    public int getStartMinute() { return startMinute; 

    }

    /**
     * Sets the start time of the session in minutes from midnight.
     *
     * @param startMinute the start minute to set (e.g. 480 for 08:00)
     */
    public void setStartMinute(int startMinute) {
         this.startMinute = startMinute; 
    }

    /**
     * Returns the end time of the session in minutes from midnight.
     *
     * @return the end minute (e.g. 540 for 09:00)
     */
    public int getEndMinute() {
         return endMinute; 
    }

    /**
     * Sets the end time of the session in minutes from midnight.
     *
     * @param endMinute the end minute to set (e.g. 540 for 09:00)
     */
    public void setEndMinute(int endMinute) {
         this.endMinute = endMinute; 
    }

    /**
     * Returns the maximum capacity of the session.
     *
     * @return the maximum number of attendees
     */
    public int getCapacity() {
         return capacity; 
    }

    /**
     * Sets the maximum capacity of the session.
     *
     * @param capacity the maximum number of attendees to set
     */
    public void setCapacity(int capacity) {
         this.capacity = capacity; 
    }

    /**
     * Returns the administrator ID who created this session slot.
     *
     * @return the creator user ID
     */
    public int getCreatedBy() {
         return createdBy; 
    }

    /**
     * Sets the administrator ID who created this session slot.
     *
     * @param createdBy the creator user ID to set
     */
    public void setCreatedBy(int createdBy) {
         this.createdBy = createdBy; 
    }
}
