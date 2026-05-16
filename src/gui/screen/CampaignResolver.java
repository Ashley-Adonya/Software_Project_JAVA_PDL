package gui.screen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import model.Campaign;
import service.CampaignService;

/**
 * Résolveur de campagne active.
 * Logique métier : sélection de la campagne prioritaire (OPEN > PREPARATION > CLOSED)
 * en fonction de la promotion de l'utilisateur.
 */
public class CampaignResolver {
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CampaignService campaignService;
    private final String userPromo;

    public CampaignResolver(CampaignService campaignService, String userPromo) {
        this.campaignService = campaignService;
        this.userPromo = userPromo;
    }

    /**
     * Résout la campagne active en privilégiant OPEN, puis PREPARATION, puis CLOSED.
     */
    public Campaign resolveActiveCampaign() {
        Campaign open = selectBest(campaignService.getCampaignsByStatus("OPEN"));
        if (open != null) return open;
        Campaign prep = selectBest(campaignService.getCampaignsByStatus("PREPARATION"));
        return prep != null ? prep : selectBest(campaignService.getCampaignsByStatus("CLOSED"));
    }

    private Campaign selectBest(List<Campaign> campaigns) {
        if (campaigns == null || campaigns.isEmpty()) return null;
        Campaign best = campaigns.stream()
            .filter(c -> userPromo != null && userPromo.equalsIgnoreCase(safe(c.getPromo())))
            .max(Comparator.comparing(this::dateSort).thenComparingInt(Campaign::getId)).orElse(null);
        return best != null ? best : campaigns.stream()
            .max(Comparator.comparing(this::dateSort).thenComparingInt(Campaign::getId)).orElse(campaigns.get(0));
    }

    private LocalDate dateSort(Campaign c) {
        if (c == null || c.getRegistrationDay() == null || c.getRegistrationDay().isBlank()) return LocalDate.MIN;
        try { return LocalDate.parse(c.getRegistrationDay(), FR_DATE); } catch (Exception e) { return LocalDate.MIN; }
    }

    private String safe(String v) { return v == null || v.isBlank() ? "-" : v; }
}