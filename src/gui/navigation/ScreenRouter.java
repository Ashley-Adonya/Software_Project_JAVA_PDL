package gui.navigation;

import gui.screen.AdminDashboardScreen;
import gui.screen.LoginScreen;
import gui.screen.StudentDashboardScreen;
import main.BaseWindow;
import model.User;
import service.AuthService;
import java.util.Locale;

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
        try {
            System.out.println("[ScreenRouter] setCurrent -> " + (screen == null ? "null" : screen.getClass().getName()));
            this.current = screen;
            this.current.mount();
            this.current.onResize();
            System.out.println("[ScreenRouter] setCurrent OK");
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("[ScreenRouter] Echec changement d'ecran vers "
                    + (screen == null ? "null" : screen.getClass().getName()));
            this.current = null;
            showLogin();
        }
    }
}
