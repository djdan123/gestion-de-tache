package ui;

import dao.DatabaseConnection;
import org.jfree.chart.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FenetreStatistiques extends JFrame {
    private static final Logger logger = Logger.getLogger(FenetreStatistiques.class.getName());
    private JTabbedPane onglets;

    public FenetreStatistiques() {
        setTitle("Tableau de Bord - Statistiques");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initUI();
        chargerDonnees();
        
        // Rafraîchissement automatique toutes les 30 secondes
        Timer timer = new Timer(30000, e -> chargerDonnees());
        timer.start();
    }

    private void initUI() {
        onglets = new JTabbedPane();
        
        // Onglet 1: Statistiques des tâches
        JFreeChart chartTaches = ChartFactory.createBarChart(
            "Répartition des tâches par statut",
            "Statut",
            "Nombre",
            new DefaultCategoryDataset(),
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            true, true, false
        );
        customizeChart(chartTaches, Color.BLUE);
        onglets.addTab("Tâches", new ChartPanel(chartTaches));

        // Onglet 2: Charge des employés
        JFreeChart chartEmployes = ChartFactory.createBarChart(
            "Charge de travail par employé (Top 10)",
            "Employé",
            "Nombre de tâches",
            new DefaultCategoryDataset(),
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            true, true, false
        );
        customizeChart(chartEmployes, Color.ORANGE);
        onglets.addTab("Employés", new ChartPanel(chartEmployes));

        // Onglet 3: Répartition par projet
        JFreeChart chartProjets = ChartFactory.createPieChart(
            "Répartition des tâches par projet",
            new DefaultPieDataset(),
            true, true, false
        );
        customizePieChart((PiePlot) chartProjets.getPlot());
        onglets.addTab("Projets", new ChartPanel(chartProjets));

        // Onglet 4: Dashboard résumé
        onglets.addTab("Résumé", creerPanelResume());

        add(onglets);
    }

    private void chargerDonnees() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                DefaultCategoryDataset datasetTaches = new DefaultCategoryDataset();
                DefaultCategoryDataset datasetEmployes = new DefaultCategoryDataset();
                DefaultPieDataset datasetProjets = new DefaultPieDataset();

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // 1. Stats tâches par statut
                    chargerStatsTaches(conn, datasetTaches);
                    
                    // 2. Charge employés
                    chargerChargeEmployes(conn, datasetEmployes);
                    
                    // 3. Répartition par projet
                    chargerRepartitionProjets(conn, datasetProjets);
                    
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erreur de chargement des données", e);
                    JOptionPane.showMessageDialog(FenetreStatistiques.this,
                        "Erreur de connexion à la base de données",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }

                // Mise à jour de l'interface dans le thread EDT
                SwingUtilities.invokeLater(() -> {
                    updateChart(0, datasetTaches);
                    updateChart(1, datasetEmployes);
                    updatePieChart(2, datasetProjets);
                    updateResumePanel();
                });
                
                return null;
            }
        };
        worker.execute();
    }

    private void chargerStatsTaches(Connection conn, DefaultCategoryDataset dataset) throws SQLException {
        String sql = "SELECT statut, COUNT(*) as count FROM Tache GROUP BY statut";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Tâches", rs.getString("statut"));
            }
        }
    }

    private void chargerChargeEmployes(Connection conn, DefaultCategoryDataset dataset) throws SQLException {
        String sql = """
            SELECT CONCAT(e.nom, ' ', e.prenom) as nom_complet, COUNT(t.id) as count 
            FROM Employe e LEFT JOIN Tache t ON e.id = t.id_employe 
            GROUP BY e.id ORDER BY count DESC LIMIT 10""";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Tâches", rs.getString("nom_complet"));
            }
        }
    }

    private void chargerRepartitionProjets(Connection conn, DefaultPieDataset dataset) throws SQLException {
        String sql = """
            SELECT p.nom, COUNT(t.id) as count 
            FROM Projet p LEFT JOIN Tache t ON p.id = t.id_projet 
            GROUP BY p.id""";
            
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dataset.setValue(rs.getString("nom"), rs.getInt("count"));
            }
        }
    }

    private void updateChart(int ongletIndex, DefaultCategoryDataset dataset) {
        ChartPanel chartPanel = (ChartPanel) onglets.getComponentAt(ongletIndex);
        CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
        plot.setDataset(dataset);
    }

    private void updatePieChart(int ongletIndex, DefaultPieDataset dataset) {
        ChartPanel chartPanel = (ChartPanel) onglets.getComponentAt(ongletIndex);
        PiePlot plot = (PiePlot) chartPanel.getChart().getPlot();
        plot.setDataset(dataset);
    }

    private JPanel creerPanelResume() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(creerCarteInfo("Tâches totales", "0", new Color(70, 130, 180)));
        panel.add(creerCarteInfo("Tâches en cours", "0", new Color(255, 165, 0)));
        panel.add(creerCarteInfo("Employés actifs", "0", new Color(34, 139, 34)));
        panel.add(creerCarteInfo("Projets en cours", "0", new Color(147, 112, 219)));
        
        return panel;
    }

    private JPanel creerCarteInfo(String titre, String valeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(couleur.darker(), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        carte.setBackground(couleur.brighter().brighter());

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel lblValeur = new JLabel(valeur, SwingConstants.CENTER);
        lblValeur.setFont(new Font("SansSerif", Font.BOLD, 36));
        
        carte.add(lblTitre, BorderLayout.NORTH);
        carte.add(lblValeur, BorderLayout.CENTER);
        
        return carte;
    }

    private void updateResumePanel() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JPanel panelResume = (JPanel) onglets.getComponentAt(3);
            
            // Tâches totales
            int totalTaches = getCount(conn, "SELECT COUNT(*) FROM Tache");
            ((JLabel)((JPanel)panelResume.getComponent(0)).getComponent(1)).setText(String.valueOf(totalTaches));
            
            // Tâches en cours
            int tachesEnCours = getCount(conn, "SELECT COUNT(*) FROM Tache WHERE statut = 'en cours'");
            ((JLabel)((JPanel)panelResume.getComponent(1)).getComponent(1)).setText(String.valueOf(tachesEnCours));
            
            // Employés actifs
            int employesActifs = getCount(conn, "SELECT COUNT(DISTINCT id_employe) FROM Tache");
            ((JLabel)((JPanel)panelResume.getComponent(2)).getComponent(1)).setText(String.valueOf(employesActifs));
            
            // Projets en cours
            int projetsActifs = getCount(conn, "SELECT COUNT(DISTINCT id_projet) FROM Tache");
            ((JLabel)((JPanel)panelResume.getComponent(3)).getComponent(1)).setText(String.valueOf(projetsActifs));
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur de chargement des résumés", e);
        }
    }

    private int getCount(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void customizeChart(JFreeChart chart, Color color) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, color);
    }

    private void customizePieChart(PiePlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(Color.WHITE);
    }
}