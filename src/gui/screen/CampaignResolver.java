package gui.screen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import model.Campaign;
import service.CampaignService;

/**
 * Resolves the currently active campaign based on the user's academic year (promo).
 * <p>
 * The resolution follows a strict priority order: campaigns in OPEN status are evaluated
 * first (registrations in progress), followed by PREPARATION (not yet open), and finally
 * CLOSED (registrations closed). Within each status group, the campaign whose promo matches
 * the user's promo is preferred. If no promo-matched campaign is found, the campaign with
 * the most recent registration date is selected. This class encapsulates the business logic
 * for determining which campaign should be displayed to a given administrator.
 * </p>
 */
public class CampaignResolver {
    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CampaignService campaignService;
    private final String userPromo;

    /**
     * Constructs a CampaignResolver with the required dependencies.
     *
     * @param campaignService the service providing access to campaign data and persistence
     * @param userPromo       the academic year (promo) of the current administrator,
     *                        used to prioritise campaigns matching the user's cohort
     */
    public CampaignResolver(CampaignService campaignService, String userPromo) {
        this.campaignService = campaignService;
        this.userPromo = userPromo;
    }

    /**
     * Resolves and returns the most relevant active campaign for the current user.
     * <p>
     * Campaigns are evaluated in the following priority order:
     * <ol>
     *   <li><b>OPEN</b> &ndash; registration is currently in progress</li>
     *   <li><b>PREPARATION</b> &ndash; registration is not yet open</li>
     *   <li><b>CLOSED</b> &ndash; registration has closed</li>
     * </ol>
     * Within each status group, the campaign whose promo matches the user's promo is
     * preferred. If no matching campaign exists for that promo, the campaign with the
     * most recent registration date (parsed in dd/MM/yyyy format) is selected. In the
     * worst case, the first campaign from the list is returned.
     * </p>
     *
     * @return the resolved active {@link Campaign}, or {@code null} if no campaigns exist
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