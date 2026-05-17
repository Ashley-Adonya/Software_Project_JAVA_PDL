package gui.navigation;

import gui.screen.AdminDashboardScreen;
import gui.screen.LoginScreen;
import gui.screen.StudentDashboardScreen;
import main.BaseWindow;
import model.User;
import service.AuthService;
import java.util.Locale;

/**
 * Centralized navigation manager for the application.
 * Handles screen transitions between Login, AdminDashboard, and StudentDashboard
 * based on the authenticated user's role.
 * Manages the main window initialization and delegates resize events to the active screen.
 * <p>
 * The router is the entry point for all screen lifecycle management. It creates screen
 * instances, calls mount() when switching screens, and propagates window resize events
 * to the currently active screen via a registered resize listener.
 * <p>
 * Key features:
 * - Role-based routing (admin vs student)
 * - Graceful fallback to login on unrecognized roles
 * - Automatic resize event delegation to the active screen
 * - Timing instrumentation for screen transitions (logged to console)
 * - Error recovery: returns to login if a screen fails to mount
 *
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class ScreenRouter {
    private final BaseWindow window;
    private final AuthService authService;
    private AppScreen current;

    /**
     * Constructs the screen router and registers a resize listener on the window.
     * Whenever the window is resized, the router delegates the resize event to the
     * currently active AppScreen to keep the layout responsive.
     *
     * @param window the main application window to manage navigation for
     */
    public ScreenRouter(BaseWindow window) {
        this.window = window;
        this.authService = new AuthService();
        this.window.addResizeListener(() -> {
            if (current != null) {
                current.onResize();
            }
        });
    }

    /**
     * Navigates to the login screen by creating a new LoginScreen instance
     * and setting it as the active screen. The LoginScreen is configured with
     * an authentication callback (onAuthenticated) that handles role-based
     * redirection after successful login.
     * This method is also used as the logout callback from dashboard screens.
     */
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
