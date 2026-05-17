package gui.screen.components;

import java.awt.Color;
import main.BaseComp;
import service.StatisticsService;
import gui.components.SurfaceCard;
import gui.components.KpiCard;

/**
 * Key Performance Indicator (KPI) section of the statistics panel.
 * Displays four summary metrics in a horizontal row of {@link KpiCard} components:
 * total sessions, completely full sessions, average fill rate, and total registered students.
 *
 * <p><b>Key features:</b></p>
 * <ul>
 *   <li>Four colour-coded KPI cards: blue (sessions), green (full), amber (fill rate), purple (registered)</li>
 *   <li>Updates all card values in a single call from a {@link StatisticsService.StatsSummary}</li>
 *   <li>Automatically distributes available width evenly among the four cards</li>
 *   <li>Supports dark/light mode theming propagated to each {@code KpiCard}</li>
 * </ul>
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

    /**
     * Returns the root UI component of this KPI section.
     *
     * @return the root {@link SurfaceCard} instance containing all four KPI cards
     */
    public SurfaceCard getRoot() { return container; }

    /**
     * Switches the section between dark and light colour themes.
     * Updates the container background, border colour, and propagates
     * the theme to all four {@link KpiCard} children.
     *
     * @param dark {@code true} for dark mode, {@code false} for light mode
     */
    public void setDarkMode(boolean dark) {
        Color bg = dark ? new Color(22, 28, 39) : Color.WHITE;
        container.setBackground(bg); container.setBorderColor(dark ? new Color(52, 63, 92) : new Color(226, 230, 238));
        totalSessionsKpi.setDarkMode(dark); completeSessionsKpi.setDarkMode(dark);
        fillRateKpi.setDarkMode(dark); registeredKpi.setDarkMode(dark);
    }

    /**
     * Updates all four KPI card values from the provided statistics summary.
     * Cards show: total session count, full session count, average fill rate
     * (formatted to one decimal place with a percent sign), and total registered students.
     *
     * @param s the {@link StatisticsService.StatsSummary} containing the latest metrics
     */
    public void update(StatisticsService.StatsSummary s) {
        totalSessionsKpi.setValue(String.valueOf(s.totalSessions));
        completeSessionsKpi.setValue(String.valueOf(s.completeSessions));
        fillRateKpi.setValue(String.format("%.1f%%", s.averageFillRate));
        registeredKpi.setValue(String.valueOf(s.registeredStudents));
    }

    /**
     * Adjusts the layout of the four KPI cards horizontally within the available width.
     * Each card receives an equal quarter of the width, with uniform 12 px gaps between them.
     *
     * @param w the total available width in pixels
     */
    public void onResize(int w) {
        container.setBounds(0, 0, w, 110);
        int kw = (w - 36) / 4;
        totalSessionsKpi.setBounds(0, 0, kw, 110);
        completeSessionsKpi.setBounds(kw + 12, 0, kw, 110);
        fillRateKpi.setBounds((kw + 12) * 2, 0, kw, 110);
        registeredKpi.setBounds((kw + 12) * 3, 0, kw, 110);
    }
}