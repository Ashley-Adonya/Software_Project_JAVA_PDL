package model;

public class Registration {
    private int id;
    private int campaignId;
    private int studentId;
    private int sessionId;
    private Integer sourceChoiceRank;
    private String status;

    public Registration() {
    }

    public Registration(int id, int campaignId, int studentId, int sessionId, Integer sourceChoiceRank, String status) {
        this.id = id;
        this.campaignId = campaignId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.sourceChoiceRank = sourceChoiceRank;
        this.status = status;
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
    public int getStudentId() {
         return studentId; 
    }
    public void setStudentId(int studentId) {
         this.studentId = studentId; 
    }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) {
         this.sessionId = sessionId; 
    }
    public Integer getSourceChoiceRank() {
         return sourceChoiceRank; 
    }
    public void setSourceChoiceRank(Integer sourceChoiceRank) {
         this.sourceChoiceRank = sourceChoiceRank; 
    }
    public String getStatus() { 
        return status; 
}
    public void setStatus(String status) {
         this.status = status; 
    }
}
