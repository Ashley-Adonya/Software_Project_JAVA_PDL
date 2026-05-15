package gui.screen.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import components.Button;
import components.Label;
import components.ScrollView;
import gui.components.KpiCard;
import gui.components.SearchField;
import gui.components.SurfaceCard;
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
    private final SurfaceCard root;
    private final ScrollView statsScroll;
    private final BaseComp statsContent;
    private final SurfaceCard heroCard;
    private final Label heroTitle;
    private final Label heroSubtitle;
    private final Label heroStatus;
    private final KpiCard totalSessionsKpi;
    private final KpiCard completeSessionsKpi;
    private final KpiCard fillRateKpi;
    private final KpiCard unregisteredKpi;
    private final SurfaceCard analysisCard;
    private final Label analysisTitle;
    private final Label analysisLine1;
    private final Label analysisLine2;
    private final Label analysisLine3;
    private final SurfaceCard studentsCard;
    private final Label studentsTitle;
    private final Label studentsSubtitle;
    private final SearchField studentSearchField;
    private final Label studentCountLabel;
    private final ScrollView unregisteredScroll;
    private final BaseComp unregisteredList;

    private Consumer<User> onSelectStudent = u -> {};
    private List<User> cachedUnregisteredStudents = new ArrayList<>();
    private boolean darkMode = true;

    public StatsPanelComponent(BaseWindow window, StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.root = new SurfaceCard(0, 0, 100, 100, new Color(14, 18, 26), new Color(14, 18, 26), 0);
        this.statsScroll = new ScrollView(0, 0, 100, 100);
        this.statsContent = statsScroll.getContent();

        this.heroCard = new SurfaceCard(0, 0, 100, 120, new Color(18, 24, 35), new Color(52, 63, 92), 14);
        this.heroTitle = new Label("Statistiques de campagne", 18, 16, 400, 26);
        this.heroTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        this.heroTitle.setColor(new Color(239, 244, 252));
        this.heroSubtitle = new Label("Vue synthétique des inscriptions et du remplissage", 18, 48, 520, 20);
        this.heroSubtitle.setFont(new Font("Dialog", Font.PLAIN, 13));
        this.heroSubtitle.setColor(new Color(161, 175, 202));
        this.heroStatus = new Label("Actualisez uniquement cette vue pour recalculer les données", 18, 72, 620, 18);
        this.heroStatus.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.heroStatus.setColor(new Color(129, 143, 170));
        heroCard.addChild(heroTitle);
        heroCard.addChild(heroSubtitle);
        heroCard.addChild(heroStatus);

        this.totalSessionsKpi = new KpiCard("Sessions totales", "0", "Nombre total", new java.awt.Color(59, 130, 246));
        this.completeSessionsKpi = new KpiCard("Sessions completes", "0", "Plein", new java.awt.Color(34, 197, 94));
        this.fillRateKpi = new KpiCard("Taux remplissage", "0%", "Moyenne", new java.awt.Color(245, 158, 11));
        this.unregisteredKpi = new KpiCard("Non inscrits", "0", "Etudiants", new java.awt.Color(239, 68, 68));

        this.analysisCard = new SurfaceCard(0, 0, 100, 180, new Color(22, 28, 39), new Color(48, 60, 82), 14);
        this.analysisTitle = new Label("Lecture rapide", 18, 16, 220, 20);
        this.analysisTitle.setFont(new Font("Dialog", Font.BOLD, 15));
        this.analysisTitle.setColor(new Color(239, 244, 252));
        this.analysisLine1 = new Label("Sessions complètes: 0", 18, 50, 320, 18);
        this.analysisLine1.setFont(new Font("Dialog", Font.PLAIN, 13));
        this.analysisLine1.setColor(new Color(193, 205, 227));
        this.analysisLine2 = new Label("Capacité utilisée: 0 / 0", 18, 76, 320, 18);
        this.analysisLine2.setFont(new Font("Dialog", Font.PLAIN, 13));
        this.analysisLine2.setColor(new Color(193, 205, 227));
        this.analysisLine3 = new Label("Etudiants non inscrits: 0", 18, 102, 320, 18);
        this.analysisLine3.setFont(new Font("Dialog", Font.PLAIN, 13));
        this.analysisLine3.setColor(new Color(193, 205, 227));
        analysisCard.addChild(analysisTitle);
        analysisCard.addChild(analysisLine1);
        analysisCard.addChild(analysisLine2);
        analysisCard.addChild(analysisLine3);

        this.studentsCard = new SurfaceCard(0, 0, 100, 180, new Color(22, 28, 39), new Color(48, 60, 82), 14);
        this.studentsTitle = new Label("Etudiants a suivre", 18, 16, 220, 20);
        this.studentsTitle.setFont(new Font("Dialog", Font.BOLD, 15));
        this.studentsTitle.setColor(new Color(239, 244, 252));
        this.studentsSubtitle = new Label("Cliquez un étudiant pour ouvrir son contexte", 18, 38, 340, 16);
        this.studentsSubtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.studentsSubtitle.setColor(new Color(161, 175, 202));

        this.studentSearchField = new SearchField(18, 58, 320, 30, "Rechercher un étudiant");
        this.studentSearchField.setColors(new Color(28, 36, 50), new Color(48, 60, 82), new Color(82, 107, 255), new Color(239, 244, 252), new Color(132, 144, 168));
        this.studentSearchField.setOnChange(this::renderFilteredStudents);

        this.studentCountLabel = new Label("0 étudiant", 18, 92, 220, 16);
        this.studentCountLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.studentCountLabel.setColor(new Color(161, 175, 202));

        this.unregisteredScroll = new ScrollView(0, 0, 100, 100);
        this.unregisteredList = unregisteredScroll.getContent();

        studentsCard.addChild(studentsTitle);
        studentsCard.addChild(studentsSubtitle);
        studentsCard.addChild(studentSearchField);
        studentsCard.addChild(studentCountLabel);
        studentsCard.addChild(unregisteredScroll);

        statsContent.addChild(heroCard);
        statsContent.addChild(totalSessionsKpi);
        statsContent.addChild(completeSessionsKpi);
        statsContent.addChild(fillRateKpi);
        statsContent.addChild(unregisteredKpi);
        statsContent.addChild(analysisCard);
        statsContent.addChild(studentsCard);
        root.addChild(statsScroll);
    }

    public BaseComp getRoot() { return root; }
    public void onSelectStudent(Consumer<User> cb) { this.onSelectStudent = cb; }
    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (dark) {
            root.setBackground(new Color(14, 18, 26));
            heroCard.setBackground(new Color(18, 24, 35));
            heroCard.setBorderColor(new Color(52, 63, 92));
            analysisCard.setBackground(new Color(22, 28, 39));
            analysisCard.setBorderColor(new Color(48, 60, 82));
            studentsCard.setBackground(new Color(22, 28, 39));
            studentsCard.setBorderColor(new Color(48, 60, 82));
        } else {
            root.setBackground(new Color(243, 246, 252));
            heroCard.setBackground(Color.WHITE);
            heroCard.setBorderColor(new Color(226, 230, 238));
            analysisCard.setBackground(Color.WHITE);
            analysisCard.setBorderColor(new Color(226, 230, 238));
            studentsCard.setBackground(Color.WHITE);
            studentsCard.setBorderColor(new Color(226, 230, 238));
        }
        root.invalidate();
    }

    public void refresh(int campaignId, String promo) {
        if (campaignId <= 0 || promo == null) {
            heroSubtitle.setText("Aucune campagne active");
            heroStatus.setText("Sélectionnez ou ouvrez une campagne pour afficher les statistiques.");
            analysisLine1.setText("Sessions complètes: 0");
            analysisLine2.setText("Capacité utilisée: 0 / 0");
            analysisLine3.setText("Etudiants non inscrits: 0");
            clearChildren(unregisteredList);
            Label empty = new Label("Aucune donnée disponible pour le moment.", 0, 0, 280, 22);
            empty.setFont(new Font("Dialog", Font.PLAIN, 13));
            empty.setColor(new Color(172, 183, 204));
            unregisteredList.addChild(empty);
            return;
        }
        heroSubtitle.setText("Promo " + promo + " • Campagne #" + campaignId);
        heroStatus.setText("Actualisé à la demande via le bouton Actualiser");
        StatisticsService.StatsSummary s = statisticsService.getStatsForCampaign(campaignId, promo);
        totalSessionsKpi.setValue(String.valueOf(s.totalSessions));
        completeSessionsKpi.setValue(String.valueOf(s.completeSessions));
        fillRateKpi.setValue(String.format("%.1f%%", s.averageFillRate));
        unregisteredKpi.setValue(String.valueOf(s.unregisteredStudents));

        analysisLine1.setText("Sessions complètes: " + s.completeSessions + " / " + s.totalSessions);
        analysisLine2.setText("Capacité utilisée: " + s.totalAllocated + " / " + s.totalCapacity);
        analysisLine3.setText("Etudiants non inscrits: " + s.unregisteredStudents + " / " + s.totalStudents);

        List<User> unregistered = statisticsService.getUnregisteredStudents(campaignId, promo);
        cachedUnregisteredStudents = new ArrayList<>(unregistered);
        clearChildren(unregisteredList);
        int y = 0;
        for (User u : unregistered) {
            String name = u.getFullName() == null || u.getFullName().isBlank() ? u.getLogin() : u.getFullName();
            Button b = new Button(name, 0, y, Math.max(220, unregisteredScroll.getWidth() - 8), 38, () -> onSelectStudent.accept(u));
            b.setBackground(new java.awt.Color(40, 50, 70));
            unregisteredList.addChild(b);
            y += 40;
        }
        if (unregistered.isEmpty()) {
            Label l = new Label("Tous les étudiants sont inscrits.", 0, 0, 280, 24);
            l.setFont(new Font("Dialog", Font.PLAIN, 13));
            l.setColor(new Color(172, 183, 204));
            unregisteredList.addChild(l);
        }
        unregisteredScroll.setContentHeight(Math.max(unregisteredScroll.getHeight(), y + 10));
    }

    public void onResize(int mainW, int mainH) {
        statsScroll.setBounds(0, 0, mainW, mainH);
        int gap = 12;
        int pad = 0;

        heroCard.setBounds(pad, 0, mainW, 124);
        int heroCardY = 128;

        int kpiW = (mainW - gap * 3) / 4;
        int kpiH = 104;
        totalSessionsKpi.setBounds(0, heroCardY, kpiW, kpiH);
        completeSessionsKpi.setBounds(kpiW + gap, heroCardY, kpiW, kpiH);
        fillRateKpi.setBounds((kpiW + gap) * 2, heroCardY, kpiW, kpiH);
        unregisteredKpi.setBounds((kpiW + gap) * 3, heroCardY, kpiW, kpiH);

        int lowerY = heroCardY + kpiH + gap;
        int halfW = (mainW - gap) / 2;
        int lowerH = Math.max(240, mainH - lowerY - 18);
        analysisCard.setBounds(0, lowerY, halfW, lowerH);
        studentsCard.setBounds(halfW + gap, lowerY, mainW - halfW - gap, lowerH);
        studentSearchField.setBounds(16, 58, Math.max(220, halfW - 32), 30);
        studentCountLabel.setBounds(16, 92, halfW - 32, 16);
        unregisteredScroll.setBounds(16, 130, studentsCard.getWidth() - 32, lowerH - 138);
    }

    /**
     * Filtre la liste des étudiants non inscrits selon la requête de recherche.
     * La recherche est effectuée sur le nom complet ou le login (case-insensitive).
     * La liste de cache est conservée, seul l'affichage est filtré.
     * 
     * @param query Texte de recherche (peut être vide)
     */
    private void renderFilteredStudents() {
        String query = studentSearchField.getCurrentText();
        clearChildren(unregisteredList);
        String lowerQuery = (query == null ? "" : query).toLowerCase();
        
        List<User> filtered = new ArrayList<>();
        for (User u : cachedUnregisteredStudents) {
            if (u == null) continue;
            String name = (u.getFullName() == null ? "" : u.getFullName()).toLowerCase();
            String login = (u.getLogin() == null ? "" : u.getLogin()).toLowerCase();
            if (name.contains(lowerQuery) || login.contains(lowerQuery)) {
                filtered.add(u);
            }
        }
        
        int y = 0;
        for (User u : filtered) {
            String displayName = (u.getFullName() == null || u.getFullName().isBlank()) ? u.getLogin() : u.getFullName();
            Button b = new Button(displayName, 0, y, Math.max(220, unregisteredScroll.getWidth() - 8), 38, () -> onSelectStudent.accept(u));
            b.setBackground(new java.awt.Color(40, 50, 70));
            unregisteredList.addChild(b);
            y += 40;
        }
        
        if (filtered.isEmpty()) {
            Label l = new Label("Aucun résultat.", 0, 0, 280, 24);
            l.setFont(new Font("Dialog", Font.PLAIN, 13));
            l.setColor(new Color(172, 183, 204));
            unregisteredList.addChild(l);
        }
        
        unregisteredScroll.setContentHeight(Math.max(unregisteredScroll.getHeight(), y + 10));
    }

    private void clearChildren(main.BaseComp parent) { ArrayList<main.BaseComp> snapshot = new ArrayList<>(parent.getChildrenList()); for (main.BaseComp c : snapshot) parent.removeChild(c); }
}
