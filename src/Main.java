import gui.navigation.ScreenRouter;
import main.BaseWindow;

/**
 * Point d'entrée principal de l'application de gestion des choix de dominantes (PDL).
 * 
 * Note : Les commentaires techniques et la structure de la Javadoc ont été réalisés 
 * partiellement avec l'assistance d'une IA pour garantir une documentation claire des fonctionnalités avancées.
 */
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