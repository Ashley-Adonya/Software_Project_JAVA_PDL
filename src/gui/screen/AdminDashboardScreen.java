package gui.screen;

import main.BaseWindow;
import model.User;
import gui.navigation.AppScreen;

/**
 * Délégateur léger pour l'écran du tableau de bord administrateur.
 * Implémente le contrat d'AppScreen en déléguant toute la logique métier et l'interface utilisateur
 * à AdminDashboardView afin de maintenir cette classe minimale (< 30 lignes) et maintenable.
 * 
 * Responsabilités :
 * - Initialisation de la vue avec la fenêtre principale et l'utilisateur authentifié
 * - Délégage du cycle de vie (mount/onResize) à AdminDashboardView
 * 
 * @author PDL Application
 * @version 1.0
 */
public class AdminDashboardScreen implements AppScreen {
    private final AdminDashboardView view;

    public AdminDashboardScreen(BaseWindow window, User user, Runnable onLogout) {
        this.view = new AdminDashboardView(window, user);
    }

    @Override
    public void mount() {
        view.mount();
    }

    @Override
    public void onResize() {
        view.onResize();
    }
}
