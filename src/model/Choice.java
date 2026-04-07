package model;

public class Choice {
    private int id;
    private int campaignId;
    private int studentId;
    private int sessionId;
    private int rankOrder;

    public Choice() {
    }

    public Choice(int id, int campaignId, int studentId, int sessionId, int rankOrder) {
        this.id = id;
        this.campaignId = campaignId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.rankOrder = rankOrder;
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
         this.studentId = studentId; }
    public int getSessionId() {
         return sessionId; 
    }
    public void setSessionId(int sessionId) {
         this.sessionId = sessionId; 
    }
    public int getRankOrder() {
         return rankOrder; 
    }
    public void setRankOrder(int rankOrder) {
         this.rankOrder = rankOrder; 
    }
}
