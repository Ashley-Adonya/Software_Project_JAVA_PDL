package gui.screen.components;

import java.awt.Font;
import java.util.function.Consumer;

import components.Label;
import gui.components.PrimaryButton;
import gui.components.ReusableLabeledInput;
import gui.components.SurfaceCard;
import main.BaseComp;
import main.BaseWindow;
import model.Campaign;

/**
 * Composant de formulaire de configuration de campagne.
 * Permet l'affichage et la modification des paramètres de campagne en cours
 * (nom, dates, limite de choix) avec validation client et retours utilisateur.
 * 
 * Responsabilités :
 * - Rendu du formulaire de saisie avec champs préremplis
 * - Validation des données avant submission
 * - Callback de sauvegarde pour intégration métier
 * - Affichage des messages de retour (succès/erreur)
 * - Gestion du redimensionnement
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class CampaignFormComponent {
    private final BaseComp root;
    private final SurfaceCard formCard;
    private final ReusableLabeledInput campaignNameInput;
    private final ReusableLabeledInput registrationDateInput;
    private final ReusableLabeledInput maxChoicesInput;
    private final ReusableLabeledInput startDateInput;
    private final ReusableLabeledInput endDateInput;
    private final PrimaryButton saveCampaignButton;
    private final Label feedbackLabel;

    private Consumer<Campaign> onSave = c -> {};

    public CampaignFormComponent(BaseWindow window) {
        this.root = new BaseComp(null);
        this.formCard = new SurfaceCard(0, 0, 100, 310, new java.awt.Color(22,28,39), new java.awt.Color(48,60,82), 12);

        this.campaignNameInput = new ReusableLabeledInput("Nom de la campagne", "", 16, 42, 380, 62);
        this.registrationDateInput = new ReusableLabeledInput("Date inscript (dd/MM/yyyy)", "", 16, 108, 200, 62);
        this.maxChoicesInput = new ReusableLabeledInput("Nombre max de choix", "5", 232, 108, 164, 62);
        this.startDateInput = new ReusableLabeledInput("Date de debut (yyyy-MM-dd)", "", 16, 174, 200, 62);
        this.endDateInput = new ReusableLabeledInput("Date de fin (yyyy-MM-dd)", "", 232, 174, 200, 62);

        this.saveCampaignButton = new PrimaryButton("Enregistrer", 16, 250, 130, 34, () -> {
            Campaign c = new Campaign();
            c.setName(campaignNameInput.getValue());
            c.setRegistrationDay(registrationDateInput.getValue());
            try { c.setMaxChoices(Integer.parseInt(maxChoicesInput.getValue())); } catch (Exception e) { c.setMaxChoices(0); }
            c.setStartDate(startDateInput.getValue());
            c.setEndDate(endDateInput.getValue());
            onSave.accept(c);
        });

        this.feedbackLabel = new Label("", 16, 288, 420, 16);
        this.feedbackLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        formCard.addChild(campaignNameInput);
        formCard.addChild(registrationDateInput);
        formCard.addChild(maxChoicesInput);
        formCard.addChild(startDateInput);
        formCard.addChild(endDateInput);
        formCard.addChild(saveCampaignButton);
        formCard.addChild(feedbackLabel);

        root.addChild(formCard);
    }

    public BaseComp getRoot() { return root; }

    public void onSave(Consumer<Campaign> cb) { this.onSave = cb; }

    public void refreshFrom(Campaign campaign) {
        if (campaign == null) {
            campaignNameInput.setValue("");
            registrationDateInput.setValue("");
            maxChoicesInput.setValue("5");
            startDateInput.setValue("");
            endDateInput.setValue("");
            feedbackLabel.setText("Aucune campagne active.");
            return;
        }
        campaignNameInput.setValue(campaign.getName());
        registrationDateInput.setValue(campaign.getRegistrationDay());
        maxChoicesInput.setValue(String.valueOf(campaign.getMaxChoices()));
        startDateInput.setValue(campaign.getStartDate());
        endDateInput.setValue(campaign.getEndDate());
        feedbackLabel.setText("Statut actuel: " + campaign.getStatus());
    }

    public void setFeedback(String msg) { feedbackLabel.setText(msg == null ? "" : msg); }

    public void onResize(int mainW) {
        formCard.setBounds(0, 0, mainW, 310);
        campaignNameInput.setBounds(16, 42, mainW - 32, 62);
        registrationDateInput.setBounds(16, 108, 220, 62);
        maxChoicesInput.setBounds(252, 108, 180, 62);
        startDateInput.setBounds(16, 174, 220, 62);
        endDateInput.setBounds(252, 174, 180, 62);
        saveCampaignButton.setBounds(16, 250, 130, 34);
        feedbackLabel.setBounds(16, 288, mainW - 32, 16);
    }
}
