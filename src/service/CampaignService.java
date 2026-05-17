package service;

import dao.CampaignDAO;
import model.Campaign;
import model.User;
import java.util.List;

/**
 * Service orchestrating the lifecycle of attribution campaigns.
 * Implements a finite state machine to ensure campaigns follow coherent steps
 * (PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED, ARCHIVED).
 * Also provides campaign configuration capabilities.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class CampaignService {
    private final CampaignDAO campaignDAO;
    private final User currentUser;

    public CampaignService(User currentUser) {
        this.campaignDAO = new CampaignDAO();
        this.currentUser = currentUser;
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
        // Set createdBy from current user
        if (currentUser != null) {
            campaign.setCreatedBy(currentUser.getId());
        }
        int id = campaignDAO.create(campaign);
        if (id > 0) {
            CacheManager.invalidatePrefix("campaign:");
            CacheManager.invalidatePrefix("stats:");
        }
        return id;
    }

    public ServiceResult changeStatus(int campaignId, String nextStatus) {
        Campaign c = campaignDAO.findById(campaignId);
        if (c == null) {
            return ServiceResult.fail("Campagne introuvable");
        }

        if (!isAllowedTransition(c.getStatus(), nextStatus)) {
            return ServiceResult.fail("Transition interdite: " + c.getStatus() + " -> " + nextStatus);
        }

        boolean ok = campaignDAO.updateStatus(campaignId, nextStatus);
        if (!ok) {
            return ServiceResult.fail("Mise a jour du statut impossible");
        }
        CacheManager.invalidatePrefix("campaign:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Statut mis a jour: " + nextStatus);
    }

    /**
     * Checks if a status transition is allowed according to the campaign lifecycle.
     * Forward transitions follow the standard workflow:
     * PREPARATION -> OPEN -> CLOSED -> PROCESSING -> VALIDATED -> ARCHIVED
     * Backward transitions to PREPARATION are always allowed to reset a campaign.
     * 
     * @param current current status
     * @param nextStatus desired next status
     * @return true if the transition is allowed, false otherwise
     */
    public boolean isAllowedTransition(String current, String nextStatus) {
        if (current == null || nextStatus == null) {
            return false;
        }
        if (current.equals(nextStatus)) {
            return true;
        }
        // Allow going back to PREPARATION from any status
        if ("PREPARATION".equals(nextStatus)) {
            return true;
        }
        // Forward transitions
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

    public boolean updateSettings(int campaignId, String name, String registrationDay, String startDate, String endDate, int maxChoices) {
        if (maxChoices <= 0) {
            return false;
        }
        boolean ok = campaignDAO.updateSettings(campaignId, name, registrationDay, startDate, endDate, maxChoices);
        if (ok) {
            CacheManager.invalidatePrefix("campaign:");
            CacheManager.invalidatePrefix("stats:");
        }
        return ok;
    }

    public Campaign getCampaign(int id) {
        return CacheManager.getOrLoad("campaign:id:" + id, () -> campaignDAO.findById(id));
    }

    public List<Campaign> getCampaignsByPromo(String promo) {
        return CacheManager.getOrLoad("campaign:promo:" + safe(promo), () -> campaignDAO.findByPromo(promo));
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return CacheManager.getOrLoad("campaign:status:" + safe(status), () -> campaignDAO.findByStatus(status));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
