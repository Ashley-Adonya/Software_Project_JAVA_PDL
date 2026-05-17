package util;

import dao.CampaignDAO;
import model.Campaign;
import java.util.List;

/**
 * Utility entry point that programmatically transitions a specific campaign
 * ("Campagne ING3 2026") to the PREPARATION status.
 * <p>
 * This class is intended for one-shot administrative operations or integration
 * testing, bypassing the normal GUI workflow. It directly uses {@link CampaignDAO}
 * to query and update the campaign in the database.
 * </p>
 */
public class SetCampaignToPreparation {
    /**
     * Locates the campaign named "Campagne ING3 2026" for the promo "ING3" and
     * updates its status to "PREPARATION".
     * <p>
     * Prints the result of the update to stdout, or a "not found" message if no
     * matching campaign exists.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        CampaignDAO dao = new CampaignDAO();
        List<Campaign> campaigns = dao.findByPromo("ING3");
        for (Campaign c : campaigns) {
            if (c.getName().equals("Campagne ING3 2026")) {
                boolean ok = dao.updateStatus(c.getId(), "PREPARATION");
                System.out.println("Set campaign to PREPARATION: " + ok);
                return;
            }
        }
        System.out.println("Campaign not found");
    }
}