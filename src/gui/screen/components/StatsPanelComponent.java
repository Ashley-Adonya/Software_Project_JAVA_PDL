package gui.screen.components;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import gui.components.KpiCard;
import components.Button;
import components.Label;
import components.ScrollView;
import main.BaseComp;
import main.BaseWindow;
import model.User;
import service.StatisticsService;

/**
 * Composant d'affichage des statistiques et indicateurs de campagne.
 * Présente des KPIs (sessions totales, completes, taux de remplissage)
 * et liste des étudiants non inscrits.
 * 
 * Responsabilités :
 * - Rendu des cartes KPI avec valeurs calculées
 * - Affichage de la liste des étudiants sans inscription
 * - Callback de sélection d'étudiant
 * - Gestion du redimensionnement pour adaptation responsive
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class StatsPanelComponent {
    private final StatisticsService statisticsService;
    private final BaseComp root;
    private final ScrollView statsScroll;
    private final BaseComp statsContent;
    private final KpiCard totalSessionsKpi;
    private final KpiCard completeSessionsKpi;
    private final KpiCard fillRateKpi;
    private final KpiCard unregisteredKpi;
    private final ScrollView unregisteredScroll;
    private final BaseComp unregisteredList;

    private Consumer<User> onSelectStudent = u -> {};

    public StatsPanelComponent(BaseWindow window, StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.root = new BaseComp(null);
        this.statsScroll = new ScrollView(0, 0, 100, 100);
        this.statsContent = statsScroll.getContent();

        this.totalSessionsKpi = new KpiCard("Sessions totales", "0", "Nombre total", new java.awt.Color(59, 130, 246));
        this.completeSessionsKpi = new KpiCard("Sessions completes", "0", "Plein", new java.awt.Color(34, 197, 94));
        this.fillRateKpi = new KpiCard("Taux remplissage", "0%", "Moyenne", new java.awt.Color(245, 158, 11));
        this.unregisteredKpi = new KpiCard("Non inscrits", "0", "Etudiants", new java.awt.Color(239, 68, 68));

        this.unregisteredScroll = new ScrollView(0, 0, 100, 100);
        this.unregisteredList = unregisteredScroll.getContent();

        statsContent.addChild(totalSessionsKpi);
        statsContent.addChild(completeSessionsKpi);
        statsContent.addChild(fillRateKpi);
        statsContent.addChild(unregisteredKpi);

        statsContent.addChild(unregisteredScroll);
        root.addChild(statsScroll);
    }

    public BaseComp getRoot() { return root; }
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }

    public void refresh(int campaignId, String promo) {
        if (campaignId <= 0 || promo == null) return;
        StatisticsService.StatsSummary s = statisticsService.getStatsForCampaign(campaignId, promo);
        totalSessionsKpi.setValue(String.valueOf(s.totalSessions));
        completeSessionsKpi.setValue(String.valueOf(s.completeSessions));
        fillRateKpi.setValue(String.format("%.1f%%", s.averageFillRate));
        unregisteredKpi.setValue(String.valueOf(s.unregisteredStudents));

        List<User> unregistered = statisticsService.getUnregisteredStudents(campaignId, promo);
        clearChildren(unregisteredList);
        int y = 0;
        for (User u : unregistered) {
            Button b = new Button(u.getFullName(), 0, y, unregisteredScroll.getWidth() - 8, 36, () -> onSelectStudent.accept(u));
            b.setBackground(new java.awt.Color(40, 50, 70));
            unregisteredList.addChild(b);
            y += 40;
        }
        if (unregistered.isEmpty()) {
            Label l = new Label("Tous les etudiants sont inscrits", 0, 0, 200, 24);
            l.setFont(new Font("Dialog", Font.PLAIN, 13));
            unregisteredList.addChild(l);
        }
        unregisteredScroll.setContentHeight(Math.max(unregisteredScroll.getHeight(), y + 10));
    }

    public void onResize(int mainW, int mainH) {
        statsScroll.setBounds(0, 0, mainW, mainH);
        int gap = 12;
        int kpiW = (mainW - gap * 3) / 4;
        int kpiH = 100;
        totalSessionsKpi.setBounds(0, 12, kpiW, kpiH);
        completeSessionsKpi.setBounds(kpiW + gap, 12, kpiW, kpiH);
        fillRateKpi.setBounds((kpiW + gap) * 2, 12, kpiW, kpiH);
        unregisteredKpi.setBounds((kpiW + gap) * 3, 12, kpiW, kpiH);

        int cardY = 12 + kpiH + gap;
        int halfW = (mainW - gap) / 2;
        unregisteredScroll.setBounds(8, cardY + 8, halfW - 16, Math.max(200, mainH - cardY - 40));
    }

    private void clearChildren(main.BaseComp parent) { ArrayList<main.BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (main.BaseComp c : snapshot) parent.removeChild(c); }
}
