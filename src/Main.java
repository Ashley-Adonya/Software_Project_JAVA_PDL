import gui.navigation.ScreenRouter;
import main.BaseWindow;

public class Main {
	public static void main(String[] args) {
		BaseWindow window = new BaseWindow("Esigelec - PDL", 1100, 760, 0);
		window.setDebugOverlayEnabled(false);
		window.setDebugEventOverlayEnabled(false);
		ScreenRouter router = new ScreenRouter(window);
		router.showLogin();
		window.show();
	}
}