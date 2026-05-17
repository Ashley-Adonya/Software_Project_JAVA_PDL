package gui.screen.components;

import java.awt.Color;
import main.BaseComp;
import service.StatisticsService;
import gui.components.SurfaceCard;
import gui.components.KpiCard;

/**
 * Section KPI du panneau de statistiques.
 * Affiche les 4 KPIs principaux : sessions totales, completes, taux de remplissage, non inscrits.
 */
public class StatsKpiSection {
    private final SurfaceCard container;
    private final KpiCard totalSessionsKpi;
    private final KpiCard completeSessionsKpi;
    private final KpiCard fillRateKpi;
    private final KpiCard registeredKpi;

    public StatsKpiSection() {
        container = new SurfaceCard(0, 0, 100, 80, new Color(22, 28, 39), new Color(52, 63, 92), 12);
        totalSessionsKpi = new KpiCard("Sessions", "0", "Total", new Color(59, 130, 246));
        completeSessionsKpi = new KpiCard("Completes", "0", "Plein", new Color(34, 197, 94));
        fillRateKpi = new KpiCard("Remplissage", "0%", "Moyenne", new Color(245, 158, 11));
        registeredKpi = new KpiCard("Inscrits", "0", "Total", new Color(168, 85, 247));
        container.addChild(totalSessionsKpi); container.addChild(completeSessionsKpi);
        container.addChild(fillRateKpi); container.addChild(registeredKpi);
    }

    public SurfaceCard getRoot() { return container; }
    public void setDarkMode(boolean dark) {
        Color bg = dark ? new Color(22, 28, 39) : Color.WHITE;
        container.setBackground(bg); container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        totalSessionsKpi.setDarkMode(dark); completeSessionsKpi.setDarkMode(dark);
        fillRateKpi.setDarkMode(dark); registeredKpi.setDarkMode(dark);
    }

    public void update(StatisticsService.StatsSummary s) {
        totalSessionsKpi.setValue(String.valueOf(s.totalSessions));
        completeSessionsKpi.setValue(String.valueOf(s.completeSessions));
        fillRateKpi.setValue(String.format("%.1f%%", s.averageFillRate));
        registeredKpi.setValue(String.valueOf(s.registeredStudents));
    }

    public void onResize(int w) {
        container.setBounds(0, 0, w, 110);
        int kw = (w - 36) / 4;
        totalSessionsKpi.setBounds(0, 0, kw, 110);
        completeSessionsKpi.setBounds(kw + 12, 0, kw, 110);
        fillRateKpi.setBounds((kw + 12) * 2, 0, kw, 110);
        registeredKpi.setBounds((kw + 12) * 3, 0, kw, 110);
    }
}