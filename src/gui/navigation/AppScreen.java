package gui.navigation;

/**
 * Defines the lifecycle contract for every screen in the application.
 * Each screen must implement mounting (initial layout and child creation)
 * and resize handling so the navigation layer can uniformly manage all
 * screens.
 *
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public interface AppScreen {
    /**
     * Called once when the screen is first mounted. Implementations should
     * create child components, register event listeners, and perform initial
     * layout.
     */
    void mount();

    /**
     * Called whenever the parent container is resized. Implementations should
     * recalculate positions and dimensions of child components.
     */
    void onResize();
}
