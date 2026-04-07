package service;

import dao.CampaignDAO;
import model.Campaign;
import java.util.List;

public class CampaignService {
    private final CampaignDAO campaignDAO;

    public CampaignService() {
        this.campaignDAO = new CampaignDAO();
    }

    public int createCampaign(Campaign campaign) {
        if (campaign == null) {
            return -1;
        }
        if (campaign.getPromo() == null || campaign.getPromo().trim().isEmpty()) {
            return -1;
        }
        if (campaign.getMaxChoices() <= 0) {
            return -1;
        }
        if (campaign.getStatus() == null || campaign.getStatus().trim().isEmpty()) {
            campaign.setStatus("PREPARATION");
        }
        return campaignDAO.create(campaign);
    }

    public ServiceResult changeStatus(int campaignId, String nextStatus) {
        Campaign c = campaignDAO.findById(campaignId);
        if (c == null) {
            return ServiceResult.fail("Campagne introuvable");
        }

        String current = c.getStatus();
        if (!isAllowedTransition(current, nextStatus)) {
            return ServiceResult.fail("Transition interdite: " + current + " -> " + nextStatus);
        }

        boolean ok = campaignDAO.updateStatus(campaignId, nextStatus);
        if (!ok) {
            return ServiceResult.fail("Mise a jour du statut impossible");
        }
        return ServiceResult.ok("Statut mis a jour: " + nextStatus);
    }

    public boolean updateSettings(int campaignId, String name, String registrationDay, String startDate, String endDate, int maxChoices) {
        if (maxChoices <= 0) {
            return false;
        }
        return campaignDAO.updateSettings(campaignId, name, registrationDay, startDate, endDate, maxChoices);
    }

    public Campaign getCampaign(int id) {
        return campaignDAO.findById(id);
    }

    public List<Campaign> getCampaignsByPromo(String promo) {
        return campaignDAO.findByPromo(promo);
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return campaignDAO.findByStatus(status);
    }

    private boolean isAllowedTransition(String current, String nextStatus) {
        if (current == null || nextStatus == null) {
            return false;
        }
        if (current.equals(nextStatus)) {
            return true;
        }
        if ("PREPARATION".equals(current) && "OPEN".equals(nextStatus)) {
            return true;
        }
        if ("OPEN".equals(current) && "CLOSED".equals(nextStatus)) {
            return true;
        }
        if ("CLOSED".equals(current) && "PROCESSING".equals(nextStatus)) {
            return true;
        }
        if ("PROCESSING".equals(current) && "VALIDATED".equals(nextStatus)) {
            return true;
        }
        if ("VALIDATED".equals(current) && "ARCHIVED".equals(nextStatus)) {
            return true;
        }
        return false;
    }
}
