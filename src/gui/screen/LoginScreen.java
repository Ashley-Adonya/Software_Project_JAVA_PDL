package gui.screen;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import components.ImageComp;
import components.Label;
import main.BaseComp;
import main.BaseWindow;
import gui.components.PrimaryButton;
import gui.components.ReusableLabeledInput;
import gui.components.SurfaceCard;
import gui.navigation.AppScreen;
import model.User;
import service.AuthService;

public class LoginScreen implements AppScreen {
    private static final Color PAGE_BG = new Color(14, 18, 26);
    private static final Color CARD_BG = new Color(22, 28, 39);
    private static final Color CARD_BORDER = new Color(48, 60, 82);
    private static final Color SUBTITLE_COLOR = new Color(151, 166, 194);
    private static final Color MESSAGE_ERROR = new Color(198, 55, 45);
    private static final Color MESSAGE_SUCCESS = new Color(21, 137, 93);

    private final BaseWindow window;
    private final AuthService authService;
    private final Consumer<User> onAuthenticated;

    private final SurfaceCard card;
    private final ImageComp appLogo;
    private final Label appSubtitle;

    private final Label cardTitle;
    private final Label cardSubtitle;
    private final ReusableLabeledInput emailInput;
    private final ReusableLabeledInput passwordInput;
    private final Label feedbackLabel;
    private final PrimaryButton loginButton;

    public LoginScreen(BaseWindow window, Consumer<User> onAuthenticated) {
        this.window = window;
        this.authService = new AuthService();
        this.onAuthenticated = onAuthenticated;

        this.appLogo = new ImageComp("assets/logo-esigelec.png", 0, 0, 260, 96);

        this.appSubtitle = new Label("Systeme de gestion des inscriptions", 0, 0, 360, 20);
        this.appSubtitle.setFont(new Font("Dialog", Font.PLAIN, 13));
        this.appSubtitle.setColor(SUBTITLE_COLOR);

        this.card = new SurfaceCard(0, 0, 390, 320, CARD_BG, CARD_BORDER, 14);
        this.cardTitle = new Label("Connexion", 0, 0, 180, 24);
        this.cardTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        this.cardTitle.setColor(new Color(235, 241, 255));

        this.cardSubtitle = new Label("Accedez a votre espace personnel", 0, 0, 290, 18);
        this.cardSubtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.cardSubtitle.setColor(new Color(151, 166, 194));

        this.emailInput = new ReusableLabeledInput("Email", "email@example.com", 0, 0, 320, 62);
        this.passwordInput = new ReusableLabeledInput("Mot de passe", "********", 0, 0, 320, 62, true);

        this.feedbackLabel = new Label("", 0, 0, 320, 18);
        this.feedbackLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.feedbackLabel.setColor(MESSAGE_ERROR);

        this.loginButton = new PrimaryButton("Se connecter", 0, 0, 320, 38, this::handleLogin);

        card.addChild(cardTitle);
        card.addChild(cardSubtitle);
        card.addChild(emailInput);
        card.addChild(passwordInput);
        card.addChild(feedbackLabel);
        card.addChild(loginButton);
    }

    @Override
    public void mount() {
        BaseComp content = window.getContent();
        content.setStyleManager(new style.StyleManager(PAGE_BG, 0, content.getWidth(), content.getHeight(), 0, 0, "absolute"));

        clearChildren(content);
        content.addChild(appLogo);
        content.addChild(appSubtitle);
        content.addChild(card);

        onResize();
    }

    @Override
    public void onResize() {
        layoutResponsive();
    }

    private void clearChildren(BaseComp parent) {
        List<BaseComp> snapshot = new ArrayList<>(parent.getChildrenList());
        for (BaseComp child : snapshot) {
            parent.removeChild(child);
        }
    }

    private void layoutResponsive() {
        BaseComp content = window.getContent();
        int width = content.getWidth();
        int height = content.getHeight();

        int horizontalPadding = width < 560 ? 20 : 30;
        int cardWidth = Math.min(390, Math.max(310, width - (horizontalPadding * 2)));
        int cardHeight = 320;

        int headerTop = Math.max(24, (height / 2) - 260);
        int logoWidth = width < 560 ? 210 : 260;
        int logoHeight = width < 560 ? 78 : 96;
        int logoX = (width - logoWidth) / 2;
        appLogo.setBounds(logoX, headerTop, logoWidth, logoHeight);

        appSubtitle.setBounds((width - 360) / 2, headerTop + logoHeight + 8, 360, 20);

        int cardX = (width - cardWidth) / 2;
        int cardY = headerTop + logoHeight + 46;
        card.setBounds(cardX, cardY, cardWidth, cardHeight);

        int cardInnerX = 20;
        int fieldWidth = cardWidth - 40;
        cardTitle.setBounds(cardInnerX, 18, fieldWidth, 24);
        cardSubtitle.setBounds(cardInnerX, 42, fieldWidth, 18);

        emailInput.setBounds(cardInnerX, 78, fieldWidth, 64);
        passwordInput.setBounds(cardInnerX, 148, fieldWidth, 64);
        feedbackLabel.setBounds(cardInnerX, 220, fieldWidth, 18);
        loginButton.setBounds(cardInnerX, 248, fieldWidth, 40);

        window.invalidateAll();
        window.requestRenderIfNeeded();
    }

    private void handleLogin() {
        String login = emailInput.getValue();
        String password = passwordInput.getValue();

        if (login.isBlank() || password.isBlank()) {
            setFeedback("Veuillez renseigner email et mot de passe.", false);
            return;
        }

        User user = authService.login(login, password);
        if (user == null) {
            setFeedback("Identifiants invalides.", false);
            return;
        }

        String role = user.getRole() == null ? "UNKNOWN" : user.getRole().toUpperCase();
        System.out.println("[LoginScreen] Auth OK user=" + user.getLogin() + " role=" + role);
        if (onAuthenticated == null) {
            setFeedback("Erreur interne: routeur non disponible.", false);
            return;
        }

        try {
            onAuthenticated.accept(user);
        } catch (Throwable t) {
            t.printStackTrace();
            setFeedback("Connexion OK mais redirection en erreur. Voir console.", false);
        }
    }

    private void setFeedback(String message, boolean success) {
        feedbackLabel.setColor(success ? MESSAGE_SUCCESS : MESSAGE_ERROR);
        feedbackLabel.setText(message);
        feedbackLabel.invalidate();
    }

}


