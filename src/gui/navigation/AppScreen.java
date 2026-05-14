package gui.navigation;

/**
 * Interface représentant un écran de l'application qui implémente le cycle de vie commun.
 * Chaque écran doit gérer son montage initial et son redimensionnement.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public interface AppScreen {
    void mount();
    void onResize();
}
