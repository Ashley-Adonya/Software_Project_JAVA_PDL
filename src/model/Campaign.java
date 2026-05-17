package model;

/**
 * Represents an attribution campaign for dominant presentation sessions.
 * A campaign defines temporal parameters, maximum number of allowed choices,
 * and the state (PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED, ARCHIVED) 
 * of the registration process.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class Campaign {
    private int id;
    private String name;
    private String promo;
    private String registrationDay;
    private String startDate;
    private String endDate;
    private int maxChoices;
    private String status;
    private int createdBy;
    private String openedAt;
    private String closedAt;
    private String processedAt;
    private String validatedAt;
    private String archivedAt;

    public Campaign() {
    }

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

    public int getId() { 
        return id; 
    }
    public void setId(int id) {
         this.id = id; 
    }
    public String getName() {
         return name; 
    }
    public void setName(String name) {
         this.name = name; }
    public String getPromo() {
         return promo; 
    }
    public void setPromo(String promo) {
         this.promo = promo; 
    }
    public String getRegistrationDay() {
         return registrationDay; 
    }
    public void setRegistrationDay(String registrationDay) {
         this.registrationDay = registrationDay; 
    }
    public String getStartDate() {
         return startDate; 
    }
    public void setStartDate(String startDate) {
         this.startDate = startDate; 
    }
    public String getEndDate() { 
        return endDate; 

    }
    public void setEndDate(String endDate) {
         this.endDate = endDate; 
    }
    public int getMaxChoices() {
         return maxChoices; 
    }
    public void setMaxChoices(int maxChoices) {
         this.maxChoices = maxChoices; 
    }
    public String getStatus() {
         return status; 
    }
    public void setStatus(String status) {
         this.status = status; 
    }
    public int getCreatedBy() {
         return createdBy; 
    }
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getOpenedAt() {
        return openedAt;
    }
    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }
    public String getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }
    public String getProcessedAt() {
        return processedAt;
    }
    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
    public String getValidatedAt() {
        return validatedAt;
    }
    public void setValidatedAt(String validatedAt) {
        this.validatedAt = validatedAt;
    }
    public String getArchivedAt() {
        return archivedAt;
    }
    public void setArchivedAt(String archivedAt) {
        this.archivedAt = archivedAt;
    }
}
