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
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class AdminDashboardScreen implements AppScreen {
    private final AdminDashboardView view;

    /**
     * Creates a new admin dashboard screen and initializes the underlying view.
     *
     * @param window   the main application window used for rendering and layout management
     * @param user     the authenticated admin user whose data and permissions will drive the dashboard content
     * @param onLogout a callback invoked when the user requests to log out; triggers navigation back to the login screen
     */
    public AdminDashboardScreen(BaseWindow window, User user, Runnable onLogout) {
        this.view = new AdminDashboardView(window, user, onLogout);
    }

    /**
     * Mounts the admin dashboard by delegating to the underlying AdminDashboardView.
     * This method is called by the ScreenRouter when this screen becomes the active screen.
     * It initializes all UI components, loads initial data, and triggers the first render.
     */
    @Override
    public void mount() {
        view.mount();
    }

    /**
     * Handles window resize events by delegating to the underlying AdminDashboardView.
     * Recalculates all component positions and dimensions to maintain a responsive layout.
     * This method is called automatically by the ScreenRouter via the window resize listener.
     */
    @Override
    public void onResize() {
        view.onResize();
    }
}
