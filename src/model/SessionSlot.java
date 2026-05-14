package model;

/**
 * Représente un créneau temporel de présentation d'une dominante.
 * Contient les détails de lieu (salle), d'horaire (date, début, fin en minutes),
 * de capacité d'accueil, et de lien vers la campagne et dominante concernées.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class SessionSlot {
    private int id;
    private int campaignId;
    private int dominanteId;
    private String title;
    private String room;
    private String sessionDate;
    private int startMinute;
    private int endMinute;
    private int capacity;
    private int createdBy;

    public SessionSlot() {
    }

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

    public int getId() {
         return id; 
    }
    public void setId(int id) {
         this.id = id; 
    }
    public int getCampaignId() {
         return campaignId; 
    }
    public void setCampaignId(int campaignId) {
         this.campaignId = campaignId; 
    }
    public int getDominanteId() {
         return dominanteId; 
    }
    public void setDominanteId(int dominanteId) {
         this.dominanteId = dominanteId; 
    }
    public String getTitle() {
         return title; 
    }
    public void setTitle(String title) {
         this.title = title; 
    }
    public String getRoom() {
         return room; 
    }
    public void setRoom(String room) {
         this.room = room; 
    }
    public String getSessionDate() {
         return sessionDate; 
    }
    public void setSessionDate(String sessionDate) {
         this.sessionDate = sessionDate; }
    public int getStartMinute() { return startMinute; 

    }
    public void setStartMinute(int startMinute) {
         this.startMinute = startMinute; 
    }
    public int getEndMinute() {
         return endMinute; 
    }
    public void setEndMinute(int endMinute) {
         this.endMinute = endMinute; 
    }
    public int getCapacity() {
         return capacity; 
    }
    public void setCapacity(int capacity) {
         this.capacity = capacity; 
    }
    public int getCreatedBy() {
         return createdBy; 
    }
    public void setCreatedBy(int createdBy) {
         this.createdBy = createdBy; 
    }
}
