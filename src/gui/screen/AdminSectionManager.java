package gui.screen;

import gui.components.PageHeader;
import service.CacheManager;

/**
 * Gestionnaire des sections du tableau de bord administrateur.
 * Centralise la visibilité des sections, les titres et le rafraîchissement.
 */
public class AdminSectionManager {
    private final AdminDashboardView view;
    
    public enum Section { DASHBOARD, DOMINANTES, SESSIONS, CAMPAGNE, STATS }
    private Section activeSection = Section.DASHBOARD;

    public AdminSectionManager(AdminDashboardView view) {
        this.view = view;
    }

    public Section getActiveSection() { return activeSection; }

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