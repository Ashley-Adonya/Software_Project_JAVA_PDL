package util;

import dao.CampaignDAO;
import model.Campaign;
import java.util.List;

public class SetCampaignToPreparation {
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