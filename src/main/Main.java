package main;
import gui.navigation.ScreenRouter;
import main.BaseWindow;

/**
 * Main entry point of the PDL (Dominante Choice Management) application for
 * Esigelec.
 * <p>
 * This class is responsible for bootstrapping the application: it creates the
 * primary {@link BaseWindow}, configures debug overlays, initialises the
 * {@link ScreenRouter}, presents the login screen, and makes the window visible.
 * The entire navigation life cycle is delegated to {@link ScreenRouter} after
 * startup.
 * </p>
 */
public class Main {
	/**
	 * Application entry point.
	 * <p>
	 * Creates the main window, disables debug visual overlays, instantiates the
	 * screen router, navigates to the login screen, and finally displays the
	 * window.
	 * </p>
	 *
	 * @param args command-line arguments (not used)
	 */
	public static void main(String[] args) {
		BaseWindow window = new BaseWindow("Esigelec - PDL", 1100, 760, 0);
		window.setDebugOverlayEnabled(false);
		window.setDebugEventOverlayEnabled(false);
		ScreenRouter router = new ScreenRouter(window);
		router.showLogin();
		window.show();
	}
}