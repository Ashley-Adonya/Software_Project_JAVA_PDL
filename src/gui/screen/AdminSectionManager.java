package gui.screen;

import gui.components.PageHeader;
import service.CacheManager;

/**
 * Manages navigation, visibility, and refresh of all sections in the admin dashboard.
 * <p>
 * Centralises the logic for switching between the five main dashboard sections
 * (Dashboard, Dominantes, Sessions, Campagne, Stats). Responsibilities include:
 * <ul>
 *   <li>Updating the active section based on a string key</li>
 *   <li>Toggling visibility of section components so only one is shown at a time</li>
 *   <li>Updating the page header title and subtitle to reflect the active section</li>
 *   <li>Delegating data refresh to the correct view method</li>
 *   <li>Invalidating the statistics cache before each refresh to guarantee fresh data</li>
 * </ul>
 * </p>
 */
public class AdminSectionManager {
    private final AdminDashboardView view;
    
    public enum Section { DASHBOARD, DOMINANTES, SESSIONS, CAMPAGNE, STATS }
    private Section activeSection = Section.DASHBOARD;

    /**
     * Constructs a section manager bound to the specified dashboard view.
     * <p>
     * The initial active section is set to {@link Section#DASHBOARD}.
     * </p>
     *
     * @param view the parent {@link AdminDashboardView} that this manager controls
     */
    public AdminSectionManager(AdminDashboardView view) {
        this.view = view;
    }

    /**
     * Returns the currently active dashboard section.
     *
     * @return the active {@link Section} enum value, never {@code null}
     */
    public Section getActiveSection() { return activeSection; }

    /**
     * Sets the active section by its string key and triggers a full layout refresh.
     * <p>
     * Accepted key values and their corresponding sections:
     * <ul>
     *   <li>{@code "dominantes"} &rarr; {@link Section#DOMINANTES}</li>
     *   <li>{@code "sessions"}   &rarr; {@link Section#SESSIONS}</li>
     *   <li>{@code "campagne"}   &rarr; {@link Section#CAMPAGNE}</li>
     *   <li>{@code "stats"}      &rarr; {@link Section#STATS}</li>
     *   <li>any other value      &rarr; {@link Section#DASHBOARD}</li>
     * </ul>
     * After updating the section, {@link AdminDashboardView#applySectionChange()} is
     * called to recompute layout and refresh content.
     * </p>
     *
     * @param key the section identifier string
     */
    public void setSection(String key) {
        activeSection = switch (key) {
            case "dominantes" -> Section.DOMINANTES;
            case "sessions" -> Section.SESSIONS;
            case "campagne" -> Section.CAMPAGNE;
            case "stats" -> Section.STATS;
            default -> Section.DASHBOARD;
        };
        view.applySectionChange();
    }

    /**
     * Applies visibility toggles so that only the currently active section is visible.
     * <p>
     * Updates the page header title/subtitle via {@link #updateHeader()} and sets
     * each section component's visibility to {@code true} only when it matches
     * the active section. All other sections are hidden.
     * </p>
     */
    public void applyVisibility() {
        updateHeader();
        view.setDashboardVisible(activeSection == Section.DASHBOARD);
        view.setDominantesVisible(activeSection == Section.DOMINANTES);
        view.setSessionsVisible(activeSection == Section.SESSIONS);
        view.setCampagneVisible(activeSection == Section.CAMPAGNE);
        view.setStatsVisible(activeSection == Section.STATS);
    }

    private void updateHeader() {
        PageHeader header = view.getHeader();
        switch (activeSection) {
            case DOMINANTES -> { header.setTitle("Dominantes"); header.setSubtitle("Gerez les domaines d'etudes disponibles"); }
            case SESSIONS -> { header.setTitle("Sessions"); header.setSubtitle("Gerez les creneaux de presentation"); }
            case CAMPAGNE -> { header.setTitle("Campagne"); header.setSubtitle("Configurez les parametres generaux"); }
            case STATS -> { header.setTitle("Statistiques"); header.setSubtitle("Analyse des inscriptions"); }
            default -> { header.setTitle("Tableau de bord"); header.setSubtitle("Vue d'ensemble de la campagne"); }
        }
    }

    /**
     * Refreshes the data displayed by the currently active section.
     * <p>
     * Invalidates all statistics cache entries (prefix {@code "stats:"})
     * via {@link CacheManager#invalidatePrefix} before delegating the actual
     * refresh to the appropriate method on {@link AdminDashboardView}. Finally,
     * a re-render of the window is requested.
     * </p>
     */
    public void refreshActiveSection() {
        CacheManager.invalidatePrefix("stats:");
        switch (activeSection) {
            case DOMINANTES -> view.refreshDominantes();
            case SESSIONS -> view.refreshSessions();
            case CAMPAGNE -> view.refreshCampagne();
            case STATS -> view.refreshStats();
            default -> view.refreshDashboard();
        }
        view.requestRender();
    }
}