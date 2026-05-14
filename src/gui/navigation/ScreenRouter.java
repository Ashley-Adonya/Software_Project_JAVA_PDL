package gui.navigation;

import gui.screen.AdminDashboardScreen;
import gui.screen.LoginScreen;
import gui.screen.StudentDashboardScreen;
import main.BaseWindow;
import model.User;
import service.AuthService;
import java.util.Locale;

/**
 * Gestionnaire de navigation centralisé pour l'application.
 * Assure la transition entre les différents écrans (Login, AdminDashboard, StudentDashboard)
 * en fonction du rôle de l'utilisateur authentifié.
 * Gère l'initialisation de la fenêtre principale et les appels de redimensionnement.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class ScreenRouter {
    private final BaseWindow window;
    private final AuthService authService;
    private AppScreen current;

    public ScreenRouter(BaseWindow window) {
        this.window = window;
        this.authService = new AuthService();
        this.window.addResizeListener(() -> {
            if (current != null) {
                current.onResize();
            }
        });
    }

    public void showLogin() {
        System.out.println("[ScreenRouter] showLogin()");
        setCurrent(new LoginScreen(window, this::onAuthenticated));
    }

    private void onAuthenticated(User user) {
        String role = normalizeRole(user);
        System.out.println("[ScreenRouter] onAuthenticated user="
                + (user == null ? "null" : user.getLogin())
                + " role=" + role
                + " isAdmin=" + authService.isAdmin(user)
                + " isStudent=" + authService.isStudent(user));
        if (authService.isAdmin(user)) {
            System.out.println("[ScreenRouter] redirect -> AdminDashboardScreen");
            setCurrent(new AdminDashboardScreen(window, user, this::showLogin));
            return;
        }
        if (authService.isStudent(user)) {
            System.out.println("[ScreenRouter] redirect -> StudentDashboardScreen");
            setCurrent(new StudentDashboardScreen(window, user, this::showLogin));
            return;
        }
        System.err.println("[ScreenRouter] Role non reconnu: '" + role + "', redirection login.");
        showLogin();
    }

    private String normalizeRole(User user) {
        if (user == null || user.getRole() == null) {
            return "";
        }
        return user.getRole().trim().toUpperCase(Locale.ROOT);
    }

    private void setCurrent(AppScreen screen) {
        float start = System.currentTimeMillis() / 1000.0f;
        try {
            System.out.println("[ScreenRouter] setCurrent -> " + (screen == null ? "null" : screen.getClass().getName()));
            this.current = screen;
            this.current.mount();
            System.out.println("[ScreenRouter] setCurrent OK");
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("[ScreenRouter] Echec changement d'ecran vers "
                    + (screen == null ? "null" : screen.getClass().getName()));
            this.current = null;
            showLogin();
        }
        float end = System.currentTimeMillis() / 1000.0f;
        System.out.println("[time=" + (end - start) + "] ScreenRouter.setCurrent: screen=" + (screen == null ? "null" : screen.getClass().getName()));
    }
}
