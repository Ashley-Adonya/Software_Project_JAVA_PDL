package service;

import dao.CampaignDAO;
import model.Campaign;
import java.util.List;

/**
 * Service orchestrant le cycle de vie des campagnes d'attribution.
 * 
 * Nous avons implémenté une machine à états finis pour garantir que les campagnes
 * passent par des étapes cohérentes (PREPARATION, OPEN, CLOSED, PROCESSING, VALIDATED, ARCHIVED).
 * Ce service permet également la configuration des paramètres de campagne.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
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

        String current = c.getStatus();
        if (!isAllowedTransition(current, nextStatus)) {
            return ServiceResult.fail("Transition interdite: " + current + " -> " + nextStatus);
        }

        boolean ok = campaignDAO.updateStatus(campaignId, nextStatus);
        if (!ok) {
            return ServiceResult.fail("Mise a jour du statut impossible");
        }
        CacheManager.invalidatePrefix("campaign:");
        CacheManager.invalidatePrefix("stats:");
        return ServiceResult.ok("Statut mis a jour: " + nextStatus);
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
